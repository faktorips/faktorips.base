/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename Type" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameTypeProcessor extends RenameRefactoringProcessor {

    /** New <tt>IIpsSrcFile</tt> containing a copy of the <tt>IPolicyCmptType</tt> to be renamed. */
    private IIpsSrcFile copiedIpsSrcFile;

    /**
     * Creates a <tt>RenameTypeProcessor</tt>.
     * 
     * @param type The <tt>IType</tt> to be refactored.
     */
    public RenameTypeProcessor(IType type) {
        super(type);
        getIgnoredValidationMessageCodes().add(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE);
        getIgnoredValidationMessageCodes().add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
    }

    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        // TODO AW: Check if type is valid.
    }

    /**
     * {@inheritDoc}
     * <p>
     * The source file of the <tt>IType</tt> to be renamed will be copied this early. Based on that
     * new source file and on the copied <tt>IType</tt> validation is performed. If the validation
     * fails the copy will be deleted so everything is left in a clean state (as it was before). If
     * only a warning or information validation message results the copy must also be deleted and
     * recreated later if the user really starts the refactoring.
     */
    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        try {
            copyToNewSourceFile(pm);
        } catch (CoreException e) {
            status.addFatalError(e.getLocalizedMessage());
            return;
        }

        IType copiedType = (IType)copiedIpsSrcFile.getIpsObject();

        MessageList validationMessageList = copiedType.validate(copiedType.getIpsProject());
        addValidationMessagesToStatus(validationMessageList, status);

        if (status.getEntries().length > 0) {
            copiedIpsSrcFile.getCorrespondingResource().delete(true, pm);
            copiedIpsSrcFile = null;
        }
    }

    @Override
    protected void validateNewElementNameThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        /*
         * Can't validate because for type validation the type's source file must be copied.
         * Validation will still be performed during final condition checking so it should be OK. We
         * still check if there is already a source file with the new name in this package however.
         */
        IIpsPackageFragment fragment = getType().getIpsPackageFragment();
        for (IIpsSrcFile ipsSrcFile : fragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            String relevantNamePart = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
            if (relevantNamePart.equals(getNewElementName())) {
                status.addFatalError(NLS.bind(Messages.RenameTypeProcessor_msgSourceFileAlreadyExists,
                        getNewElementName(), fragment.getName()));
                break;
            }
        }
    }

    /** Copies the <tt>IType</tt> to be renamed into a new source file with the new name. */
    private void copyToNewSourceFile(IProgressMonitor pm) throws CoreException {
        IPath destinationPath = getCorrespondingResource().getFullPath();
        destinationPath = destinationPath.removeLastSegments(1);
        destinationPath = destinationPath.append(getNewElementName() + "."
                + getType().getIpsObjectType().getFileExtension());

        getCorrespondingResource().copy(destinationPath, true, pm);

        copiedIpsSrcFile = getType().getIpsPackageFragment().getIpsSrcFile(
                getNewElementName() + "." + getType().getIpsObjectType().getFileExtension());
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        if (copiedIpsSrcFile == null) {
            copyToNewSourceFile(pm);
        }
        return null;
    }

    @Override
    protected Change refactorModel(IProgressMonitor pm) throws CoreException {
        // Initialized here because these source files are needed in multiple helper methods.
        Set<IIpsSrcFile> typeSrcFiles = findReferencingIpsSrcFiles(getType().getIpsObjectType());

        // The policy component type to be renamed could reference itself.
        typeSrcFiles.add(getType().getIpsSrcFile());

        if (getType() instanceof IPolicyCmptType) {
            updateConfiguringProductCmptTypeReference();
            updateTestCaseTypeParameterReferences();
        } else {
            // TODO AW: Implement ProductCmptType specific stuff here.
        }
        updateMethodParameterReferences(typeSrcFiles);
        updateAssociationReferences(typeSrcFiles);
        updateSubtypeReferences(typeSrcFiles);

        return deleteSourceFile();
    }

    /**
     * Updates the reference to the <tt>IPolicyCmptType</tt> in the configuring
     * <tt>IProductCmptType</tt>.
     * <p>
     * Only applicable to <tt>IPolicyCmptType</tt>s.
     */
    private void updateConfiguringProductCmptTypeReference() throws CoreException {
        if (!((IPolicyCmptType)getType()).isConfigurableByProductCmptType()) {
            return;
        }
        IProductCmptType productCmptType = ((IPolicyCmptType)getType()).findProductCmptType(getIpsProject());
        productCmptType.setPolicyCmptType(getQualifiedNewTypeName());
        addModifiedSrcFile(productCmptType.getIpsSrcFile());
    }

    /**
     * Updates all references in <tt>ITestPolicyCmptTypeParameter</tt>s of <tt>ITestCaseType</tt>s.
     * <p>
     * Only applicable to <tt>IPolicyCmptType</tt>s.
     */
    private void updateTestCaseTypeParameterReferences() throws CoreException {
        Set<IIpsSrcFile> testCaseTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            for (ITestPolicyCmptTypeParameter testParameter : testCaseType.getTestPolicyCmptTypeParameters()) {

                // A subclass of the policy component type to be renamed could also be referenced.
                IIpsProject ipsProject = testParameter.getIpsProject();
                IPolicyCmptType referencedPolicyCmptType = testParameter.findPolicyCmptType(ipsProject);
                if (referencedPolicyCmptType.isSubtypeOrSameType(getType(), ipsProject)) {

                    // Update the parameter's policy component type reference if it is not a
                    // subclass that is referenced.
                    if (testParameter.getPolicyCmptType().equals(getQualifiedOriginalTypeName())) {
                        testParameter.setPolicyCmptType(getQualifiedNewTypeName());
                        addModifiedSrcFile(ipsSrcFile);
                    }

                    // Update the test attributes where necessary.
                    for (ITestAttribute testAttribute : testParameter.getTestAttributes()) {
                        if (testAttribute.getPolicyCmptType().equals(getQualifiedOriginalTypeName())) {
                            testAttribute.setPolicyCmptType(getQualifiedNewTypeName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates all references to the <tt>IType</tt> to be renamed in parameter data types of
     * <tt>IMethod</tt>s.
     */
    private void updateMethodParameterReferences(Set<IIpsSrcFile> typeSrcFiles) throws CoreException {
        // We need all type source files.
        IpsObjectType otherObjectType = getType().getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE) ? IpsObjectType.POLICY_CMPT_TYPE
                : IpsObjectType.PRODUCT_CMPT_TYPE;
        Set<IIpsSrcFile> allTypeSrcFiles = findReferencingIpsSrcFiles(otherObjectType);
        allTypeSrcFiles.addAll(typeSrcFiles);

        for (IIpsSrcFile ipsSrcFile : allTypeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            for (IMethod method : type.getMethods()) {
                for (IParameter parameter : method.getParameters()) {
                    if (parameter.getDatatype().equals(getQualifiedOriginalTypeName())) {
                        parameter.setDatatype(getQualifiedNewTypeName());
                        addModifiedSrcFile(ipsSrcFile);
                    }
                }
            }
        }
    }

    /**
     * Updates all references in associations of <tt>IType</tt>s that target the <tt>IType</tt> to
     * be renamed.
     */
    private void updateAssociationReferences(Set<IIpsSrcFile> typeSrcFiles) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            for (IAssociation association : type.getAssociations()) {
                if (association.getTarget().equals(getType().getQualifiedName())) {
                    association.setTarget(getQualifiedNewTypeName());
                    addModifiedSrcFile(ipsSrcFile);
                }
            }
        }
    }

    /**
     * Updates the supertype property of all sub types that inherit from the <tt>IType</tt> to be
     * renamed.
     */
    private void updateSubtypeReferences(Set<IIpsSrcFile> typeSrcFiles) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            if (type.getSupertype().equals(getType().getQualifiedName())) {
                type.setSupertype(getQualifiedNewTypeName());
                addModifiedSrcFile(ipsSrcFile);
            }
        }
    }

    /**
     * Creates and returns a <tt>Change</tt> that describes the deletion of the resource
     * corresponding to the <tt>IIpsSrcFile</tt> of the <tt>IType</tt> to be renamed.
     */
    private Change deleteSourceFile() throws CoreException {
        return new DeleteResourceChange(getCorrespondingResource().getFullPath(), true);
    }

    /**
     * Returns the <tt>IResource</tt> corresponding to the <tt>IIpsSrcFile</tt> that contains the
     * <tt>IType</tt> to be renamed.
     */
    private IResource getCorrespondingResource() {
        return getType().getIpsSrcFile().getCorrespondingResource();
    }

    /** Returns the new qualified name of the <tt>IType</tt> to be renamed. */
    private String getQualifiedNewTypeName() {
        String newTypeName = getNewElementName();
        if (getQualifiedOriginalTypeName().contains(".")) {
            newTypeName = getQualifiedOriginalTypeName().substring(0,
                    getQualifiedOriginalTypeName().lastIndexOf('.') + 1)
                    + getNewElementName();
        }
        return newTypeName;
    }

    /** Returns the original qualified name of the <tt>IType</tt> to be renamed. */
    private String getQualifiedOriginalTypeName() {
        return getType().getQualifiedName();
    }

    /** Returns the <tt>IType</tt> to be refactored. */
    private IType getType() {
        return (IType)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "RenameType";
    }

    @Override
    public String getProcessorName() {
        return "Rename Type";
    }

}
