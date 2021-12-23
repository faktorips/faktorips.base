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

import static org.faktorips.abstracttest.matcher.ExistsAFileMatcher.exists;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class DefinedOrderComparatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    private DefinedOrderComparator comparator;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    @Test
    public void testCompareDefinedSortOrder() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$

        comparator = new DefinedOrderComparator(packB, packC, packA);

        // 2. X < Y <=> Y > X
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);

        // 3. X < Y & Y < Z => X < Z
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);
        assertTrue(comparator.compare(packC, packA) < 0);
        assertTrue(comparator.compare(packA, packC) > 0);
        assertTrue(comparator.compare(packB, packA) < 0);
        assertTrue(comparator.compare(packA, packB) > 0);

        // 4. Object equals
        assertTrue(comparator.compare(packA, packA) == 0);

        // 5. NullPointerExeption
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

    @Test
    public void testCompareDefinedSortOrderFromFileInRoot() throws CoreRuntimeException, IOException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$
        createSortOrderFile((AFolder)ipsRoot.getCorrespondingResource(), "b", "c", "a");

        comparator = DefinedOrderComparator.forPackage((IpsPackageFragment)ipsRoot.getDefaultIpsPackageFragment());

        // 2. X < Y <=> Y > X
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);

        // 3. X < Y & Y < Z => X < Z
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);
        assertTrue(comparator.compare(packC, packA) < 0);
        assertTrue(comparator.compare(packA, packC) > 0);
        assertTrue(comparator.compare(packB, packA) < 0);
        assertTrue(comparator.compare(packA, packB) > 0);
    }

    @Test
    public void testCompareDefinedSortOrderFromFile() throws CoreRuntimeException, IOException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("x.y.a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("x.y.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("x.y.c", true, null); //$NON-NLS-1$
        IpsPackageFragment parentPack = (IpsPackageFragment)ipsRoot.getIpsPackageFragment("x.y");
        createSortOrderFile((AFolder)parentPack.getCorrespondingResource(), "b", "c", "a");

        comparator = DefinedOrderComparator.forPackage(parentPack);

        // 2. X < Y <=> Y > X
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);

        // 3. X < Y & Y < Z => X < Z
        assertTrue(comparator.compare(packB, packC) < 0);
        assertTrue(comparator.compare(packC, packB) > 0);
        assertTrue(comparator.compare(packC, packA) < 0);
        assertTrue(comparator.compare(packA, packC) > 0);
        assertTrue(comparator.compare(packB, packA) < 0);
        assertTrue(comparator.compare(packA, packB) > 0);
    }

    @Test
    public void testCompareUndefinedSortOrder() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null); //$NON-NLS-1$
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null); //$NON-NLS-1$
        IIpsPackageFragment packE = ipsRoot.createPackageFragment("e", true, null); //$NON-NLS-1$

        comparator = new DefinedOrderComparator(packB, packE, packC);

        assertTrue(comparator.compare(packA, packA) == 0); // ==
        assertTrue(comparator.compare(packA, packB) > 0); // A is not in sortOrder, B is
        assertTrue(comparator.compare(packA, packC) > 0); // A is not in sortOrder, C is
        assertTrue(comparator.compare(packA, packD) < 0); // neither A nor D is in sortOrder,
                                                          // fallback to alphabetic
        assertTrue(comparator.compare(packA, packE) > 0); // A is not in sortOrder, E is

        assertTrue(comparator.compare(packB, packA) < 0); // B is in sortOrder, A isn't
        assertTrue(comparator.compare(packB, packB) == 0); // ==
        assertTrue(comparator.compare(packB, packC) < 0); // B is in sortOrder before C
        assertTrue(comparator.compare(packB, packD) < 0); // B is in sortOrder, D isn't
        assertTrue(comparator.compare(packB, packE) < 0); // B is in sortOrder before E

        assertTrue(comparator.compare(packC, packA) < 0); // C is in sortOrder, A isn't
        assertTrue(comparator.compare(packC, packB) > 0); // C is in sortOrder after B
        assertTrue(comparator.compare(packC, packC) == 0); // ==
        assertTrue(comparator.compare(packC, packD) < 0); // C is in sortOrder, D isn't
        assertTrue(comparator.compare(packC, packE) > 0); // C is in sortOrder after E

        assertTrue(comparator.compare(packD, packA) > 0); // neither D nor A is in sortOrder,
                                                          // fallback to alphabetic
        assertTrue(comparator.compare(packD, packB) > 0); // D is not in sortOrder, B is
        assertTrue(comparator.compare(packD, packC) > 0); // D is not in sortOrder, C is
        assertTrue(comparator.compare(packD, packD) == 0); // ==
        assertTrue(comparator.compare(packD, packE) > 0); // D is not in sortOrder, B is

        assertTrue(comparator.compare(packE, packA) < 0); // E is in sortOrder, A isn't
        assertTrue(comparator.compare(packE, packB) > 0); // E is in sortOrder after B
        assertTrue(comparator.compare(packE, packC) < 0); // E is in sortOrder before C
        assertTrue(comparator.compare(packE, packD) < 0); // E is in sortOrder, D isn't
        assertTrue(comparator.compare(packE, packE) == 0); // ==
    }

    @Test
    public void testPersistTo() throws CoreRuntimeException, IOException {
        IpsPackageFragment packA = (IpsPackageFragment)ipsRoot.createPackageFragment("a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packAA = ipsRoot.createPackageFragment("a.a", true, null); //$NON-NLS-1$
        IIpsPackageFragment packAB = ipsRoot.createPackageFragment("a.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packAC = ipsRoot.createPackageFragment("a.c", true, null); //$NON-NLS-1$
        ProductCmpt prodA = newProductCmpt(ipsProject, "a.A 2018-01"); //$NON-NLS-1$
        ProductCmpt prodA2 = newProductCmpt(ipsProject, "a.A 2018-02"); //$NON-NLS-1$
        ProductCmpt prodB = newProductCmpt(ipsProject, "a.B 2018-01"); //$NON-NLS-1$

        comparator = new DefinedOrderComparator(packAB, packAC, packAA, prodA2.getIpsSrcFile(), prodB.getIpsSrcFile(),
                prodA.getIpsSrcFile());

        comparator.persistTo(packA);

        AFile file = ((AFolder)packA.getCorrespondingResource()).getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME);
        assertThat(file, exists());
        String[] lines = StringUtil.readFromInputStream(file.getContents(), StringUtil.CHARSET_UTF8)
                .split(System.lineSeparator());
        assertThat(lines[0], startsWith("#"));
        assertThat(lines[1], is("b"));
        assertThat(lines[2], is("c"));
        assertThat(lines[3], is("a"));
        assertThat(lines[4], is("A 2018-02.ipsproduct"));
        assertThat(lines[5], is("B 2018-01.ipsproduct"));
        assertThat(lines[6], is("A 2018-01.ipsproduct"));
    }

    @Test
    // this case is not really supported, but a check to prevent it would be unnecessary in the
    // intended
    // usage and is therefore not implemented, as the default does no harm
    public void testCompareElementsInDifferentPackage() throws CoreRuntimeException {
        IIpsPackageFragment packAB = ipsRoot.createPackageFragment("a.b", true, null); //$NON-NLS-1$
        IIpsPackageFragment packAC = ipsRoot.createPackageFragment("a.c", true, null); //$NON-NLS-1$

        comparator = new DefinedOrderComparator();

        assertTrue(comparator.compare(packAB, packAC) < 0); // fallback to alphabetic
        assertTrue(comparator.compare(packAC, packAB) > 0); // fallback to alphabetic
    }

}