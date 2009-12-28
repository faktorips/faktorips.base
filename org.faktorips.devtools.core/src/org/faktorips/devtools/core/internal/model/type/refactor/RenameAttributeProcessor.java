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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsRenameMoveProcessor;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAttributeProcessor extends IpsRenameMoveProcessor {

    /**
     * Creates a <tt>RenameAttributeProcessor</tt>.
     * 
     * @param attribute The <tt>IAttribute</tt> to be refactored.
     */
    public RenameAttributeProcessor(IAttribute attribute) {
        super(attribute, false);
    }

    @Override
    protected LocationDescriptor initOriginalLocation() {
        return new LocationDescriptor(getType().getIpsPackageFragment().getRoot(), getType().getQualifiedName() + "."
                + getAttribute().getName());
    }

    @Override
    public void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!(getAttribute().isValid())) {
            status.addFatalError(NLS.bind(Messages.RenameAttributeProcessor_msgAttributeNotValid, getAttribute()
                    .getName()));
        } else {
            if (!(getAttribute().getType().isValid())) {
                status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeProcessor_msgTypeNotValid, getAttribute()
                        .getType().getName()));
            }
        }
    }

    @Override
    protected void validateTargetLocationThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        /*
         * TODO AW: Stop broadcasting change events would be good for name changing here, make it
         * published?
         */
        getAttribute().setName(getNewAttributeName());

        IIpsProject ipsProject = getAttribute().getIpsProject();
        MessageList validationMessageList = getAttribute().validate(ipsProject);
        validationMessageList.add(getType().validate(ipsProject));
        addValidationMessagesToStatus(validationMessageList, status);

        getAttribute().setName(getOriginalAttributeName());

        // The source file was not really modified.
        getAttribute().getIpsSrcFile().markAsClean();
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {
        // Nothing more to do.
    }

    @Override
    protected Change refactorIpsModel(IProgressMonitor pm) throws CoreException {
        if (getAttribute() instanceof IProductCmptTypeAttribute) {
            updateProductCmptAttributeValueReferences();
        } else {
            updateProductCmptConfigElementReferences();
            updateTestCaseTypeReferences();
        }
        updateAttributeName();
        return null;
    }

    /**
     * Updates all references to the <tt>IAttribute</tt> in <tt>IAttributeValue</tt>s of referencing
     * <tt>IProductCmpt</tt>s.
     * <p>
     * Only applicable to <tt>IProductCmptTypeAttribute</tt>s.
     */
    private void updateProductCmptAttributeValueReferences() throws CoreException {
        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

            /*
             * Continue if this product component does not reference the product component type of
             * the attribute to be renamed.
             */
            IProductCmptType referencedProductCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            if (!(referencedProductCmptType.isSubtypeOrSameType(getType(), productCmpt.getIpsProject()))) {
                continue;
            }
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                IAttributeValue attributeValue = generation.getAttributeValue(getOriginalAttributeName());
                if (attributeValue != null) {
                    attributeValue.setAttribute(getNewAttributeName());
                    addModifiedSrcFile(productCmpt.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Updates all references to the <tt>IPolicyCmptTypeAttribute</tt> in <tt>IConfigElement</tt>s
     * of referencing <tt>IProductCmpt</tt>s.
     * <p>
     * Only applicable to <tt>IPolicyCmptTypeAttribute</tt>s.
     */
    private void updateProductCmptConfigElementReferences() throws CoreException {
        if (!((IPolicyCmptTypeAttribute)getAttribute()).isProductRelevant()) {
            return;
        }

        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

            /*
             * Continue if this product component does not reference the product component type that
             * configures the policy component type of the attribute to be renamed.
             */
            IProductCmptType referencedProductCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            IProductCmptType configuringProductCmptType = ((IPolicyCmptType)getType())
                    .findProductCmptType(getIpsProject());
            /*
             * TODO AW: If a method isSubtypeOrSameType(String qualifiedName) would be provided,
             * this and the other refactorings could have better performance.
             */
            if (!(referencedProductCmptType
                    .isSubtypeOrSameType(configuringProductCmptType, productCmpt.getIpsProject()))) {
                continue;
            }
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                IConfigElement configElement = generation.getConfigElement(getOriginalAttributeName());
                if (configElement != null) {
                    configElement.setPolicyCmptTypeAttribute(getNewAttributeName());
                    addModifiedSrcFile(productCmpt.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Updates all references to the <tt>IAttribute</tt> in referencing <tt>ITestCaseType</tt>s.
     * <p>
     * Only applicable to <tt>IPolicyCmptTypeAttribute</tt>s.
     */
    private void updateTestCaseTypeReferences() throws CoreException {
        Set<IIpsSrcFile> testCaseTypeCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testCaseTypeCmptSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            for (ITestPolicyCmptTypeParameter parameter : testCaseType.getTestPolicyCmptTypeParameters()) {
                /*
                 * Continue if this parameter does not reference the policy component type of the
                 * attribute to be renamed.
                 */
                IPolicyCmptType referencedPolicyCmptType = parameter.findPolicyCmptType(parameter.getIpsProject());
                if (!(referencedPolicyCmptType.isSubtypeOrSameType(getType(), parameter.getIpsProject()))) {
                    continue;
                }
                for (ITestAttribute testAttribute : parameter.getTestAttributes(getOriginalAttributeName())) {
                    testAttribute.setAttribute(getNewAttributeName());
                    addModifiedSrcFile(testCaseType.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IAttribute</tt> to be refactored to the new name provided by the
     * user.
     */
    private void updateAttributeName() {
        getAttribute().setName(getNewAttributeName());
        addModifiedSrcFile(getAttribute().getIpsSrcFile());
    }

    /** Returns the new name of the <tt>IAttribute</tt> to be renamed, provided by the user. */
    private String getNewAttributeName() {
        return QNameUtil.getUnqualifiedName(getTargetLocation().getQualifiedName());
    }

    /** Returns the original name of the <tt>IAttribute</tt> to be renamed. */
    private String getOriginalAttributeName() {
        return QNameUtil.getUnqualifiedName(getOriginalLocation().getQualifiedName());
    }

    /** Returns the <tt>IAttribute</tt> to be refactored. */
    private IAttribute getAttribute() {
        return (IAttribute)getIpsElement();
    }

    /** Returns the <tt>IType</tt> of the <tt>IAttribute</tt> to be refactored. */
    private IType getType() {
        return getAttribute().getType();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameAttributeProcessor";
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameAttributeProcessor_processorName;
    }

}
