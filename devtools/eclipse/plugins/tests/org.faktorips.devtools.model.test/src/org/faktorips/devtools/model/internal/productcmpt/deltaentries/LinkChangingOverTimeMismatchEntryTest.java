/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class LinkChangingOverTimeMismatchEntryTest {


    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProductCmptType type;
    @Mock
    private IProductCmptGeneration gen1;
    @Mock
    private IProductCmptGeneration gen2;
    @Mock
    private IProductCmptGeneration genLatest;
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmpt target;
    @Mock
    private IProductCmptTypeAssociation assoc1;
    @Mock
    private IProductCmptTypeAssociation assoc2;
    @Mock
    private IProductCmptTypeAssociation staticAssoc1;
    @Mock
    private IProductCmptTypeAssociation staticAssoc2;
    @Mock
    private IProductCmptLink link1;
    @Mock
    private IProductCmptLink link2;
    @Mock
    private IProductCmptLink linkA;
    @Mock
    private IProductCmptLink linkB;
    @Mock
    private IProductCmptLink linkLatest1;
    @Mock
    private IProductCmptLink linkLatest2;
    @Mock
    private IProductCmptLink linkLatest3;
    @Mock
    private IProductCmptLink staticLink1;
    @Mock
    private IProductCmptLink staticLink2;
    @Mock
    private IProductCmptLink staticLink3;

    private MockitoSession mockito;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        setUpLinksForLinkContainer(gen1, link1, link2);
        setUpLinksForLinkContainer(gen2, linkA, linkB);
        setUpLinksForLinkContainer(genLatest, linkLatest1, linkLatest2, linkLatest3);
        setUpLinksForLinkContainer(prodCmpt, staticLink1, staticLink2, staticLink3);

        setUpGenerationOrder();

        setUpLink(link1);
        setUpLink(link2);
        setUpLink(linkA);
        setUpLink(linkB);
        setUpLink(linkLatest1);
        setUpLink(linkLatest2);
        setUpLink(linkLatest3);
        setUpLink(staticLink1);
        setUpLink(staticLink2);
        setUpLink(staticLink3);

        lenient().when(prodCmpt.getProductCmpt()).thenReturn(prodCmpt);

        lenient().when(assoc2.isChangingOverTime()).thenReturn(true);
        lenient().when(staticAssoc1.isChangingOverTime()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    private void setUpLink(IProductCmptLink link) {
        lenient().when(link.getTarget()).thenReturn("targetName");
    }

    private void setUpGenerationOrder() {
        List<IProductCmptGeneration> genList = new ArrayList<>();
        genList.add(gen1);
        genList.add(gen2);
        genList.add(genLatest);
        lenient().when(prodCmpt.getLatestProductCmptGeneration()).thenReturn(genLatest);
        lenient().when(prodCmpt.getProductCmptGenerations()).thenReturn(genList);
    }

    private void setUpLinksForLinkContainer(IProductCmptLinkContainer container, IProductCmptLink... links) {
        List<IProductCmptLink> genLinks = new ArrayList<>();
        genLinks.addAll(Arrays.asList(links));
        for (IProductCmptLink link : links) {
            lenient().when(link.getProductCmptLinkContainer()).thenReturn(container);
            lenient().when(link.getProductCmpt()).thenReturn(prodCmpt);
        }
    }

    @Test
    public void testFixLatestGenerationToProdCmpt() {
        when(assoc2.isChangingOverTime()).thenReturn(false);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(assoc2, linkLatest2);
        IProductCmptLink newLink = mock(IProductCmptLink.class);
        when(prodCmpt.newLink(assoc2)).thenReturn(newLink);

        entry.fix();

        verify(prodCmpt).newLink(assoc2);
        verify(newLink).copyFrom(linkLatest2);
        verify(linkLatest2).delete();
    }

    @Test
    public void testFixOutdatedGenerationToProdCmpt() {
        when(assoc2.isChangingOverTime()).thenReturn(false);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(assoc2, link2);
        IProductCmptLink newLink = mock(IProductCmptLink.class);

        entry.fix();

        verify(prodCmpt, never()).newLink(assoc2);
        verify(newLink, never()).copyFrom(linkLatest2);
        verify(link2).delete();
    }

    @Test
    public void testFixProdCmptToAllGenerations() {
        when(staticAssoc1.isChangingOverTime()).thenReturn(true);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(staticAssoc1, staticLink1);
        IProductCmptLink newLink1 = mock(IProductCmptLink.class);
        IProductCmptLink newLink2 = mock(IProductCmptLink.class);
        IProductCmptLink newLinkLatest = mock(IProductCmptLink.class);
        when(gen1.newLink(staticAssoc1)).thenReturn(newLink1);
        when(gen2.newLink(staticAssoc1)).thenReturn(newLink2);
        when(genLatest.newLink(staticAssoc1)).thenReturn(newLinkLatest);

        entry.fix();

        verify(gen1).newLink(staticAssoc1);
        verify(gen2).newLink(staticAssoc1);
        verify(genLatest).newLink(staticAssoc1);
        verify(newLink1).copyFrom(staticLink1);
        verify(newLink2).copyFrom(staticLink1);
        verify(newLinkLatest).copyFrom(staticLink1);
        verify(staticLink1).delete();
    }

}
