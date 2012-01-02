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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.junit.Test;

public class RenameIpsObjectProcessorTest extends AbstractMoveRenameIpsObjectTest {

    private static final String NEW_NAME = "NewName";

    @Test
    public void testCheckFinalConditionsValid() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        IpsRenameProcessor ipsRenameProcessor = new RenameIpsObjectProcessor(policyCmptType);
        ipsRenameProcessor.setNewName("test");

        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        IpsRenameProcessor ipsRenameProcessor = new RenameIpsObjectProcessor(policyCmptType);
        ipsRenameProcessor.setNewName(policyCmptType.getName());

        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckFinalConditionsInvalidTypeName() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        IpsRenameProcessor ipsRenameProcessor = new RenameIpsObjectProcessor(policyCmptType);
        ipsRenameProcessor.setNewName("$§§  $");

        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testRenamePolicyCmptType() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        performTestRenamePolicyCmptType(policyCmptType, NEW_NAME, false);
    }

    @Test
    public void testRenameSuperPolicyCmptType() throws CoreException {
        IPolicyCmptType superPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SuperPolicy");
        superPolicyCmptType.setAbstract(true);
        SuperPolicyCmptTypeReferences superPolicyCmptTypeReferences = new SuperPolicyCmptTypeReferences(
                superPolicyCmptType);

        superPolicyCmptTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(superPolicyCmptType, NEW_NAME);

        superPolicyCmptTypeReferences.check(NEW_NAME);
    }

    @Test
    public void testRenamePolicyCmptTypeWithInverseAssociation() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        performTestRenamePolicyCmptType(policyCmptType, NEW_NAME, true);
    }

    @Test
    public void testRenameProductCmptType() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
        ProductCmptTypeReferences productCmptTypeReferences = new ProductCmptTypeReferences(productCmptType);

        productCmptTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(productCmptType, NEW_NAME);

        checkIpsSourceFile(productCmptType.getName(), NEW_NAME, productCmptType.getIpsPackageFragment(),
                productCmptType.getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT_TYPE);
        productCmptTypeReferences.check(NEW_NAME);
    }

    @Test
    public void testRenameSuperProductCmptType() throws CoreException {
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        superProductCmptType.setAbstract(true);
        SuperProductCmptTypeReferences superProductCmptTypeReferences = new SuperProductCmptTypeReferences(
                superProductCmptType);

        superProductCmptTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(superProductCmptType, NEW_NAME);

        superProductCmptTypeReferences.check(NEW_NAME);
    }

    @Test
    public void testRenameTestCaseType() throws CoreException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        TestCaseTypeReferences testCaseTypeReferences = new TestCaseTypeReferences(testCaseType);

        testCaseTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(testCaseType, NEW_NAME);

        checkIpsSourceFile(testCaseType.getName(), NEW_NAME, testCaseType.getIpsPackageFragment(),
                testCaseType.getIpsPackageFragment(), IpsObjectType.TEST_CASE_TYPE);
        testCaseTypeReferences.check(NEW_NAME);
    }

    @Test
    public void testRenameEnumType() throws CoreException {
        IEnumType enumType = createEnumType("EnumType", null, "id", "name");
        enumType.newEnumLiteralNameAttribute();
        EnumTypeReferences enumTypeReferences = new EnumTypeReferences(enumType);

        enumTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(enumType, NEW_NAME);

        checkIpsSourceFile(enumType.getName(), NEW_NAME, enumType.getIpsPackageFragment(),
                enumType.getIpsPackageFragment(), IpsObjectType.ENUM_TYPE);
        enumTypeReferences.check(NEW_NAME);
    }

    @Test
    public void testRenameTableStructure() throws CoreException {
        ITableStructure tableStructure = newTableStructure(ipsProject, "TableStructure");
        TableStructureReferences tableStructureReferences = new TableStructureReferences(tableStructure);

        tableStructureReferences.saveIpsSrcFiles();
        performRenameRefactoring(tableStructure, NEW_NAME);

        checkIpsSourceFile(tableStructure.getName(), NEW_NAME, tableStructure.getIpsPackageFragment(),
                tableStructure.getIpsPackageFragment(), IpsObjectType.TABLE_STRUCTURE);
        tableStructureReferences.check(NEW_NAME);
    }

    @Test
    public void testRenameBusinessFunction() throws CoreException {
        IBusinessFunction businessFunction = createBusinessFunction("BusinessFunction");

        saveIpsSrcFile(businessFunction);
        performRenameRefactoring(businessFunction, NEW_NAME);

        checkIpsSourceFile(businessFunction.getName(), NEW_NAME, businessFunction.getIpsPackageFragment(),
                businessFunction.getIpsPackageFragment(), BusinessFunctionIpsObjectType.getInstance());
    }

    @Test
    public void testRenameProductCmpt() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        ProductCmptReferences productCmptReferences = new ProductCmptReferences(productCmpt, productCmptType);

        productCmptReferences.saveIpsSrcFiles();
        performRenameRefactoring(productCmpt, NEW_NAME, false);

        checkIpsSourceFile(productCmpt.getName(), NEW_NAME, productCmpt.getIpsPackageFragment(),
                productCmpt.getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT);
        productCmptReferences.check(NEW_NAME);
        assertEquals(productCmpt.getName(), ipsProject.findProductCmpt(NEW_NAME).getRuntimeId());
    }

    @Test
    public void testRenameProductCmptAdaptRuntimeId() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        ProductCmptReferences productCmptReferences = new ProductCmptReferences(productCmpt, productCmptType);

        productCmptReferences.saveIpsSrcFiles();
        performRenameRefactoring(productCmpt, NEW_NAME, true);

        checkIpsSourceFile(productCmpt.getName(), NEW_NAME, productCmpt.getIpsPackageFragment(),
                productCmpt.getIpsPackageFragment(), IpsObjectType.PRODUCT_CMPT);
        productCmptReferences.check(NEW_NAME);
        assertEquals(NEW_NAME, ipsProject.findProductCmpt(NEW_NAME).getRuntimeId());
    }

    @Test
    public void testRenameTestCase() throws CoreException {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        ITestCase testCase = newTestCase(testCaseType, "TestCase");

        saveIpsSrcFile(testCaseType);
        saveIpsSrcFile(testCase);
        performRenameRefactoring(testCase, NEW_NAME);

        checkIpsSourceFile(testCase.getName(), NEW_NAME, testCase.getIpsPackageFragment(),
                testCase.getIpsPackageFragment(), IpsObjectType.TEST_CASE);
    }

    @Test
    public void testRenameEnumContent() throws CoreException {
        IEnumType enumType = createEnumType("EnumType", null, "id", "name");
        enumType.setContainingValues(false);
        enumType.setEnumContentName("EnumContent");
        IEnumContent enumContent = newEnumContent(enumType, "EnumContent");

        saveIpsSrcFile(enumType);
        saveIpsSrcFile(enumContent);
        performRenameRefactoring(enumContent, NEW_NAME);

        checkIpsSourceFile(enumContent.getName(), NEW_NAME, enumContent.getIpsPackageFragment(),
                enumContent.getIpsPackageFragment(), IpsObjectType.ENUM_CONTENT);
    }

    @Test
    public void testRenameTableContent() throws CoreException {
        ITableStructure tableStructure = newTableStructure(ipsProject, "TableStructure");
        ITableContents tableContents = newTableContents(tableStructure, "TableContents");

        saveIpsSrcFile(tableStructure);
        saveIpsSrcFile(tableContents);
        performRenameRefactoring(tableContents, NEW_NAME);

        checkIpsSourceFile(tableContents.getName(), NEW_NAME, tableContents.getIpsPackageFragment(),
                tableContents.getIpsPackageFragment(), IpsObjectType.TABLE_CONTENTS);
    }

    @Test
    public void testRenameOnlyLetterCaseChanged() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");

        String newName = "Policy".toLowerCase();
        performTestRenamePolicyCmptType(policyCmptType, newName, false);
        // Test successful if no exception occurs
    }

    private void performTestRenamePolicyCmptType(IPolicyCmptType policyCmptType,
            String newName,
            boolean createInverseAssociation) throws CoreException {

        PolicyCmptTypeReferences policyCmptTypeReferences = new PolicyCmptTypeReferences(policyCmptType,
                createInverseAssociation);

        policyCmptTypeReferences.saveIpsSrcFiles();
        performRenameRefactoring(policyCmptType, newName);

        checkIpsSourceFile(policyCmptType.getName(), newName, policyCmptType.getIpsPackageFragment(),
                policyCmptType.getIpsPackageFragment(), IpsObjectType.POLICY_CMPT_TYPE);
        policyCmptTypeReferences.check(newName);
    }

    @Test
    public void testRenameProductCmptThatReferencesItself() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "MyProductType");
        IProductCmptTypeAssociation toSelfAssociation = productCmptType.newProductCmptTypeAssociation();
        toSelfAssociation.setTarget(productCmptType.getQualifiedName());
        toSelfAssociation.setTargetRoleSingular("SelfType");
        toSelfAssociation.setTargetRolePlural("SelfTypes");

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "MyProductCmpt");
        IProductCmptLink toSelfLink = productCmpt.getProductCmptGeneration(0).newLink(toSelfAssociation);
        toSelfLink.setTarget("MyProductCmpt");
        toSelfLink.setMinCardinality(0);
        toSelfLink.setMaxCardinality(Integer.MAX_VALUE);

        saveIpsSrcFile(productCmptType);
        saveIpsSrcFile(productCmpt);
        performRenameRefactoring(productCmpt, "MyProductCmpt2");

        assertNull(ipsProject.findProductCmpt("MyProductCmpt"));
        assertNotNull(ipsProject.findProductCmpt("MyProductCmpt2"));
        assertTrue(ipsProject.findProductCmpt("MyProductCmpt2").isValid(ipsProject));
    }

}
