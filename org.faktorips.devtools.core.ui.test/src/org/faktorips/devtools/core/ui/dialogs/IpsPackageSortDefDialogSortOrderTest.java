/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.faktorips.abstracttest.matcher.ExistsFileMatcher.exists;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.core.ui.dialogs.IpsPackageSortDefDialog.SortOrder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsPackageSortDefDialogSortOrderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;
    private AFolder folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
        folder = (AFolder)ipsRoot.getCorrespondingResource();
    }

    @Test
    public void testRestore() throws CoreRuntimeException, IOException {
        ipsRoot.createPackageFragment("a", true, null);
        ipsRoot.createPackageFragment("b", true, null);
        ipsRoot.createPackageFragment("c", true, null);
        createSortOrderFile(folder, "b", "c",
                "a");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();
        assertThat(defaultIpsPackageFragment.getChildOrderComparator(), is(instanceOf(DefinedOrderComparator.class)));

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.restore();

        // no change visible outside
        assertThat(defaultIpsPackageFragment.getChildOrderComparator(), is(instanceOf(DefinedOrderComparator.class)));

        sortOrder.save();

        assertThat(defaultIpsPackageFragment.getChildOrderComparator(),
                is(AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR));
    }

    @Test
    public void testRestoreAndDirty() throws CoreRuntimeException, IOException {
        ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        ipsRoot.createPackageFragment("c", true, null);
        createSortOrderFile(folder, "b", "c", "a");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();
        assertThat(defaultIpsPackageFragment.getChildOrderComparator(), is(instanceOf(DefinedOrderComparator.class)));

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.restore();

        // no change visible outside
        assertThat(defaultIpsPackageFragment.getChildOrderComparator(), is(instanceOf(DefinedOrderComparator.class)));

        // makes sortOrder "dirty"
        sortOrder.up(Arrays.asList(packB));

        sortOrder.save();

        assertThat(defaultIpsPackageFragment.getChildOrderComparator(), is(instanceOf(DefinedOrderComparator.class)));
    }

    @Test
    public void testSave_nothingChanged() throws CoreRuntimeException {
        ipsRoot.createPackageFragment("a", true, null);
        ipsRoot.createPackageFragment("b", true, null);
        ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        IFile file = ((AFolder)defaultIpsPackageFragment.getCorrespondingResource())
                .getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME).unwrap();
        assertThat(file, not(exists()));
    }

    @Test
    public void testSave_newSortOrder() throws CoreRuntimeException {
        ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);
        sortOrder.up(Arrays.asList(packB));
        sortOrder.save();

        IFile file = ((AFolder)defaultIpsPackageFragment.getCorrespondingResource())
                .getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME).unwrap();
        assertThat(file, exists());
    }

    @Test
    public void testGetElements_inDefaultOrder() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        ProductCmpt prodA = newProductCmpt(ipsProject, "A 2018-01");
        ProductCmpt prodA2 = newProductCmpt(ipsProject, "A 2018-02");
        ProductCmpt prodB = newProductCmpt(ipsProject, "B 2018-01");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packA));
        assertThat((IIpsPackageFragment)elements[1], is(packB));
        assertThat((IIpsPackageFragment)elements[2], is(packC));
        assertThat((IIpsSrcFile)elements[3], is(prodA.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[4], is(prodA2.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[5], is(prodB.getIpsSrcFile()));
    }

    @Test
    public void testGetElements_inSortOrder() throws CoreRuntimeException, IOException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        ProductCmpt prodA = newProductCmpt(ipsProject, "A 2018-01");
        ProductCmpt prodA2 = newProductCmpt(ipsProject, "A 2018-02");
        ProductCmpt prodB = newProductCmpt(ipsProject, "B 2018-01");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();
        createSortOrderFile(folder, "b", "c", "a",
                "B 2018-01.ipsproduct",
                "A 2018-02.ipsproduct", "A 2018-01.ipsproduct");

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packB));
        assertThat((IIpsPackageFragment)elements[1], is(packC));
        assertThat((IIpsPackageFragment)elements[2], is(packA));
        assertThat((IIpsSrcFile)elements[3], is(prodB.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[4], is(prodA2.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[5], is(prodA.getIpsSrcFile()));
    }

    @Test
    public void testGetElements_inSortOrderAndDefaultOrder() throws CoreRuntimeException, IOException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        ProductCmpt prodA = newProductCmpt(ipsProject, "A 2018-01");
        ProductCmpt prodA2 = newProductCmpt(ipsProject, "A 2018-02");
        ProductCmpt prodB = newProductCmpt(ipsProject, "B 2018-01");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();
        createSortOrderFile(folder, "b",
                "B 2018-01.ipsproduct");

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packB));
        assertThat((IIpsPackageFragment)elements[1], is(packA));
        assertThat((IIpsPackageFragment)elements[2], is(packC));
        assertThat((IIpsSrcFile)elements[3], is(prodB.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[4], is(prodA.getIpsSrcFile()));
        assertThat((IIpsSrcFile)elements[5], is(prodA2.getIpsSrcFile()));
    }

    @Test
    public void testUp() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.up(Arrays.asList(packB));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packB));
        assertThat((IIpsPackageFragment)elements[1], is(packA));
        assertThat((IIpsPackageFragment)elements[2], is(packC));
    }

    @Test
    public void testUpList() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null);
        IIpsPackageFragment packE = ipsRoot.createPackageFragment("e", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.up(Arrays.asList(packB, packD));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packB));
        assertThat((IIpsPackageFragment)elements[1], is(packA));
        assertThat((IIpsPackageFragment)elements[2], is(packD));
        assertThat((IIpsPackageFragment)elements[3], is(packC));
        assertThat((IIpsPackageFragment)elements[4], is(packE));
    }

    @Test
    public void testDown() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.down(Arrays.asList(packB));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packA));
        assertThat((IIpsPackageFragment)elements[1], is(packC));
        assertThat((IIpsPackageFragment)elements[2], is(packB));
    }

    @Test
    public void testDownList() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null);
        IIpsPackageFragment packE = ipsRoot.createPackageFragment("e", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.down(Arrays.asList(packB, packD));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packA));
        assertThat((IIpsPackageFragment)elements[1], is(packC));
        assertThat((IIpsPackageFragment)elements[2], is(packB));
        assertThat((IIpsPackageFragment)elements[3], is(packE));
        assertThat((IIpsPackageFragment)elements[4], is(packD));
    }

    @Test
    public void testAbove() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null);
        IIpsPackageFragment packE = ipsRoot.createPackageFragment("e", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.above(packB, Arrays.asList(packC, packE));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packA));
        assertThat((IIpsPackageFragment)elements[1], is(packC));
        assertThat((IIpsPackageFragment)elements[2], is(packE));
        assertThat((IIpsPackageFragment)elements[3], is(packB));
        assertThat((IIpsPackageFragment)elements[4], is(packD));
    }

    @Test
    public void testBelow() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment packC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment packD = ipsRoot.createPackageFragment("d", true, null);
        IIpsPackageFragment packE = ipsRoot.createPackageFragment("e", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        sortOrder.below(packD, Arrays.asList(packA, packC));

        Object[] elements = sortOrder.getElements(defaultIpsPackageFragment);
        assertThat((IIpsPackageFragment)elements[0], is(packB));
        assertThat((IIpsPackageFragment)elements[1], is(packD));
        assertThat((IIpsPackageFragment)elements[2], is(packA));
        assertThat((IIpsPackageFragment)elements[3], is(packC));
        assertThat((IIpsPackageFragment)elements[4], is(packE));
    }

    @Test
    public void testIsFirstPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        assertThat(sortOrder.isFirst(packA), is(true));
        assertThat(sortOrder.isFirst(packB), is(false));
    }

    @Test
    public void testIsLastPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment packA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment packB = ipsRoot.createPackageFragment("b", true, null);
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        assertThat(sortOrder.isLast(packA), is(false));
        assertThat(sortOrder.isLast(packB), is(true));
    }

    @Test
    public void testIsFirstSrcFile() throws CoreRuntimeException {
        ProductCmpt prodA = newProductCmpt(ipsProject, "A 2018-01");
        ProductCmpt prodB = newProductCmpt(ipsProject, "B 2018-01");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        assertThat(sortOrder.isFirst(prodA.getIpsSrcFile()), is(true));
        assertThat(sortOrder.isFirst(prodB.getIpsSrcFile()), is(false));
    }

    @Test
    public void testIsLastSrcFile() throws CoreRuntimeException {
        ProductCmpt prodA = newProductCmpt(ipsProject, "A 2018-01");
        ProductCmpt prodB = newProductCmpt(ipsProject, "B 2018-01");
        IIpsPackageFragment defaultIpsPackageFragment = ipsRoot.getDefaultIpsPackageFragment();

        SortOrder sortOrder = new SortOrder();
        sortOrder.inputChanged(null, null, defaultIpsPackageFragment);

        assertThat(sortOrder.isLast(prodA.getIpsSrcFile()), is(false));
        assertThat(sortOrder.isLast(prodB.getIpsSrcFile()), is(true));
    }
}
