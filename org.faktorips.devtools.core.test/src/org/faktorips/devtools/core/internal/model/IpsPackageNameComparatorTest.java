/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 *
 * @author Markus Blum
 */
public class IpsPackageNameComparatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    private IpsPackageNameComparator comparator;

    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    protected void tearDown() throws Exception {
        super.tearDown();

    }

    public void testCompareBasic() throws CoreException, IOException {

        comparator = new IpsPackageNameComparator();

        ArrayList orderList = new ArrayList(2);

        orderList.add("d"); //$NON-NLS-1$
        orderList.add("a"); //$NON-NLS-1$

        createPackageOrderFile((IFolder) ipsRoot.getCorrespondingResource() , orderList);
        orderList.clear();

        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null); //$NON-NLS-1$

        orderList.add("c"); //$NON-NLS-1$
        orderList.add("b"); //$NON-NLS-1$
        orderList.add("m"); //$NON-NLS-1$

        createPackageOrderFile((IFolder) packA.getCorrespondingResource() , orderList);
        orderList.clear();

        IIpsPackageFragment packB = ipsRoot.createPackageFragment("a.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("a.c", true, null); //$NON-NLS-1$
        IIpsPackageFragment packM = ipsRoot.createPackageFragment("a.b.m", true, null); //$NON-NLS-1$

        orderList.add("f"); //$NON-NLS-1$
        orderList.add("e"); //$NON-NLS-1$

        IIpsPackageFragment packE = ipsRoot.createPackageFragment("a.b.e", true, null); //$NON-NLS-1$
        IIpsPackageFragment packF = ipsRoot.createPackageFragment("a.b.f", true, null); //$NON-NLS-1$

        orderList.clear();

        assertTrue( comparator.compare(packA, packD) > 0);
        assertTrue( comparator.compare(packD, packA) < 0);
        assertTrue( comparator.compare(packA, packB) == 0);

        assertTrue( comparator.compare(packD, packC) < 0);
        assertTrue( comparator.compare(packC, packD) > 0);
        assertTrue( comparator.compare(packD, packB) < 0);
        assertTrue( comparator.compare(packB, packD) > 0);

        assertTrue( comparator.compare(packB, packC) > 0);
        assertTrue( comparator.compare(packC, packB) < 0);

        assertTrue( comparator.compare(packB, packE) == 0);
        assertTrue( comparator.compare(packC, packF) < 0);
        assertTrue( comparator.compare(packM, packF) > 0);
        assertTrue( comparator.compare(packF, packC) > 0);
        assertTrue( comparator.compare(packF, packM) < 0);

        // test not listed package
        IIpsPackageFragment packEnd1 = ipsRoot.createPackageFragment("a.x", true, null); //$NON-NLS-1$
        IIpsPackageFragment packEnd2 = ipsRoot.createPackageFragment("b.y", true, null); //$NON-NLS-1$

        assertTrue( comparator.compare(packA, packEnd1) == 0);
        assertTrue( comparator.compare(packEnd2, packA) > 0);
        assertTrue( comparator.compare(packA, packEnd2) < 0);
        assertTrue( comparator.compare(packF, packEnd2) < 0);
        assertTrue( comparator.compare(packEnd2, packF) > 0);

    }
}
