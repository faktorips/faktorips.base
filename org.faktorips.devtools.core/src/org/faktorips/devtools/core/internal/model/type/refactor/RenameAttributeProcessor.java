/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
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
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAttributeProcessor extends IpsRenameProcessor {

    /** Set containing all potentially referencing product components. */
    private Set<IIpsSrcFile> productCmptSrcFiles;

    /** Set containing all potentially referencing test case types. */
    private Set<IIpsSrcFile> testCaseTypeCmptSrcFiles;

    /** Set containing all potentially referencing policy component types. */
    private Set<IIpsSrcFile> typeSrcFiles;

    public RenameAttributeProcessor(IAttribute attribute) {
        super(attribute, attribute.getName());
        addIgnoredValidationMessageCodes();
    }

    private void addIgnoredValidationMessageCodes() {
        getIgnoredValidationMessageCodes().add(IValidationRule.MSGCODE_UNDEFINED_ATTRIBUTE);
        getIgnoredValidationMessageCodes().add(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        try {
            result.add(getIpsSrcFile());
            productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
            for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
                result.add(ipsSrcFile);
            }
            typeSrcFiles = findReferencingIpsSrcFiles(getAttribute().getIpsObject().getIpsObjectType());
            for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
                result.add(ipsSrcFile);
            }
            if (getAttribute() instanceof IPolicyCmptTypeAttribute) {
                testCaseTypeCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
                for (IIpsSrcFile ipsSrcFile : testCaseTypeCmptSrcFiles) {
                    result.add(ipsSrcFile);
                }
            }
            // Collect source files for overwritten attributes
            List<IAttribute> overwrittenAttributes = getAllOverwrittenAttributes();
            for (IAttribute overwrittenAttribute : overwrittenAttributes) {
                result.add(overwrittenAttribute.getIpsSrcFile());
            }

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        validationMessageList.add(getAttribute().validate(getIpsProject()));
        validationMessageList.add(getType().validate(getIpsProject()));
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        addAffectedSrcFiles(modificationSet);
        if (getAttribute() instanceof IProductCmptTypeAttribute) {
            updateProductCmptAttributeValueReferences();
        } else {
            updateValidationRule();
            updateProductCmptConfigElementReferences();
            updateTestCaseTypeReferences();
        }
        updateSuperHierarchyAttributes();
        updateSubHierarchyAttributes();
        updateAttributeName();
        return modificationSet;
    }

    /**
     * Updates all references to the {@link IPolicyCmptTypeAttribute} in overwriting attributes of
     * the sub type hierarchy.
     */
    private void updateSubHierarchyAttributes() {
        for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
            IType cmptType = (IType)ipsSrcFile.getIpsObject();

            // The policy component type needs to be a sub type of the attribute's type
            if (!(cmptType.isSubtypeOf(getType(), cmptType.getIpsProject()))) {
                continue;
            }

            // An overwriting attribute with the same name must exist
            IAttribute potentialOverwritingAttribute = cmptType.getAttribute(getOriginalName());
            if (potentialOverwritingAttribute == null || !(potentialOverwritingAttribute.isOverwrite())) {
                continue;
            }

            // At this point a valid overwriting attribute was found
            potentialOverwritingAttribute.setName(getNewName());
        }
    }

    /**
     * Updates all references to the {@link IPolicyCmptTypeAttribute} in overwritten attributes of
     * the super type hierarchy.
     */
    private void updateSuperHierarchyAttributes() throws CoreException {
        List<IAttribute> attributesToRename = getAllOverwrittenAttributes();

        /*
         * Rename the collected attributes (cannot do this in one step as findOverwrittenAttribute
         * would not work this way)
         */
        for (IAttribute attribute : attributesToRename) {
            attribute.setName(getNewName());
        }
    }

    private List<IAttribute> getAllOverwrittenAttributes() throws CoreException {
        List<IAttribute> attributesToRename = new ArrayList<IAttribute>(1);

        // Collect overwritten attributes
        IAttribute overwrittenAttribute = null;
        while (true) {
            if (overwrittenAttribute == null) {
                overwrittenAttribute = getAttribute().findOverwrittenAttribute(getIpsProject());
            } else {
                overwrittenAttribute = overwrittenAttribute.findOverwrittenAttribute(getIpsProject());
            }
            if (overwrittenAttribute == null) {
                break;
            }
            attributesToRename.add(overwrittenAttribute);
        }
        return attributesToRename;
    }

    /**
     * Updates all references to the <tt>IAttribute</tt> in <tt>IAttributeValue</tt>s of referencing
     * <tt>IProductCmpt</tt>s.
     * <p>
     * Only applicable to <tt>IProductCmptTypeAttribute</tt>s.
     */
    private void updateProductCmptAttributeValueReferences() {
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
                IAttributeValue attributeValue = generation.getAttributeValue(getOriginalName());
                if (attributeValue != null) {
                    attributeValue.setAttribute(getNewName());
                }
            }
        }
    }

    /**
     * Updates the reference to the <tt>IAttribute</tt> in the corresponding
     * <tt>IValidationRule</tt> if any exists.
     * <p>
     * Only applicable to <tt>IPolicyCmptTypeAttribute</tt>s.
     */
    private void updateValidationRule() {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)getAttribute();
        IValidationRule validationRule = policyCmptTypeAttribute.findValueSetRule(getIpsProject());
        if (validationRule != null) {
            for (int i = 0; i < validationRule.getValidatedAttributes().length; i++) {
                String attributeName = validationRule.getValidatedAttributes()[i];
                if (attributeName.equals(getOriginalName())) {
                    validationRule.setValidatedAttributeAt(i, getNewName());
                    break;
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

        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

            /*
             * Continue if this product component does not reference the product component type that
             * configures the policy component type of the attribute to be renamed.
             */
            IProductCmptType referencedProductCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            IProductCmptType configuringProductCmptType = ((IPolicyCmptType)getType())
                    .findProductCmptType(getIpsProject());
            if (!(referencedProductCmptType
                    .isSubtypeOrSameType(configuringProductCmptType, productCmpt.getIpsProject()))) {
                continue;
            }
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                IConfigElement configElement = generation.getConfigElement(getOriginalName());
                if (configElement != null) {
                    configElement.setPolicyCmptTypeAttribute(getNewName());
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
                for (ITestAttribute testAttribute : parameter.getTestAttributes(getOriginalName())) {
                    testAttribute.setAttribute(getNewName());
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IAttribute</tt> to be refactored to the new name provided by the
     * user.
     */
    private void updateAttributeName() {
        getAttribute().setName(getNewName());
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameAttributeProcessor_processorName;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    private IAttribute getAttribute() {
        return (IAttribute)getIpsElement();
    }

    private IType getType() {
        return getAttribute().getType();
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getAttribute().getIpsSrcFile();
    }

}
