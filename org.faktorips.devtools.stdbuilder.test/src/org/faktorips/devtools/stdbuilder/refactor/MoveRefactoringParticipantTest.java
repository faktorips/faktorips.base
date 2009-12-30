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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * Tests the various Faktor-IPS "Move" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class MoveRefactoringParticipantTest extends RefactoringParticipantTest {

    private static final String TARGET_PACKAGE_NAME = "level.targetipspackage";

    private IIpsPackageFragment targetIpsPackageFragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = policyCmptType.getIpsPackageFragment().getRoot();
        targetIpsPackageFragment = fragmentRoot.createPackageFragment(TARGET_PACKAGE_NAME, true, null);
    }

    public void testMovePolicyCmptType() throws CoreException {
        performFullBuild();

        performMoveRefactoring(policyCmptType, targetIpsPackageFragment);

        assertFalse(policyClass.exists());
        assertFalse(policyInterface.exists());

        // Obtain the moved Java elements.
        policyClass = getJavaType(TARGET_PACKAGE_NAME, POLICY_NAME, true);
        policyInterface = getJavaType(TARGET_PACKAGE_NAME, POLICY_NAME, false);

        assertTrue(policyClass.exists());
        assertTrue(policyInterface.exists());
    }

    public void testMoveProductCmptType() throws CoreException {
        performFullBuild();

        performMoveRefactoring(productCmptType, targetIpsPackageFragment);

        assertFalse(productClass.exists());
        assertFalse(productInterface.exists());
        assertFalse(productGenClass.exists());
        assertFalse(productGenInterface.exists());

        // Obtain the moved Java elements.
        productClass = getJavaType(TARGET_PACKAGE_NAME, PRODUCT_NAME, true);
        productInterface = getJavaType(TARGET_PACKAGE_NAME, PRODUCT_NAME, false);
        productGenClass = getJavaType(TARGET_PACKAGE_NAME, PRODUCT_NAME + "Gen", true);
        productGenInterface = getJavaType(TARGET_PACKAGE_NAME, PRODUCT_NAME + "Gen", false);

        assertTrue(productClass.exists());
        assertTrue(productInterface.exists());
        assertTrue(productGenClass.exists());
        assertTrue(productGenInterface.exists());
    }

}
