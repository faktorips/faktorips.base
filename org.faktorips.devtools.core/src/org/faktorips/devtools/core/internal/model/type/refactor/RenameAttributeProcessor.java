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
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAttributeProcessor extends IpsRenameProcessor {

    /** Set containing all potentially referencing product components. */
    private Set<IIpsSrcFile> productCmptSrcFiles;

    /** Set containing all potentially referencing test case types. */
    private Set<IIpsSrcFile> testCaseTypeCmptSrcFiles;

    /**
     * Creates a <tt>RenameAttributeProcessor</tt>.
     * 
     * @param attribute The <tt>IAttribute</tt> to be refactored.
     */
    public RenameAttributeProcessor(IAttribute attribute) {
        super(attribute);
    }

    @Override
    public void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!(getAttribute().isValid())) {
            status.addFatalError(NLS.bind(Messages.RenameAttributeProcessor_msgAttributeNotValid, getAttribute()
                    .getName()));
        } else {
            if (!(getAttribute().getType().isValid())) {
                status.addFatalError(NLS.bind(Messages.RenameTypeMoveTypeHelper_msgTypeNotValid, getAttribute()
                        .getType().getName()));
            }
        }
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getAttribute().getIpsSrcFile());
        productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            addIpsSrcFile(ipsSrcFile);
        }
        testCaseTypeCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testCaseTypeCmptSrcFiles) {
            addIpsSrcFile(ipsSrcFile);
        }
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        /*
         * TODO AW: Stop broadcasting change events would be good for name changing here, make it
         * published?
         */
        getAttribute().setName(getNewName());

        IIpsProject ipsProject = getAttribute().getIpsProject();
        MessageList validationMessageList = getAttribute().validate(ipsProject);
        validationMessageList.add(getType().validate(ipsProject));
        addValidationMessagesToStatus(validationMessageList, status);

        getAttribute().setName(getOriginalName());

        // The source file was not really modified.
        getAttribute().getIpsSrcFile().markAsClean();
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
