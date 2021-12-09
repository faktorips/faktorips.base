/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class AlphaNumericSimpleNameComparatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    private Comparator<IIpsElement> comparator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    @Test
    public void testCompare() throws CoreRuntimeException {
        comparator = AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR;

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

        // 4. Object equals
        assertTrue(comparator.compare(packA, packA) == 0);

        // 5. null
        assertTrue(comparator.compare(packA, null) > 0);
        assertTrue(comparator.compare(null, null) == 0);
        assertTrue(comparator.compare(null, packA) < 0);
    }

}
