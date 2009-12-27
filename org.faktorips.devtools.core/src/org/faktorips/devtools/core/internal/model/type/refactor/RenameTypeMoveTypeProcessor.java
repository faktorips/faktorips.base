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
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsRenameMoveProcessor;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename Type" / "Move Type" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameTypeMoveTypeProcessor extends IpsRenameMoveProcessor {

    /** New <tt>IIpsSrcFile</tt> containing a copy of the <tt>IType</tt> to be refactored. */
    private IIpsSrcFile copiedIpsSrcFile;

    /**
     * Creates a <tt>RenameTypeMoveTypeProcessor</tt>.
     * 
     * @param type The <tt>IType</tt> to be refactored.
     * @param move Flag indicating whether a move is performed.
     */
    public RenameTypeMoveTypeProcessor(IType type, boolean move) {
        super(type, move);
        getIgnoredValidationMessageCodes().add(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE);
        getIgnoredValidationMessageCodes().add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
    }

    @Override
    protected void initOriginalLocation() {
        setOriginalLocation(new LocationDescriptor(getType().getIpsPackageFragment().getRoot(), getType()
                .getQualifiedName()));
    }

    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!(getType().isValid())) {
            status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeProcessor_msgTypeNotValid, getType().getName()));
        }
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

    /**
     * Copies the <tt>IType</tt> to be refactored into a new source file with the new name inside
     * the destination package.
     */
    private void copyToNewSourceFile(IProgressMonitor pm) throws CoreException {
        String targetFragmentName = QNameUtil.getPackageName(getTargetLocation().getQualifiedName());
        IIpsPackageFragment targetFragment = getTargetLocation().getIpsPackageFragmentRoot().getIpsPackageFragment(
                targetFragmentName);
        IPath destinationFolder = targetFragment.getCorrespondingResource().getFullPath();

        String targetName = QNameUtil.getUnqualifiedName(getTargetLocation().getQualifiedName());
        String targetSrcFileName = targetName + "." + getType().getIpsObjectType().getFileExtension();
        IPath destinationPath = destinationFolder.append(targetSrcFileName);

        getType().getIpsSrcFile().getCorrespondingResource().copy(destinationPath, true, pm);
        copiedIpsSrcFile = targetFragment.getIpsSrcFile(targetSrcFileName);
    }

    @Override
    protected void validateTargetLocationThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        /*
         * Can't validate because for type validation the type's source file must be copied.
         * Validation will still be performed during final condition checking so it should be OK. We
         * still check if there is already a source file with the new name in this package however.
         */
        IIpsPackageFragment fragment = getType().getIpsPackageFragment();
        for (IIpsSrcFile ipsSrcFile : fragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            String relevantNamePart = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
            String unqualifiedTargetName = QNameUtil.getUnqualifiedName(getTargetLocation().getQualifiedName());
            if (relevantNamePart.equals(unqualifiedTargetName)) {
                status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeProcessor_msgSourceFileAlreadyExists,
                        unqualifiedTargetName, fragment.getName()));
                break;
            }
        }
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
            updateConfiguredPolicyCmptTypeReference();
            updateProductReferences();
        }
        updateMethodParameterReferences(typeSrcFiles);
        updateAssociationReferences(typeSrcFiles);
        updateSubtypeReferences(typeSrcFiles);

        return deleteSourceFile();
    }

    /**
     * Updates the reference to the <tt>IPolicyCmptType</tt> to be refactored in the configuring
     * <tt>IProductCmptType</tt>.
     * <p>
     * Only applicable to <tt>IPolicyCmptType</tt>s.
     */
    private void updateConfiguringProductCmptTypeReference() throws CoreException {
        if (!((IPolicyCmptType)getType()).isConfigurableByProductCmptType()) {
            return;
        }
        IProductCmptType productCmptType = ((IPolicyCmptType)getType()).findProductCmptType(getIpsProject());
        productCmptType.setPolicyCmptType(getTargetLocation().getQualifiedName());
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
                    if (testParameter.getPolicyCmptType().equals(getOriginalLocation().getQualifiedName())) {
                        testParameter.setPolicyCmptType(getTargetLocation().getQualifiedName());
                        addModifiedSrcFile(ipsSrcFile);
                    }

                    // Update the test attributes where necessary.
                    for (ITestAttribute testAttribute : testParameter.getTestAttributes()) {
                        if (testAttribute.getPolicyCmptType().equals(getOriginalLocation().getQualifiedName())) {
                            testAttribute.setPolicyCmptType(getTargetLocation().getQualifiedName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the reference to the <tt>IProductCmptType</tt> to be refactored in the configured
     * <tt>IPolicyCmptType</tt>.
     * <p>
     * Only applicable to <tt>IProductCmptType</tt>s.
     */
    private void updateConfiguredPolicyCmptTypeReference() throws CoreException {
        if (!((IProductCmptType)getType()).isConfigurationForPolicyCmptType()) {
            return;
        }
        IPolicyCmptType policyCmptType = ((IProductCmptType)getType()).findPolicyCmptType(getIpsProject());
        policyCmptType.setProductCmptType(getTargetLocation().getQualifiedName());
        addModifiedSrcFile(policyCmptType.getIpsSrcFile());
    }

    /**
     * Updates references to the <tt>IProductCmptType</tt> in <tt>IProductCmpt</tt>s that are based
     * on the <tt>IProductCmptType</tt> to be refactored.
     * <p>
     * Only applicable to <tt>IProductCmptType</tt>s.
     */
    private void updateProductReferences() throws CoreException {
        if (getType().isAbstract()) {
            return;
        }
        Set<IIpsSrcFile> productSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            if (productCmpt.getProductCmptType().equals(getOriginalLocation().getQualifiedName())) {
                productCmpt.setProductCmptType(getTargetLocation().getQualifiedName());
                addModifiedSrcFile(ipsSrcFile);
            }
        }
    }

    /**
     * Updates all references in parameter data types of <tt>IMethod</tt>s to the <tt>IType</tt> to
     * be refactored.
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
                    if (parameter.getDatatype().equals(getOriginalLocation().getQualifiedName())) {
                        parameter.setDatatype(getTargetLocation().getQualifiedName());
                        addModifiedSrcFile(ipsSrcFile);
                    }
                }
            }
        }
    }

    /**
     * Updates all references in associations of <tt>IType</tt>s that target the <tt>IType</tt> to
     * be refactored.
     */
    private void updateAssociationReferences(Set<IIpsSrcFile> typeSrcFiles) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            for (IAssociation association : type.getAssociations()) {
                if (association.getTarget().equals(getOriginalLocation().getQualifiedName())) {
                    association.setTarget(getTargetLocation().getQualifiedName());
                    addModifiedSrcFile(ipsSrcFile);
                }
            }
        }
    }

    /**
     * Updates the supertype property of all sub types that inherit from the <tt>IType</tt> to be
     * refactored.
     */
    private void updateSubtypeReferences(Set<IIpsSrcFile> typeSrcFiles) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType potentialSubtype = (IType)ipsSrcFile.getIpsObject();
            if (potentialSubtype.getSupertype().equals(getOriginalLocation().getQualifiedName())) {
                potentialSubtype.setSupertype(getTargetLocation().getQualifiedName());
                addModifiedSrcFile(ipsSrcFile);
            }
        }
    }

    /**
     * Creates and returns a <tt>Change</tt> that describes the deletion of the resource
     * corresponding to the <tt>IIpsSrcFile</tt> of the <tt>IType</tt> to be refactored.
     */
    private Change deleteSourceFile() throws CoreException {
        return new DeleteResourceChange(getType().getIpsSrcFile().getCorrespondingResource().getFullPath(), true);
    }

    /** Returns the <tt>IType</tt> to be refactored. */
    private IType getType() {
        return (IType)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameTypeMoveTypeProcessor";
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameTypeMoveTypeProcessor_processorName;
    }

}
