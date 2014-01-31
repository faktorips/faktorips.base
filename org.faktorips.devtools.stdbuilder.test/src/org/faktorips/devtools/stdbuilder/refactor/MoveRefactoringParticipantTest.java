/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
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
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, ORIGINAL_PACKAGE_NAME
                + '.' + "Policy");

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
        IBusinessFunction businessFunction = createBusinessFunction(ORIGINAL_PACKAGE_NAME + '.' + "BusinessFunction");

        saveIpsSrcFile(businessFunction);
        performFullBuild(ipsProject);

        performMoveRefactoring(businessFunction, targetIpsPackageFragment);

        checkJavaSourceFilesBusinessFunction(ORIGINAL_PACKAGE_NAME, "BusinessFunction", TARGET_PACKAGE_NAME,
                "BusinessFunction");
    }

}
