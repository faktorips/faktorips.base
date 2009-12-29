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

    private IIpsPackageFragment targetPackageFragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsPackageFragmentRoot fragmentRoot = policyCmptType.getIpsPackageFragment().getRoot();
        targetPackageFragment = fragmentRoot.createPackageFragment("targetPackageLevel1", true, null);
        targetPackageFragment = targetPackageFragment.createSubPackage("targetPackageLevel2", true, null);
    }

    public void testMovePolicyCmptType() throws CoreException {
        performFullBuild();
    }

    public void testMoveProductCmptType() {

    }

}
