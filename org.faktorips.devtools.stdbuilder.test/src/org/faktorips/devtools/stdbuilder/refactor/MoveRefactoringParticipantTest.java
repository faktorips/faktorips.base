/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the various Faktor-IPS "Move" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class MoveRefactoringParticipantTest extends RefactoringParticipantTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private IIpsPackageFragment targetIpsPackageFragment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = policyCmptType.getIpsPackageFragment().getRoot();
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);

        performFullBuild();
    }

    @Test
    public void testMovePolicyCmptType() throws CoreException {
        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);

        checkJavaSourceFilesPolicyCmptType(TARGET_PACKAGE_NAME, POLICY_CMPT_TYPE_NAME);
    }

    @Test
    public void testMoveProductCmptType() throws CoreException {
        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        checkJavaSourceFilesProductCmptType(TARGET_PACKAGE_NAME, PRODUCT_CMPT_TYPE_NAME);
    }

    @Test
    public void testMoveEnumType() throws CoreException {
        performMoveRefactoring(enumType, targetIpsPackageFragment);

        checkJavaSourceFilesEnumType(TARGET_PACKAGE_NAME, ENUM_TYPE_NAME);
    }

    @Test
    public void testMoveTableStructure() throws CoreException {
        performMoveRefactoring(tableStructure, targetIpsPackageFragment);

        checkJavaSourceFilesTableStructure(TARGET_PACKAGE_NAME, TABLE_STRUCTURE_NAME);
    }

    @Test
    public void testMoveTestCaseType() throws CoreException {
        performMoveRefactoring(testCaseType, targetIpsPackageFragment);

        checkJavaSourceFilesTestCaseType(TARGET_PACKAGE_NAME, TEST_CASE_TYPE_NAME);
    }

    @Test
    public void testMoveBusinessFunction() throws CoreException {
        performMoveRefactoring(businessFunction, targetIpsPackageFragment);

        checkJavaSourceFilesBusinessFunction(TARGET_PACKAGE_NAME, BUSINESS_FUNCTION_NAME);
    }

}
