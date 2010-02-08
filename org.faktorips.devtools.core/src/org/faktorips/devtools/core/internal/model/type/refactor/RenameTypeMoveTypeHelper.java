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
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
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
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * The <tt>RenameTypeMoveTypeHelper</tt> bundles common functionality of the "Rename Type" and
 * "Move Type" refactorings.
 * 
 * @author Alexander Weickmann
 */
public final class RenameTypeMoveTypeHelper {

    /** New <tt>IIpsSrcFile</tt> containing a copy of the <tt>IType</tt> to be refactored. */
    private IIpsSrcFile copiedIpsSrcFile;

    /** The <tt>IType</tt> to be refactored. */
    private final IType type;

    /** The <tt>IpsRefactoringProcessor</tt> that uses this helper. */
    private final IpsRefactoringProcessor refactoringProcessor;

    /**
     * Set containing all <tt>IIpsSrcFile</tt>s that potentially reference the <tt>IType</tt> to be
     * refactored and that have the same <tt>IpsObjectType</tt>.
     */
    private Set<IIpsSrcFile> typeSrcFiles;

    /** Set containing all potentially referencing product components. */
    private Set<IIpsSrcFile> productCmptSrcFiles;

    /** Set containing all potentially referencing test case types. */
    private Set<IIpsSrcFile> testCaseTypeSrcFiles;

    /**
     * Creates a <tt>RenameTypeMoveTypeHelper</tt>.
     * 
     * @param refactoringProcessor The <tt>IpsRefactoringProcessor</tt> that uses this helper.
     * @param type The <tt>IType</tt> to be refactored.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public RenameTypeMoveTypeHelper(IpsRefactoringProcessor refactoringProcessor, IType type) {
        ArgumentCheck.notNull(new Object[] { refactoringProcessor, type });
        this.refactoringProcessor = refactoringProcessor;
        this.type = type;
    }

    /**
     * Adds message codes to the set of ignored validation message codes that must be ignored by the
     * "Rename Type" and "Move Type" refactorings.
     * <p>
     * The configuring <tt>IProductCmptType</tt> / configured <tt>IPolicyCmptType</tt> does not
     * reference the copy of the <tt>IType</tt> that is created during the refactoring so this must
     * be ignored during refactoring validation.
     */
    public void addIgnoredValidationMessageCodes(Set<String> ignoredValidationMessageCodes) {
        ignoredValidationMessageCodes.add(IPolicyCmptType.MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE);
        ignoredValidationMessageCodes.add(IProductCmptType.MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE);
    }

