/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
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
    public void testCheckFinalConditionsValid() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");
        IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor(policyCmptType);
        ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);

        RefactoringStatus status = ipsMoveProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");
        IpsMoveProcessor ipsMoveProcessor = new MoveIpsObjectProcessor(policyCmptType);
        ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        newProductCmptType(ipsProject, TARGET_PACKAGE_NAME + '.' + policyCmptType.getName());

        RefactoringStatus status = ipsMoveProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testMovePolicyCmptType() throws CoreException {
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
    public void testMoveSuperPolicyCmptType() throws CoreException {
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
    public void testMoveProductCmptType() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        ProductCmptTypeReferences productCmptTypeReferences = new ProductCmptTypeReferences(productCmptType);

        productCmptTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkIpsSourceFile(productCmptType.getName(), productCmptType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT_TYPE);
        productCmptTypeReferences.check(TARGET_PACKAGE_NAME + '.' + productCmptType.getName());
    }

    @Test
    public void testMoveSuperProductCmptType() throws CoreException {
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
    public void testMoveTestCaseType() throws CoreException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TestCaseType");
        TestCaseTypeReferences testCaseTypeReferences = new TestCaseTypeReferences(testCaseType);

        testCaseTypeReferences.saveIpsSrcFiles();
        performMoveRefactoring(testCaseType, targetIpsPackageFragment);

        checkIpsSourceFile(testCaseType.getName(), testCaseType.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TEST_CASE_TYPE);
        testCaseTypeReferences.check(TARGET_PACKAGE_NAME + '.' + testCaseType.getName());
    }

    @Test
    public void testMoveEnumType() throws CoreException {
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
    public void testMoveTableStructure() throws CoreException {
        ITableStructure tableStructure = newTableStructure(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TableStructure");
        TableStructureReferences tableStructureReferences = new TableStructureReferences(tableStructure);

        tableStructureReferences.saveIpsSrcFiles();
        performMoveRefactoring(tableStructure, targetIpsPackageFragment);

        checkIpsSourceFile(tableStructure.getName(), tableStructure.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TABLE_STRUCTURE);
        tableStructureReferences.check(TARGET_PACKAGE_NAME + '.' + tableStructure.getName());
    }

    @Test
    public void testMoveBusinessFunction() throws CoreException {
        IBusinessFunction businessFunction = createBusinessFunction(ORIGINAL_PACKAGE_NAME + '.' + "BusinessFunction");

        saveIpsSrcFile(businessFunction);
        performMoveRefactoring(businessFunction, targetIpsPackageFragment);

        checkIpsSourceFile(businessFunction.getName(), businessFunction.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, BusinessFunctionIpsObjectType.getInstance());
    }

    @Test
    public void testMoveProductCmpt() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, ORIGINAL_PACKAGE_NAME + '.' + "ProductCmpt");
        ProductCmptReferences productCmptReferences = new ProductCmptReferences(productCmpt, productCmptType);

        productCmptReferences.saveIpsSrcFiles();
        performMoveRefactoring(productCmpt, targetIpsPackageFragment, false);

        checkIpsSourceFile(productCmpt.getName(), productCmpt.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT);
        String newProductCmptQualifiedName = TARGET_PACKAGE_NAME + '.' + productCmpt.getName();
        productCmptReferences.check(newProductCmptQualifiedName);
        assertEquals(productCmpt.getName(), ipsProject.findProductCmpt(newProductCmptQualifiedName).getRuntimeId());
    }

    @Test
    public void testMoveProductCmptAdaptRuntimeId() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, ORIGINAL_PACKAGE_NAME + '.' + "ProductCmpt");
        ProductCmptReferences productCmptReferences = new ProductCmptReferences(productCmpt, productCmptType);

        productCmptReferences.saveIpsSrcFiles();
        performMoveRefactoring(productCmpt, targetIpsPackageFragment, true);

        checkIpsSourceFile(productCmpt.getName(), productCmpt.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.PRODUCT_CMPT);
        String newProductCmptQualifiedName = TARGET_PACKAGE_NAME + '.' + productCmpt.getName();
        productCmptReferences.check(newProductCmptQualifiedName);
        assertEquals(newProductCmptQualifiedName, ipsProject.findProductCmpt(newProductCmptQualifiedName)
                .getRuntimeId());
    }

    @Test
    public void testMoveTestCase() throws CoreException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TestCaseType");
        ITestCase testCase = newTestCase(testCaseType, ORIGINAL_PACKAGE_NAME + '.' + "TestCase");

        saveIpsSrcFile(testCaseType);
        saveIpsSrcFile(testCase);
        performMoveRefactoring(testCase, targetIpsPackageFragment);

        checkIpsSourceFile(testCase.getName(), testCase.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TEST_CASE);
    }

    @Test
    public void testMoveEnumContent() throws CoreException {
        IEnumType enumType = createEnumType(ORIGINAL_PACKAGE_NAME + '.' + "EnumType", null, "id", "name");
        enumType.setContainingValues(false);
        enumType.setEnumContentName(ORIGINAL_PACKAGE_NAME + '.' + "EnumContent");
        IEnumContent enumContent = newEnumContent(enumType, ORIGINAL_PACKAGE_NAME + '.' + "EnumContent");

        saveIpsSrcFile(enumType);
        saveIpsSrcFile(enumContent);
        performMoveRefactoring(enumContent, targetIpsPackageFragment);

        checkIpsSourceFile(enumContent.getName(), enumContent.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.ENUM_CONTENT);
    }

    @Test
    public void testMoveTableContents() throws CoreException {
        ITableStructure tableStructure = newTableStructure(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "TableStructure");
        ITableContents tableContents = newTableContents(tableStructure, ORIGINAL_PACKAGE_NAME + '.' + "TableContents");

        saveIpsSrcFile(tableStructure);
        saveIpsSrcFile(tableContents);
        performMoveRefactoring(tableContents, targetIpsPackageFragment);

        checkIpsSourceFile(tableContents.getName(), tableContents.getName(), originalIpsPackageFragment,
                targetIpsPackageFragment, IpsObjectType.TABLE_CONTENTS);
    }

}
