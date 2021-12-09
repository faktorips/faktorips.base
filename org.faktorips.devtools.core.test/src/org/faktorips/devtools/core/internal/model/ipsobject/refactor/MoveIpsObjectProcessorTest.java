/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.junit.Before;
import org.junit.Test;

public class MoveIpsObjectProcessorTest extends AbstractMoveRenameIpsObjectTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private static final String ORIGINAL_PACKAGE_NAME = "original";

    private IIpsPackageFragment targetIpsPackageFragment;

    private IIpsPackageFragment originalIpsPackageFragment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        originalIpsPackageFragment = fragmentRoot.createPackageFragment(ORIGINAL_PACKAGE_NAME, true, null);
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);
    }

    @Test
    public void testCheckFinalConditionsValid() throws CoreRuntimeException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");
        IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor(policyCmptType);
        ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);

        RefactoringStatus status = ipsMoveProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsFileAlreadyExists() throws CoreRuntimeException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");
        IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor(policyCmptType);
        ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        newProductCmptType(ipsProject, TARGET_PACKAGE_NAME + '.' + policyCmptType.getName());

        RefactoringStatus status = ipsMoveProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertTrue(status.hasError());
    }

    @Test
    public void testMovePolicyCmptType() throws CoreRuntimeException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");
        PolicyCmptTypeReferences policyCmptTypeReferences = new PolicyCmptTypeReferences(policyCmptType, false);

        policyCmptTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);

        checkIpsSourceFile(policyCmptType.getName(), policyCmptType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.POLICY_CMPT_TYPE);
        policyCmptTypeReferences.check(TARGET_PACKAGE_NAME + '.' + policyCmptType.getName());
    }

    @Test
    public void testMoveSuperPolicyCmptType() throws CoreRuntimeException {
        IPolicyCmptType superPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "SuperPolicyCmptType");
        superPolicyCmptType.setAbstract(true);
        SuperPolicyCmptTypeReferences superPolicyCmptTypeReferences = new SuperPolicyCmptTypeReferences(
                superPolicyCmptType);

        superPolicyCmptTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(superPolicyCmptType, targetIpsPackageFragment);

        superPolicyCmptTypeReferences.check(TARGET_PACKAGE_NAME + '.' + superPolicyCmptType.getName());
    }

    @Test
    public void testMoveProductCmptType() throws CoreRuntimeException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        ProductCmptTypeReferences productCmptTypeReferences = new ProductCmptTypeReferences(productCmptType);

        productCmptTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkIpsSourceFile(productCmptType.getName(), productCmptType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT_TYPE);
        productCmptTypeReferences.check(TARGET_PACKAGE_NAME + '.' + productCmptType.getName());
    }

    @Test
    public void testMoveSuperProductCmptType() throws CoreRuntimeException {
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.'
                + "SuperProduct");
        superProductCmptType.setAbstract(true);
        SuperProductCmptTypeReferences superProductCmptTypeReferences = new SuperProductCmptTypeReferences(
                superProductCmptType);

        superProductCmptTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(superProductCmptType, targetIpsPackageFragment);

        superProductCmptTypeReferences.check(TARGET_PACKAGE_NAME + '.' + superProductCmptType.getName());
    }

    @Test
    public void testMoveTestCaseType() throws CoreRuntimeException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TestCaseType");
        TestCaseTypeReferences testCaseTypeReferences = new TestCaseTypeReferences(testCaseType);

        testCaseTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(testCaseType, targetIpsPackageFragment);

        checkIpsSourceFile(testCaseType.getName(), testCaseType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TEST_CASE_TYPE);
        testCaseTypeReferences.check(TARGET_PACKAGE_NAME + '.' + testCaseType.getName());
    }

    @Test
    public void testMoveEnumType() throws CoreRuntimeException {
        IEnumType enumType = createEnumType(ORIGINAL_PACKAGE_NAME + '.' + "EnumType", null, "id", "name");
        enumType.newEnumLiteralNameAttribute();
        EnumTypeReferences enumTypeReferences = new EnumTypeReferences(enumType);

        enumTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(enumType, targetIpsPackageFragment);

        checkIpsSourceFile(enumType.getName(), enumType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.ENUM_TYPE);
        enumTypeReferences.check(TARGET_PACKAGE_NAME + '.' + enumType.getName());
    }

    @Test
    public void testMoveTableStructure() throws CoreRuntimeException {
        ITableStructure tableStructure = newTableStructure(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TableStructure");
        TableStructureReferences tableStructureReferences = new TableStructureReferences(tableStructure);

        tableStructureReferences.saveIpsSrcFiles();
        performMoveRefactoring(tableStructure, targetIpsPackageFragment);

        checkIpsSourceFile(tableStructure.getName(), tableStructure.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TABLE_STRUCTURE);
        tableStructureReferences.check(TARGET_PACKAGE_NAME + '.' + tableStructure.getName());
    }

    @Test
    public void testMoveProductCmpt() throws CoreRuntimeException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, ORIGINAL_PACKAGE_NAME + '.' + "ProductCmpt");
        ProductCmptReferences productCmptReferences = new ProductCmptReferences(productCmpt, productCmptType);

        productCmptReferences.saveIpsSrcFiles();
        performMoveRefactoring(productCmpt, targetIpsPackageFragment);

        checkIpsSourceFile(productCmpt.getName(), productCmpt.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT);
        String newProductCmptQualifiedName = TARGET_PACKAGE_NAME + '.' + productCmpt.getName();
        productCmptReferences.check(newProductCmptQualifiedName);
    }

    @Test
    public void testMoveTestCase() throws CoreRuntimeException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TestCaseType");
        ITestCase testCase = newTestCase(testCaseType, ORIGINAL_PACKAGE_NAME + '.' + "TestCase");

        saveIpsSrcFile(testCaseType);
        saveIpsSrcFile(testCase);
        performMoveRefactoring(testCase, targetIpsPackageFragment);

        checkIpsSourceFile(testCase.getName(), testCase.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TEST_CASE);
    }

    @Test
    public void testMoveEnumContent() throws CoreRuntimeException {
        IEnumType enumType = createEnumType(ORIGINAL_PACKAGE_NAME + '.' + "EnumType", null, "id", "name");
        enumType.setExtensible(true);
        enumType.setEnumContentName(ORIGINAL_PACKAGE_NAME + '.' + "EnumContent");
        IEnumContent enumContent = newEnumContent(enumType, ORIGINAL_PACKAGE_NAME + '.' + "EnumContent");

        saveIpsSrcFile(enumType);
        saveIpsSrcFile(enumContent);
        performMoveRefactoring(enumContent, targetIpsPackageFragment);

        checkIpsSourceFile(enumContent.getName(), enumContent.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.ENUM_CONTENT);
    }

    @Test
    public void testMoveTableContents() throws CoreRuntimeException {
        ITableStructure tableStructure = newTableStructure(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TableStructure");
        ITableContents tableContents = newTableContents(tableStructure, ORIGINAL_PACKAGE_NAME + '.' + "TableContents");

        saveIpsSrcFile(tableStructure);
        saveIpsSrcFile(tableContents);
        performMoveRefactoring(tableContents, targetIpsPackageFragment);

        checkIpsSourceFile(tableContents.getName(), tableContents.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TABLE_CONTENTS);
    }

}