    /**
     * Checks that the <tt>IType</tt> is valid.
     * 
     * @see IpsRefactoringProcessor#checkInitialConditionsThis(RefactoringStatus, IProgressMonitor)
     */
    public void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!(type.isValid())) {
            status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeHelper_msgTypeNotValid, type.getName()));
        }
    }

    /**
     * @see IpsRefactoringProcessor#addIpsSrcFiles()
     */
    public void addIpsSrcFiles() throws CoreException {
        typeSrcFiles = refactoringProcessor.findReferencingIpsSrcFiles(type.getIpsObjectType());
        IpsObjectType otherObjectType = type.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE) ? IpsObjectType.POLICY_CMPT_TYPE
                : IpsObjectType.PRODUCT_CMPT_TYPE;
        Set<IIpsSrcFile> otherTypeSrcFiles = refactoringProcessor.findReferencingIpsSrcFiles(otherObjectType);
        typeSrcFiles.addAll(otherTypeSrcFiles);

        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            addIpsSrcFile(ipsSrcFile);
        }
        if (type instanceof IProductCmptType) {
            if (!(type.isAbstract())) {
                productCmptSrcFiles = refactoringProcessor.findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
                for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
                    addIpsSrcFile(ipsSrcFile);
                }
            }
            if (((IProductCmptType)type).isConfigurationForPolicyCmptType()) {
                IPolicyCmptType policyCmptType = ((IProductCmptType)type).findPolicyCmptType(type.getIpsProject());
                addIpsSrcFile(policyCmptType.getIpsSrcFile());
            }
        } else {
            testCaseTypeSrcFiles = refactoringProcessor.findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
            for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
                addIpsSrcFile(ipsSrcFile);
            }
            if (((IPolicyCmptType)type).isConfigurableByProductCmptType()) {
                IProductCmptType productCmptType = ((IPolicyCmptType)type).findProductCmptType(type.getIpsProject());
                addIpsSrcFile(productCmptType.getIpsSrcFile());
            }
        }
    }

    /**
     * Can't validate because for type validation the type's source file must be copied. Validation
     * will still be performed during final condition checking so it should be OK. We check if there
     * is already a source file with the new name in this package however.
     * 
     * @see IpsRenameProcessor#validateUserInputThis(RefactoringStatus, IProgressMonitor)
     * @see IpsMoveProcessor#validateUserInputThis(RefactoringStatus, IProgressMonitor)
     */
    public void validateUserInputThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm) throws CoreException {

        for (IIpsSrcFile ipsSrcFile : targetIpsPackageFragment.getIpsSrcFiles()) {
            String sourceFileName = ipsSrcFile.getName();
            String relevantNamePart = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
            if (relevantNamePart.equals(newName)) {
                status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeHelper_msgSourceFileAlreadyExists, newName,
                        targetIpsPackageFragment.getName()));
                break;
            }
        }
    }

    /**
     * The source file of the <tt>IType</tt> will be copied this early. Based on that new source
     * file and on the copied <tt>IType</tt> validation is performed. If the validation fails the
     * copy will be deleted so everything is left in a clean state (as it was before). If only a
     * warning or information validation message results the copy must also be deleted and recreated
     * later if the user really starts the refactoring.
     * 
     * @see IpsRefactoringProcessor#checkFinalConditionsThis(RefactoringStatus, IProgressMonitor,
     *      CheckConditionsContext)
     */
    public void checkFinalConditionsThis(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        try {
            copySourceFileToTargetLocation(targetIpsPackageFragment, newName, pm);
        } catch (CoreException e) {
            status.addFatalError(e.getLocalizedMessage());
            return;
        }

        IType copiedType = (IType)copiedIpsSrcFile.getIpsObject();

        MessageList validationMessageList = copiedType.validate(copiedType.getIpsProject());
        refactoringProcessor.addValidationMessagesToStatus(validationMessageList, status);

        // Delete the copy again in every case because participants condition checking may fail.
        copiedIpsSrcFile.getCorrespondingResource().delete(true, pm);
        copiedIpsSrcFile = null;
    }

    /**
     * Copies the <tt>IType</tt> to be refactored into a new source file with the desired new name
     * inside the desired destination package.
     */
    private void copySourceFileToTargetLocation(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IProgressMonitor pm) throws CoreException {

        IPath destinationFolder = targetIpsPackageFragment.getCorrespondingResource().getFullPath();

        String targetSrcFileName = newName + "." + type.getIpsObjectType().getFileExtension();
        IPath destinationPath = destinationFolder.append(targetSrcFileName);

        type.getIpsSrcFile().getCorrespondingResource().copy(destinationPath, true, pm);
        copiedIpsSrcFile = targetIpsPackageFragment.getIpsSrcFile(targetSrcFileName);
    }

    public void refactorIpsModel(IIpsPackageFragment targetIpsPackageFragment, String newName, IProgressMonitor pm)
            throws CoreException {

        copySourceFileToTargetLocation(targetIpsPackageFragment, newName, pm);

        if (type instanceof IPolicyCmptType) {
            updateConfiguringProductCmptTypeReference(targetIpsPackageFragment, newName);
            updateTestCaseTypeParameterReferences(targetIpsPackageFragment, newName);
        } else {
            updateConfiguredPolicyCmptTypeReference(targetIpsPackageFragment, newName);
            updateProductCmptReferences(targetIpsPackageFragment, newName);
        }
        updateMethodParameterReferences(targetIpsPackageFragment, newName);
        updateAssociationReferences(targetIpsPackageFragment, newName);
        updateSubtypeReferences(targetIpsPackageFragment, newName);

        deleteOldIpsSourceFile(pm);
    }

    /**
     * Updates the reference to the <tt>IPolicyCmptType</tt> to be refactored in the configuring
     * <tt>IProductCmptType</tt>.
     * <p>
     * Only applicable to <tt>IPolicyCmptType</tt>s.
     */
    private void updateConfiguringProductCmptTypeReference(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        if (!((IPolicyCmptType)type).isConfigurableByProductCmptType()) {
            return;
        }
        IProductCmptType productCmptType = ((IPolicyCmptType)type).findProductCmptType(type.getIpsProject());
        productCmptType.setPolicyCmptType(getNewQualifiedName(targetIpsPackageFragment, newName));
    }

    /**
     * Updates all references in <tt>ITestPolicyCmptTypeParameter</tt>s of <tt>ITestCaseType</tt>s.
     * <p>
     * Only applicable to <tt>IPolicyCmptType</tt>s.
     */
    private void updateTestCaseTypeParameterReferences(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();

            for (ITestPolicyCmptTypeParameter testParameter : testCaseType.getTestPolicyCmptTypeParameters()) {
                updateTestCaseTypeParameter(targetIpsPackageFragment, newName, ipsSrcFile, testParameter);
                updateTestCaseTypeParameterChildren(testParameter, targetIpsPackageFragment, newName, ipsSrcFile);
            }
        }
    }

    /** Goes recursively over all child <tt>ITestPolicyCmptTypeParameter</tt>s. */
    private void updateTestCaseTypeParameterChildren(ITestPolicyCmptTypeParameter testParameter,
            IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IIpsSrcFile ipsSrcFile) throws CoreException {

        for (ITestPolicyCmptTypeParameter child : testParameter.getTestPolicyCmptTypeParamChilds()) {
            if (child.hasChildren()) {
                updateTestCaseTypeParameterChildren(child, targetIpsPackageFragment, newName, ipsSrcFile);
            }
            updateTestCaseTypeParameter(targetIpsPackageFragment, newName, ipsSrcFile, child);
        }
    }

    /** Updates all references in the provided <tt>ITestPolicyCmptTypeParameter</tt>. */
    private void updateTestCaseTypeParameter(IIpsPackageFragment targetIpsPackageFragment,
            String newName,
            IIpsSrcFile ipsSrcFile,
            ITestPolicyCmptTypeParameter testParameter) throws CoreException {

        // A subclass of the policy component type to be renamed could also be referenced.
        IIpsProject ipsProject = testParameter.getIpsProject();
        IPolicyCmptType referencedPolicyCmptType = testParameter.findPolicyCmptType(ipsProject);
        if (referencedPolicyCmptType.isSubtypeOrSameType(type, ipsProject)) {

            // Update the parameter's policy component type reference if it is not a
            // subclass that is referenced.
            if (testParameter.getPolicyCmptType().equals(getOriginalQualifiedName())) {
                testParameter.setPolicyCmptType(getNewQualifiedName(targetIpsPackageFragment, newName));
            }

            // Update the test attributes where necessary.
            for (ITestAttribute testAttribute : testParameter.getTestAttributes()) {
                if (testAttribute.getPolicyCmptType().equals(getOriginalQualifiedName())) {
                    testAttribute.setPolicyCmptType(getNewQualifiedName(targetIpsPackageFragment, newName));
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
    private void updateConfiguredPolicyCmptTypeReference(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        if (!((IProductCmptType)type).isConfigurationForPolicyCmptType()) {
            return;
        }
        IPolicyCmptType policyCmptType = ((IProductCmptType)type).findPolicyCmptType(type.getIpsProject());
        policyCmptType.setProductCmptType(getNewQualifiedName(targetIpsPackageFragment, newName));
    }

    /**
     * Updates references to the <tt>IProductCmptType</tt> in <tt>IProductCmpt</tt>s that are based
     * on the <tt>IProductCmptType</tt> to be refactored.
     * <p>
     * Only applicable to <tt>IProductCmptType</tt>s.
     */
    private void updateProductCmptReferences(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        if (type.isAbstract()) {
            return;
        }

        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            if (productCmpt.getProductCmptType().equals(getOriginalQualifiedName())) {
                productCmpt.setProductCmptType(getNewQualifiedName(targetIpsPackageFragment, newName));
            }
        }
    }

    /**
     * Updates all references in parameter data types of <tt>IMethod</tt>s to the <tt>IType</tt> to
     * be refactored.
     */
    private void updateMethodParameterReferences(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            for (IMethod method : type.getMethods()) {
                for (IParameter parameter : method.getParameters()) {
                    if (parameter.getDatatype().equals(getOriginalQualifiedName())) {
                        parameter.setDatatype(getNewQualifiedName(targetIpsPackageFragment, newName));
                    }
                }
            }
        }
    }

    /**
     * Updates all references in associations of <tt>IType</tt>s that target the <tt>IType</tt> to
     * be refactored.
     */
    private void updateAssociationReferences(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType type = (IType)ipsSrcFile.getIpsObject();
            for (IAssociation association : type.getAssociations()) {
                if (association.getTarget().equals(getOriginalQualifiedName())) {
                    association.setTarget(getNewQualifiedName(targetIpsPackageFragment, newName));
                }
            }
        }
    }

    /**
     * Updates the supertype property of all sub types that inherit from the <tt>IType</tt> to be
     * refactored.
     */
    private void updateSubtypeReferences(IIpsPackageFragment targetIpsPackageFragment, String newName)
            throws CoreException {

        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType potentialSubtype = (IType)ipsSrcFile.getIpsObject();
            if (potentialSubtype.getSupertype().equals(getOriginalQualifiedName())) {
                potentialSubtype.setSupertype(getNewQualifiedName(targetIpsPackageFragment, newName));
            }
        }
    }

    /**
     * Adds the given <tt>IIpsSrcFile</tt> to the list of source files managed by the
     * <tt>IIpsRefactoringProcessor</tt>.
     * <p>
     * This operation assures that the given source file will not be added in case it is the source
     * file of the <tt>IType</tt> to be refactored. This source file will be deleted after the
     * refactoring and it would lead to an exception if it would be saved.
     */
    private void addIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        if (type.getIpsSrcFile().equals(ipsSrcFile)) {
            return;
        }
        refactoringProcessor.addIpsSrcFile(ipsSrcFile);
    }

    /** Deletes the original IPS source file that is now no longer needed. */
    private void deleteOldIpsSourceFile(IProgressMonitor pm) throws CoreException {
        type.getIpsSrcFile().getCorrespondingResource().delete(true, pm);
    }

    /** Returns the <tt>IType</tt>'s original qualified name. */
    private String getOriginalQualifiedName() {
        return getQualifiedName(type.getIpsPackageFragment(), type.getName());
    }

    /** Returns the <tt>IType</tt>'s new qualified name. */
    private String getNewQualifiedName(IIpsPackageFragment targetIpsPackageFragment, String newName) {
        return getQualifiedName(targetIpsPackageFragment, newName);
    }

    /**
     * Builds and returns a qualified name from the provided <tt>IIpsPackageFragment</tt> and
     * unqualified name.
     */
    private String getQualifiedName(IIpsPackageFragment ipsPackageFragment, String name) {
        return ipsPackageFragment.isDefaultPackage() ? name : ipsPackageFragment.getName() + "." + name;
    }

}
