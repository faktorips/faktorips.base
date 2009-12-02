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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * This is the "Rename Policy Component Type Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenamePolicyCmptTypeAttributeRefactoring extends RenameRefactoringProcessor {

    /**
     * Creates a <tt>RenamePolicyCmptTypeAttributeRefactoring</tt>.
     * 
     * @param policyCmptTypeAttribute The <tt>IPolicyCmptTypeAttribute</tt> to be refactored.
     */
    public RenamePolicyCmptTypeAttributeRefactoring(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        super(policyCmptTypeAttribute);
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        updateTestCaseTypeReferences();
        changeAttributeName();
    }

    /**
     * Changes the name of the <tt>IPolicyCmptTypeAttribute</tt> to be refactored to the new name
     * provided by the user.
     */
    private void changeAttributeName() {
        getPolicyCmptTypeAttribute().setName(getNewElementName());
        addModifiedSrcFile(getPolicyCmptTypeAttribute().getIpsSrcFile());
    }

    /**
     * Searches and updates all references to the <tt>IPolicyCmptTypeAttribute</tt> to be renamed in
     * <tt>ITestCaseType</tt>s.
     */
    private void updateTestCaseTypeReferences() throws CoreException {
        IIpsSrcFile[] testSrcFiles = getPolicyCmptTypeAttribute().getIpsProject().findIpsSrcFiles(
                IpsObjectType.TEST_CASE_TYPE);
        for (IIpsSrcFile ipsSrcFile : testSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            ITestPolicyCmptTypeParameter[] testParameters = testCaseType.getTestPolicyCmptTypeParameters();
            for (ITestPolicyCmptTypeParameter parameter : testParameters) {
                for (ITestAttribute testAttribute : parameter.getTestAttributes(getPolicyCmptTypeAttribute())) {
                    testAttribute.setAttribute(getNewElementName());
                    addModifiedSrcFile(ipsSrcFile);
                }
            }
        }
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
        return "RenamePolicyCmptTypeAttributeRefactoring";
    }

    @Override
    public String getProcessorName() {
        return "Rename Policy Component Type Attribute Refactoring Processor";
    }

}
