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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameIpsObjectProcessorTest extends AbstractMoveRenameIpsObjectTest {

    private static final String NEW_OBJECT_NAME = "NewObjectName";

    public void testCheckFinalConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("test");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName(PRODUCT_CMPT_TYPE_NAME);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsInvalidTypeName() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("$§§  $");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testRenamePolicyCmptType() throws CoreException {
        performRenamePolicyCmptType(NEW_OBJECT_NAME);
    }

    public void testRenameSuperPolicyCmptType() throws CoreException {
        performRenameRefactoring(superPolicyCmptType, NEW_OBJECT_NAME);

        checkSuperPolicyCmptTypeReferences(NEW_OBJECT_NAME);
    }

    public void testRenamePolicyCmptTypeWithInverseAssociation() throws CoreException {
        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setInverseAssociation(otherPolicyToPolicyAssociation.getName());
        association.setTarget(otherPolicyCmptType.getQualifiedName());
        association.setTargetRoleSingular("foo");
        association.setTargetRolePlural("foobar");
        otherPolicyToPolicyAssociation.setInverseAssociation(association.getName());

        performRenamePolicyCmptType(NEW_OBJECT_NAME);
    }

    private void performRenamePolicyCmptType(String newName) throws CoreException {
        performRenameRefactoring(policyCmptType, newName);

        checkIpsSourceFiles(POLICY_CMPT_TYPE_NAME, newName, policyCmptType.getIpsPackageFragment(), policyCmptType
                .getIpsPackageFragment(), IpsObjectType.POLICY_CMPT_TYPE);

        checkPolicyCmptTypeReferences(PACKAGE_NAME + "." + newName);
    }

    public void testRenameProductCmptType() throws CoreException {
        performRenameRefactoring(productCmptType, NEW_OBJECT_NAME);

        checkIpsSourceFiles(PRODUCT_CMPT_TYPE_NAME, NEW_OBJECT_NAME, productCmptType.getIpsPackageFragment(),
                productCmptType.getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT_TYPE);

        checkProductCmptTypeReferences(PACKAGE_NAME + "." + NEW_OBJECT_NAME);
    }

    public void testRenameSuperProductCmptType() throws CoreException {
        performRenameRefactoring(superProductCmptType, NEW_OBJECT_NAME);

        checkSuperProductCmptTypeReferences(NEW_OBJECT_NAME);
    }

    public void testRenameTestCaseType() throws CoreException {
        performRenameRefactoring(testCaseType, NEW_OBJECT_NAME);

        checkIpsSourceFiles(TEST_CASE_TYPE_NAME, NEW_OBJECT_NAME, testCaseType.getIpsPackageFragment(), testCaseType
                .getIpsPackageFragment(), IpsObjectType.TEST_CASE_TYPE);

        checkTestCaseTypeReferences(NEW_OBJECT_NAME);
    }

    public void testRenameEnumType() throws CoreException {
        performRenameRefactoring(enumType, NEW_OBJECT_NAME);

        checkIpsSourceFiles(ENUM_TYPE_NAME, NEW_OBJECT_NAME, enumType.getIpsPackageFragment(), enumType
                .getIpsPackageFragment(), IpsObjectType.ENUM_TYPE);

        checkEnumTypeReferences(NEW_OBJECT_NAME);
    }

    public void testRenameTableStructure() throws CoreException {
        performRenameRefactoring(tableStructure, NEW_OBJECT_NAME);

        checkIpsSourceFiles(TABLE_STRUCTURE_NAME, NEW_OBJECT_NAME, tableStructure.getIpsPackageFragment(),
                tableStructure.getIpsPackageFragment(), IpsObjectType.TABLE_STRUCTURE);

        checkTableStructureReferences(NEW_OBJECT_NAME);
    }

    public void testRenameBusinessFunction() throws CoreException {
        performRenameRefactoring(businessFunction, NEW_OBJECT_NAME);

        checkIpsSourceFiles(BUSINESS_FUNCTION_NAME, NEW_OBJECT_NAME, businessFunction.getIpsPackageFragment(),
                businessFunction.getIpsPackageFragment(), BusinessFunctionIpsObjectType.getInstance());

        checkBusinessFunctionReferences(NEW_OBJECT_NAME);
    }

    public void testRenameProductCmpt() throws CoreException {
        performRenameRefactoring(productCmpt, NEW_OBJECT_NAME);

        checkIpsSourceFiles(PRODUCT_NAME, NEW_OBJECT_NAME, productCmpt.getIpsPackageFragment(), productCmpt
                .getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT);

        checkProductCmptReferences(NEW_OBJECT_NAME);
    }

    public void testRenameTestCase() throws CoreException {
        performRenameRefactoring(testCase, NEW_OBJECT_NAME);

        checkIpsSourceFiles(TEST_CASE_NAME, NEW_OBJECT_NAME, testCase.getIpsPackageFragment(), testCase
                .getIpsPackageFragment(), IpsObjectType.TEST_CASE);

        checkTestCaseReferences(NEW_OBJECT_NAME);
    }

    public void testRenameEnumContent() throws CoreException {
        performRenameRefactoring(enumContent, NEW_OBJECT_NAME);

        checkIpsSourceFiles(ENUM_CONTENT_NAME, NEW_OBJECT_NAME, enumContent.getIpsPackageFragment(), enumContent
                .getIpsPackageFragment(), IpsObjectType.ENUM_CONTENT);

        checkEnumContentReferences(NEW_OBJECT_NAME);
    }

    public void testRenameTableContent() throws CoreException {
        performRenameRefactoring(tableContents, NEW_OBJECT_NAME);

        checkIpsSourceFiles(TABLE_CONTENTS_NAME, NEW_OBJECT_NAME, tableContents.getIpsPackageFragment(), tableContents
                .getIpsPackageFragment(), IpsObjectType.TABLE_CONTENTS);

        checkTableContentsReferences(NEW_OBJECT_NAME);
    }

    public void testRenameOnlyLetterCaseChanged() throws CoreException {
        String newName = POLICY_CMPT_TYPE_NAME.toLowerCase();
        performRenamePolicyCmptType(newName);
    }

    @Override
    protected ProcessorBasedRefactoring getRefactoring(IIpsElement ipsElement) {
        return ipsElement.getRenameRefactoring();
    }

}
