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

        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkJavaSourceFilesProductCmptType(ORIGINAL_PACKAGE_NAME, "Product", TARGET_PACKAGE_NAME, "Product");
    }

    @Test
    public void testMoveEnumType() throws CoreException {
        IEnumType enumType = createEnumType(ORIGINAL_PACKAGE_NAME + '.' + "EnumType", null, "id", "name");
        enumType.setContainingValues(false);
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
