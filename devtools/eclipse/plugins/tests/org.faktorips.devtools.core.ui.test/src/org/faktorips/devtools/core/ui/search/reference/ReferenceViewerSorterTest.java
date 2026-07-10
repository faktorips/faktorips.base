/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.ui.search.reference.ReferenceSearchResultPage.ReferenceViewerSorter;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Before;
import org.junit.Test;

public class ReferenceViewerSorterTest {

    private ReferenceViewerSorter sorter;

    @Before
    public void setUp() {
        sorter = new ReferenceSearchResultPage().new ReferenceViewerSorter();
    }

    @Test
    public void testCompare_DeepHierarchy_TenElementsSortedCorrectly() {
        IIpsElement akb202306FlorteKomfort = mockPackageFragment(
                "AKB_202306.flotte_komfort_202306.klauseln.bedingungen");
        IIpsElement akb202306FlorteKompakt = mockPackageFragment(
                "AKB_202306.flotte_kompakt_202306.klauseln.bedingungen");
        IIpsElement akb202306Pauschalbeitrag = mockPackageFragment(
                "AKB_202306.pauschalbeitrag_202306.klauseln.bedingungen");
        IIpsElement akb202306Standalone = mockPackageFragment("AKB_202306.standalone_202306.klauseln.bedingungen");
        IIpsElement akb202306Stueckbeitrag = mockPackageFragment(
                "AKB_202306.stueckbeitrag_202306.klauseln.bedingungen");
        IIpsElement akb202406FlorteKomfort = mockPackageFragment(
                "AKB_202406.flotte_komfort_202406.klauseln.bedingungen");
        IIpsElement akb202406FlorteKompakt = mockPackageFragment(
                "AKB_202406.flotte_kompakt_202406.klauseln.bedingungen");
        IIpsElement akb202406Pauschalbeitrag = mockPackageFragment(
                "AKB_202406.pauschalbeitrag_202406.klauseln.bedingungen");
        IIpsElement akb202406Standalone = mockPackageFragment("AKB_202406.standalone_202406.klauseln.bedingungen");
        IIpsElement akb202406Stueckbeitrag = mockPackageFragment(
                "AKB_202406.stueckbeitrag_202406.klauseln.bedingungen");

        List<IIpsElement> shuffled = Arrays.asList(
                akb202406Stueckbeitrag, akb202306Stueckbeitrag, akb202406Pauschalbeitrag,
                akb202306FlorteKompakt, akb202406FlorteKomfort, akb202306Standalone,
                akb202406Standalone, akb202306FlorteKomfort, akb202406FlorteKompakt,
                akb202306Pauschalbeitrag);
        shuffled.sort((a, b) -> sorter.compare(null, a, b));

        assertThat(shuffled, contains(
                akb202306FlorteKomfort,
                akb202306FlorteKompakt,
                akb202306Pauschalbeitrag,
                akb202306Standalone,
                akb202306Stueckbeitrag,
                akb202406FlorteKomfort,
                akb202406FlorteKompakt,
                akb202406Pauschalbeitrag,
                akb202406Standalone,
                akb202406Stueckbeitrag));
    }

    @Test
    public void testCompare_PackageFragments_AlphanumericVersionOrder() {
        IIpsElement pkg2019 = mockPackageFragment("produkt_2019");
        IIpsElement pkg2020 = mockPackageFragment("produkt_2020");
        IIpsElement pkg2021 = mockPackageFragment("produkt_2021");

        assertThat(sorter.compare(null, pkg2019, pkg2020), lessThan(0));
        assertThat(sorter.compare(null, pkg2020, pkg2021), lessThan(0));
        assertThat(sorter.compare(null, pkg2019, pkg2021), lessThan(0));
        assertThat(sorter.compare(null, pkg2021, pkg2019), greaterThan(0));
    }

    @Test
    public void testCompare_PackageFragments_AlphabeticOrder() {
        IIpsElement varianteKomfort = mockPackageFragment("variante_komfort_2019");
        IIpsElement varianteKompakt = mockPackageFragment("variante_kompakt_2019");
        IIpsElement varianteStandard = mockPackageFragment("variante_standard_2019");

        assertThat(sorter.compare(null, varianteKomfort, varianteKompakt), lessThan(0));
        assertThat(sorter.compare(null, varianteKompakt, varianteStandard), lessThan(0));
        assertThat(sorter.compare(null, varianteKomfort, varianteStandard), lessThan(0));
    }

    @Test
    public void testCompare_PackageFragments_SharedPrefix() {
        IIpsElement komfort = mockPackageFragment("produkt_2019.variante_komfort_2019.bedingungen");
        IIpsElement standard = mockPackageFragment("produkt_2019.variante_standard_2019.bedingungen");

        assertThat(sorter.compare(null, komfort, standard), lessThan(0));
        assertThat(sorter.compare(null, standard, komfort), greaterThan(0));
    }

    @Test
    public void testCompare_PackageFragments_EqualNames() {
        IIpsElement pkgA = mockPackageFragment("produkt_2019.variante_komfort_2019.bedingungen");
        IIpsElement pkgB = mockPackageFragment("produkt_2019.variante_komfort_2019.bedingungen");

        assertThat(sorter.compare(null, pkgA, pkgB), equalTo(0));
    }

    @Test
    public void testCompare_TestCases_SortedToEnd() {
        IIpsObject productA = mockIpsObject("productA1", IpsObjectType.PRODUCT_CMPT);
        IIpsObject productB = mockIpsObject("productB2", IpsObjectType.PRODUCT_CMPT);
        IIpsObject productC = mockIpsObject("productC3", IpsObjectType.PRODUCT_CMPT);
        IIpsObject productD = mockIpsObject("productD4", IpsObjectType.PRODUCT_CMPT);
        IIpsObject testCase1 = mockIpsObject("test1", IpsObjectType.TEST_CASE);
        IIpsObject testCase2 = mockIpsObject("test2", IpsObjectType.TEST_CASE);
        IIpsObject testCase3 = mockIpsObject("test3", IpsObjectType.TEST_CASE);
        IIpsObject testCase4 = mockIpsObject("test4", IpsObjectType.TEST_CASE);

        List<IIpsElement> shuffled = Arrays.asList(
                testCase3, productD, testCase1, productB,
                testCase4, productC, productA, testCase2);
        shuffled.sort((a, b) -> sorter.compare(null, a, b));

        assertThat(shuffled, contains(
                productA,
                productB,
                productC,
                productD,
                testCase1,
                testCase2,
                testCase3,
                testCase4));
    }

    private IIpsElement mockPackageFragment(String name) {
        IIpsElement element = mock(IIpsElement.class);
        when(element.getName()).thenReturn(name);
        return element;
    }

    private IIpsObject mockIpsObject(String name, IpsObjectType objectType) {
        IIpsObject object = mock(IIpsObject.class);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(object.getIpsSrcFile()).thenReturn(srcFile);
        when(srcFile.getIpsObjectType()).thenReturn(objectType);
        when(srcFile.getName()).thenReturn(name);
        return object;
    }

}
