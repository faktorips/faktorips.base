/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Markus Blum
 */
public class IpsPackageNameComparatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    private IpsPackageNameComparator comparator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    public void testCompareBasicDefaultSortOrder() throws CoreException {
        comparator = new IpsPackageNameComparator(true);

        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$

        // 2. A < B <=> B > A
        assertTrue(comparator.compare(packA, packB) < 0);
        assertTrue(comparator.compare(packB, packA) > 0);

        // 3. A < B & B < C => A < C
        assertTrue(comparator.compare(packA, packB) < 0);
        assertTrue(comparator.compare(packB, packA) > 0);
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);
        assertTrue(comparator.compare(packA, packC) < 0);
        assertTrue(comparator.compare(packC, packA) > 0);

        // 4. equals
        assertEquals(0, comparator.compare(packA, packA));

        // 5. Object equals
        assertTrue(packA.equals(packA));

        // 6. NullPointerExeption
        boolean caughtException = false;
        try {
            comparator.compare(packA, null);
        } catch (NullPointerException e) {
            caughtException = true;
        }
        assertTrue(caughtException);

        caughtException = false;
        try {
            comparator.compare(null, packA);
        } catch (NullPointerException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }

    public void testCompareExtendedDefaultSortOrder() throws CoreException {
        comparator = new IpsPackageNameComparator(true);

        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$
        IIpsPackageFragment packX = ipsRoot.createPackageFragment("a.a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packY = ipsRoot.createPackageFragment("a.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packZ = ipsRoot.createPackageFragment("c.a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packW = ipsRoot.createPackageFragment("c.b", true, null); //$NON-NLS-1$

        // check compareTo contracts:

        // 2. A < D <=> D > A
        assertTrue(comparator.compare(packX, packY) < 0);
        assertTrue(comparator.compare(packY, packX) > 0);

        assertTrue(comparator.compare(packB, packZ) < 0);
        assertTrue(comparator.compare(packZ, packB) > 0);

        // 3. A < B; B < C => A < C
        assertTrue(comparator.compare(packX, packB) < 0);
        assertTrue(comparator.compare(packB, packX) > 0);
        assertTrue(comparator.compare(packB, packW) < 0);
        assertTrue(comparator.compare(packW, packB) > 0);
        assertTrue(comparator.compare(packA, packW) < 0);
        assertTrue(comparator.compare(packW, packA) > 0);

        // 4. equals
        assertEquals(0, comparator.compare(packA, packX));

        // 5. Object equals
        assertFalse(packA.equals(packX));
    }

    public void testCompareBasicSortOrder() throws CoreException, IOException {
        comparator = new IpsPackageNameComparator(false);

        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$

        ArrayList<String> orderList = new ArrayList<String>(2);

        orderList.add("b"); //$NON-NLS-1$
        orderList.add("c"); //$NON-NLS-1$
        orderList.add("a"); //$NON-NLS-1$

        createPackageOrderFile((IFolder)ipsRoot.getCorrespondingResource(), orderList);
        orderList.clear();

        // 2. A < B <=> B > A
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);

        // 3. A < B & B < C => A < C
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);
        assertTrue(comparator.compare(packC, packA) < 0);
        assertTrue(comparator.compare(packA, packC) > 0);
        assertTrue(comparator.compare(packB, packA) < 0);
        assertTrue(comparator.compare(packA, packB) > 0);

        // 4. equals
        assertEquals(0, comparator.compare(packA, packA));

        // 5. Object equals
        assertTrue(packA.equals(packA));

        // 6. NullPointerExeption
        boolean caughtException = false;
        try {
            comparator.compare(packA, null);
        } catch (NullPointerException e) {
            caughtException = true;
        }
        assertTrue(caughtException);

        caughtException = false;
        try {
            comparator.compare(null, packA);
        } catch (NullPointerException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }

    public void testCompareExtendedSortOrder() throws CoreException, IOException {

        comparator = new IpsPackageNameComparator(false);

        ArrayList<String> orderList = new ArrayList<String>(2);

        orderList.add("d"); //$NON-NLS-1$
        orderList.add("a"); //$NON-NLS-1$

        createPackageOrderFile((IFolder)ipsRoot.getCorrespondingResource(), orderList);
        orderList.clear();

        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null); //$NON-NLS-1$

        orderList.add("c"); //$NON-NLS-1$
        orderList.add("b"); //$NON-NLS-1$
        orderList.add("m"); //$NON-NLS-1$

        createPackageOrderFile((IFolder)packA.getCorrespondingResource(), orderList);
        orderList.clear();

        IIpsPackageFragment packB = ipsRoot.createPackageFragment("a.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("a.c", true, null); //$NON-NLS-1$
        IIpsPackageFragment packM = ipsRoot.createPackageFragment("a.b.m", true, null); //$NON-NLS-1$

        orderList.add("f"); //$NON-NLS-1$
        orderList.add("e"); //$NON-NLS-1$

        createPackageOrderFile((IFolder)packB.getCorrespondingResource(), orderList);
        orderList.clear();

        IIpsPackageFragment packE = ipsRoot.createPackageFragment("a.b.e", true, null); //$NON-NLS-1$
        IIpsPackageFragment packF = ipsRoot.createPackageFragment("a.b.f", true, null); //$NON-NLS-1$

        orderList.add("h"); //$NON-NLS-1$
        orderList.add("g"); //$NON-NLS-1$

        createPackageOrderFile((IFolder)packC.getCorrespondingResource(), orderList);
        orderList.clear();

        IIpsPackageFragment packG = ipsRoot.createPackageFragment("a.c.g", true, null); //$NON-NLS-1$
        IIpsPackageFragment packH = ipsRoot.createPackageFragment("a.c.h", true, null); //$NON-NLS-1$

        assertTrue(comparator.compare(packA, packD) > 0);
        assertTrue(comparator.compare(packD, packA) < 0);
        assertTrue(comparator.compare(packA, packB) == 0);

        assertTrue(comparator.compare(packD, packC) < 0);
        assertTrue(comparator.compare(packC, packD) > 0);
        assertTrue(comparator.compare(packD, packB) < 0);
        assertTrue(comparator.compare(packB, packD) > 0);

        assertTrue(comparator.compare(packB, packC) > 0);
        assertTrue(comparator.compare(packC, packB) < 0);

        assertTrue(comparator.compare(packB, packE) == 0);
        assertTrue(comparator.compare(packC, packF) < 0);
        assertTrue(comparator.compare(packM, packF) > 0);
        assertTrue(comparator.compare(packF, packC) > 0);
        assertTrue(comparator.compare(packF, packM) < 0);

        assertTrue(comparator.compare(packE, packH) > 0);
        assertTrue(comparator.compare(packH, packE) < 0);

        assertTrue(comparator.compare(packG, packH) > 0);
        assertTrue(comparator.compare(packH, packG) < 0);

        // test not listed package
        IIpsPackageFragment packEnd1 = ipsRoot.createPackageFragment("a.x", true, null); //$NON-NLS-1$
        IIpsPackageFragment packEnd2 = ipsRoot.createPackageFragment("b.y", true, null); //$NON-NLS-1$

        assertTrue(comparator.compare(packA, packEnd1) == 0);
        assertTrue(comparator.compare(packEnd2, packA) > 0);
        assertTrue(comparator.compare(packA, packEnd2) < 0);
        assertTrue(comparator.compare(packF, packEnd2) < 0);
        assertTrue(comparator.compare(packEnd2, packF) > 0);
    }

}
