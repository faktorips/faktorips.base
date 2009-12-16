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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

/**
 * This is the "Rename Policy Component Type" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenamePolicyCmptTypeProcessor extends RenameRefactoringProcessor {

    /**
     * Creates a <tt>RenamePolicyCmptTypeProcessor</tt>.
     * 
     * @param policyCmptType The <tt>IPolicyCmptType</tt> to be refactored.
     */
    public RenamePolicyCmptTypeProcessor(IPolicyCmptType policyCmptType) {
        super(policyCmptType);
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        if (getPolicyCmptType().isConfigurableByProductCmptType()) {
            updateProductCmptTypeReference();
        }
        updateSubtypeAndAssociationReferences();
        updateTestCaseTypeReferences();
        changeTypeName();
    }

    /**
     * Updates the reference to the <tt>IPolicyCmptType</tt> in the configuring
     * <tt>IProductCmptType</tt>.
     * <p>
     * This method may only be called if the <tt>IPolicyCmptType</tt> to be renamed is configurable.
     */
    private void updateProductCmptTypeReference() throws CoreException {
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(getIpsProject());
        productCmptType.setPolicyCmptType(getQualifiedNewTypeName());
        addModifiedSrcFile(productCmptType.getIpsSrcFile());
    }

    /**
     * Updates all sub types that inherit from the <tt>IPolicyCmptType</tt> to be renamed.
     * <p>
     * This operation also updates all references in associations of <tt>IPolicyCmptType</tt>s that
     * target the <tt>IPolicyCmptType</tt> to be renamed.
     */
    private void updateSubtypeAndAssociationReferences() throws CoreException {
        Set<IIpsSrcFile> policyCmptTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);

        // The policy component type to be renamed could reference itself.
        policyCmptTypeSrcFiles.add(getPolicyCmptType().getIpsSrcFile());

        for (IIpsSrcFile ipsSrcFile : policyCmptTypeSrcFiles) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsSrcFile.getIpsObject();

            // Update supertype reference if necessary.
            if (policyCmptType.getSupertype().equals(getPolicyCmptType().getQualifiedName())) {
                policyCmptType.setSupertype(getQualifiedNewTypeName());
                addModifiedSrcFile(ipsSrcFile);
            }

            // Update association references if any.
            for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyCmptType.getPolicyCmptTypeAssociations()) {
                if (policyCmptTypeAssociation.getTarget().equals(getPolicyCmptType().getQualifiedName())) {
                    policyCmptTypeAssociation.setTarget(getQualifiedNewTypeName());
                    addModifiedSrcFile(ipsSrcFile);
                }
            }
        }
    }

    /** Updates all references in <tt>ITestPolicyCmptTypeParameter</tt>s of <tt>ITestCaseType</tt>s. */
    private void updateTestCaseTypeReferences() throws CoreException {
        Set<IIpsSrcFile> testCaseTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            for (ITestPolicyCmptTypeParameter testParameter : testCaseType.getTestPolicyCmptTypeParameters()) {
                if (testParameter.getPolicyCmptType().equals(getPolicyCmptType().getQualifiedName())) {
                    testParameter.setPolicyCmptType(getQualifiedNewTypeName());
                    addModifiedSrcFile(ipsSrcFile);
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IPolicyCmptType</tt> to be refactored to the new name provided by
     * the user.
     */
    private void changeTypeName() {
        getPolicyCmptType().setName(getNewElementName());
        addModifiedSrcFile(getPolicyCmptType().getIpsSrcFile());
    }

    /** Returns the new qualified name of the <tt>IPolicyCmptType</tt> to be renamed. */
    private String getQualifiedNewTypeName() {
        String newTypeName = getNewElementName();
        if (getOriginalElementName().contains(".")) {
            newTypeName = getOriginalElementName().substring(0, getOriginalElementName().lastIndexOf('.') + 1)
                    + getNewElementName();
        }
        return newTypeName;
    }

    /** Returns the <tt>IPolicyCmptType</tt> to be refactored. */
    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "RenamePolicyCmptType";
    }

    @Override
    public String getProcessorName() {
        return "Rename Policy Component Type";
    }

}
