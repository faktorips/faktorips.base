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

package org.faktorips.devtools.core.internal.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
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
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        if (getPolicyCmptTypeAttribute().isProductRelevant()) {
            updateProductCmptReferences();
        }
        updateTestCaseTypeReferences();
        changeAttributeName();
    }

    /**
     * Updates all references to the <tt>IPolicyCmptTypeAttribute</tt> in referencing
     * <tt>IProductCmpt</tt>s.
     */
    private void updateProductCmptReferences() throws CoreException {
        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingSourceFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
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
        Set<IIpsSrcFile> testCaseTypeCmptSrcFiles = findReferencingSourceFiles(IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testCaseTypeCmptSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            for (ITestPolicyCmptTypeParameter parameter : testCaseType.getTestPolicyCmptTypeParameters()) {
                for (ITestAttribute testAttribute : parameter.getTestAttributes(getPolicyCmptTypeAttribute())) {
                    testAttribute.setAttribute(getNewElementName());
                    addModifiedSrcFile(testCaseType.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IPolicyCmptTypeAttribute</tt> to be refactored to the new name
     * provided by the user.
     */
    private void changeAttributeName() {
        getPolicyCmptTypeAttribute().setName(getNewElementName());
        addModifiedSrcFile(getPolicyCmptTypeAttribute().getIpsSrcFile());
    }

    /** Returns the <tt>IPolicyCmptTypeAttribute</tt> to be refactored. */
    private IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute() {
        return (IPolicyCmptTypeAttribute)getIpsElement();
    }

    @Override
    public boolean isApplicable() throws CoreException {
        for (Object element : getElements()) {
            if (!(element instanceof IPolicyCmptTypeAttribute)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return "RenamePolicyCmptTypeAttributeProcessor";
    }

    @Override
    public String getProcessorName() {
        return "Rename Policy Component Type Attribute Refactoring Processor";
    }

}
