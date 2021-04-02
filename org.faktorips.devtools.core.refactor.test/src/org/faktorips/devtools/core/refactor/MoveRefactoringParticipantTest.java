/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.faktorips.abstracttest.matcher.IpsElementNamesMatcher.containsInOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the various Faktor-IPS "Move" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class MoveRefactoringParticipantTest extends RefactoringParticipantTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private static final String ORIGINAL_PACKAGE_NAME = "original";

    private IIpsPackageFragment targetIpsPackageFragment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        fragmentRoot.createPackageFragment(ORIGINAL_PACKAGE_NAME, true, null);
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);
    }

    @Test
    public void testMovePolicyCmptType() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject,
                ORIGINAL_PACKAGE_NAME + '.' + "Policy");

        saveIpsSrcFile(policyCmptType);
        performFullBuild(ipsProject);

        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);

        checkJavaSourceFilesPolicyCmptType(ORIGINAL_PACKAGE_NAME, "Policy", TARGET_PACKAGE_NAME, "Policy");
    }

    @Test
    public void testMoveProductCmptType() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        productCmptType.setConfigurationForPolicyCmptType(false);

        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkJavaSourceFilesProductCmptType(ORIGINAL_PACKAGE_NAME, "Product", TARGET_PACKAGE_NAME, "Product");
    }

    @Test
    public void testMoveProductCmpt() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME + '.' + "Product");
        saveIpsSrcFile(productCmptType);
        productCmptType.setConfigurationForPolicyCmptType(false);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, ORIGINAL_PACKAGE_NAME + '.' + "Prod");
        ProductCmpt productCmpt2 = newProductCmpt(productCmptType, ORIGINAL_PACKAGE_NAME + '.' + "Prod2");
        IpsPackageFragment originalIpsPackageFragment = (IpsPackageFragment)productCmpt.getIpsPackageFragment();
        originalIpsPackageFragment.setChildOrderComparator(
                new DefinedOrderComparator(productCmpt.getIpsSrcFile(), productCmpt2.getIpsSrcFile()));
        saveIpsSrcFile(productCmpt);
        performFullBuild(ipsProject);

        performMoveRefactoring(productCmpt, targetIpsPackageFragment);

        assertNull(ipsProject.findProductCmpt(ORIGINAL_PACKAGE_NAME + '.' + "Prod"));
        assertNotNull(ipsProject.findProductCmpt(TARGET_PACKAGE_NAME + '.' + "Prod"));
        IIpsElement[] elements = ((DefinedOrderComparator)originalIpsPackageFragment.getChildOrderComparator())
                .getElements();
        // still there, to allow undo
        assertThat(Arrays.asList(elements),
                containsInOrder(ORIGINAL_PACKAGE_NAME + "." + "Prod", ORIGINAL_PACKAGE_NAME + "." + "Prod2"));
    }

    @Test
    public void testMoveEnumType() throws CoreException {
        IEnumType enumType = createEnumType(ORIGINAL_PACKAGE_NAME + '.' + "EnumType", null, "id", "name");
        enumType.newEnumLiteralNameAttribute();
        enumType.setExtensible(true);
        enumType.setEnumContentName("EnumContent");

        saveIpsSrcFile(enumType);
        performFullBuild(ipsProject);

        performMoveRefactoring(enumType, targetIpsPackageFragment);

        checkJavaSourceFilesEnumType(ORIGINAL_PACKAGE_NAME, "EnumType", TARGET_PACKAGE_NAME, "EnumType");
    }

    @Test
    public void testMoveTableStructure() throws CoreException {
        ITableStructure tableStructure = createTableStructure(ORIGINAL_PACKAGE_NAME + '.' + "TableStructure");

        saveIpsSrcFile(tableStructure);
        performFullBuild(ipsProject);

        performMoveRefactoring(tableStructure, targetIpsPackageFragment);

        checkJavaSourceFilesTableStructure(ORIGINAL_PACKAGE_NAME, "TableStructure", TARGET_PACKAGE_NAME,
                "TableStructure");
    }

    @Test
    public void testMoveTestCaseType() throws CoreException {
        ITestCaseType testCaseType = createTestCaseType(ORIGINAL_PACKAGE_NAME + '.' + "TestCaseType");

        saveIpsSrcFile(testCaseType);
        performFullBuild(ipsProject);

        performMoveRefactoring(testCaseType, targetIpsPackageFragment);

        checkJavaSourceFilesTestCaseType(ORIGINAL_PACKAGE_NAME, "TestCaseType", TARGET_PACKAGE_NAME, "TestCaseType");
    }

    @Test
    public void testMoveBusinessFunction() throws CoreException {
        @SuppressWarnings("deprecation")
        org.faktorips.devtools.model.bf.IBusinessFunction businessFunction = createBusinessFunction(
                ORIGINAL_PACKAGE_NAME + '.' + "BusinessFunction");

        saveIpsSrcFile(businessFunction);
        performFullBuild(ipsProject);

        performMoveRefactoring(businessFunction, targetIpsPackageFragment);

        checkJavaSourceFilesBusinessFunction(ORIGINAL_PACKAGE_NAME, "BusinessFunction", TARGET_PACKAGE_NAME,
                "BusinessFunction");
    }

}
