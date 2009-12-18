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

package org.faktorips.devtools.core.internal.model.pctype.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

/**
 * This is the "Rename Policy Component Type Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenamePolicyCmptTypeAttributeProcessor extends RenameRefactoringProcessor {

    /**
     * Creates a <tt>RenamePolicyCmptTypeAttributeProcessor</tt>.
     * 
     * @param policyCmptTypeAttribute The <tt>IPolicyCmptTypeAttribute</tt> to be refactored.
     */
    public RenamePolicyCmptTypeAttributeProcessor(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        super(policyCmptTypeAttribute);
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        RefactoringStatus status = super.checkInitialConditions(pm);
        if (!(getPolicyCmptTypeAttribute().isValid())) {
            status.addFatalError(NLS.bind(Messages.RenamePolicyCmptTypeAttributeProcessor_msgAttributeNotValid,
                    getPolicyCmptTypeAttribute().getName()));
        } else {
            if (!(getPolicyCmptTypeAttribute().getPolicyCmptType().isValid())) {
                status.addFatalError(NLS.bind(Messages.RenamePolicyCmptTypeAttributeProcessor_msgTypeNotValid,
                        getPolicyCmptTypeAttribute().getPolicyCmptType().getName()));
            }
        }
        return status;
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        if (getPolicyCmptTypeAttribute().isProductRelevant()) {
            updateProductCmptReferences();
        }
        updateTestCaseTypeReferences();
        updateAttributeName();
    }

    /**
     * Updates all references to the <tt>IPolicyCmptTypeAttribute</tt> in referencing
     * <tt>IProductCmpt</tt>s.
     */
    private void updateProductCmptReferences() throws CoreException {
        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();

            /*
             * Continue if this product component does not reference the product component type that
             * configures the policy component type of the attribute to be renamed.
             */
            IProductCmptType referencedProductCmptType = productCmpt.findProductCmptType(productCmpt.getIpsProject());
            IProductCmptType configuringProductCmptType = getPolicyCmptType().findProductCmptType(getIpsProject());
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
                IConfigElement configElement = generation.getConfigElement(getOriginalElementName());
                if (configElement != null) {
                    configElement.setPolicyCmptTypeAttribute(getNewElementName());
                    addModifiedSrcFile(productCmpt.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Updates all references to the <tt>IPolicyCmptTypeAttribute</tt> in referencing
     * <tt>ITestCaseType</tt>s.
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
                if (!(referencedPolicyCmptType.isSubtypeOrSameType(getPolicyCmptType(), parameter.getIpsProject()))) {
                    continue;
                }
                for (ITestAttribute testAttribute : parameter.getTestAttributes(getOriginalElementName())) {
                    testAttribute.setAttribute(getNewElementName());
                    addModifiedSrcFile(testCaseType.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Updates the name of the <tt>IPolicyCmptTypeAttribute</tt> to be refactored to the new name
     * provided by the user.
     */
    private void updateAttributeName() {
        getPolicyCmptTypeAttribute().setName(getNewElementName());
        addModifiedSrcFile(getPolicyCmptTypeAttribute().getIpsSrcFile());
    }

    /** Returns the <tt>IPolicyCmptTypeAttribute</tt> to be refactored. */
    private IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)getIpsElement();
    }

    /**
     * Returns the <tt>IPolicyCmptType</tt> of the <tt>IPolicyCmptTypeAttribute</tt> to be
     * refactored.
     */
    private IPolicyCmptType getPolicyCmptType() {
        return getPolicyCmptTypeAttribute().getPolicyCmptType();
    }

    @Override
    public String getIdentifier() {
        return "RenamePolicyCmptTypeAttribute";
    }

    @Override
    public String getProcessorName() {
        return "Rename Policy Component Type Attribute";
    }

}
