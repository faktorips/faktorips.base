/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkChangingOverTimeMismatchEntryTest {

    @Mock
    IIpsProject ipsProject;
    @Mock
    IProductCmptType type;
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

    @Before
    public void setUp() throws CoreException {
        setUpLinksForLinkContainer(gen1, link1, link2);
        setUpLinksForLinkContainer(gen2, linkA, linkB);
        setUpLinksForLinkContainer(genLatest, linkLatest1, linkLatest2, linkLatest3);
        setUpLinksForLinkContainer(prodCmpt, staticLink1, staticLink2, staticLink3);

        setUpGenerationOrder();

        when(type.findAssociation("assoc1", ipsProject)).thenReturn(assoc1);
        when(type.findAssociation("assoc2", ipsProject)).thenReturn(assoc2);
        when(type.findAssociation("staticAssoc1", ipsProject)).thenReturn(staticAssoc1);
        when(type.findAssociation("staticAssoc2", ipsProject)).thenReturn(staticAssoc2);

        when(link1.getAssociation()).thenReturn("assoc1");
        when(link1.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(link2.getAssociation()).thenReturn("assoc2");
        when(link2.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(linkA.getAssociation()).thenReturn("assoc1");
        when(linkA.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(linkB.getAssociation()).thenReturn("assoc2");
        when(linkB.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(linkLatest1.getAssociation()).thenReturn("assoc1");
        when(linkLatest1.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(linkLatest2.getAssociation()).thenReturn("assoc2");
        when(linkLatest2.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(linkLatest3.getAssociation()).thenReturn("assoc2");
        when(linkLatest2.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(staticLink1.getAssociation()).thenReturn("staticAssoc1");
        when(staticLink1.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(staticLink2.getAssociation()).thenReturn("staticAssoc1");
        when(staticLink2.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(staticLink3.getAssociation()).thenReturn("staticAssoc2");
        when(staticLink3.findTarget(any(IIpsProject.class))).thenReturn(target);

        when(gen1.getProductCmpt()).thenReturn(prodCmpt);
        when(gen2.getProductCmpt()).thenReturn(prodCmpt);
        when(genLatest.getProductCmpt()).thenReturn(prodCmpt);
        when(prodCmpt.getProductCmpt()).thenReturn(prodCmpt);
        when(prodCmpt.getLatestGeneration()).thenReturn(genLatest);

        when(assoc1.isChangingOverTime()).thenReturn(true);
        when(assoc2.isChangingOverTime()).thenReturn(true);
        when(staticAssoc1.isChangingOverTime()).thenReturn(false);
        when(staticAssoc2.isChangingOverTime()).thenReturn(false);
    }

    private void setUpGenerationOrder() {
        List<IProductCmptGeneration> genList = new ArrayList<IProductCmptGeneration>();
        genList.add(gen1);
        genList.add(gen2);
        genList.add(genLatest);
        when(prodCmpt.getLatestProductCmptGeneration()).thenReturn(genLatest);
        when(prodCmpt.getProductCmptGenerations()).thenReturn(genList);
    }

    private void setUpLinksForLinkContainer(IProductCmptLinkContainer container, IProductCmptLink... links) {
        List<IProductCmptLink> genLinks = new ArrayList<IProductCmptLink>();
        genLinks.addAll(Arrays.asList(links));
        when(container.getLinksAsList()).thenReturn(genLinks);
        for (IProductCmptLink link : links) {
            when(link.getProductCmptLinkContainer()).thenReturn(container);
            when(link.getProductCmpt()).thenReturn(prodCmpt);
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
        when(prodCmpt.newLink(assoc2)).thenReturn(newLink);

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

    @Test
    public void testIsMovingLinkFrom_prodCmpt() {
        when(staticAssoc1.isChangingOverTime()).thenReturn(true);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(staticAssoc1, staticLink1);
        assertTrue(entry.isMovingLink());
    }

    @Test
    public void testIsMovingLinkFrom_oldGeneration1() {
        when(assoc1.isChangingOverTime()).thenReturn(false);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(assoc1, link1);
        assertFalse(entry.isMovingLink());
    }

    @Test
    public void testIsMovingLinkFrom_oldGeneration2() {
        when(assoc2.isChangingOverTime()).thenReturn(false);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(assoc2, linkB);
        assertFalse(entry.isMovingLink());
    }

    @Test
    public void testIsMovingLinkFrom_latestGeneration() {
        when(assoc2.isChangingOverTime()).thenReturn(false);
        LinkChangingOverTimeMismatchEntry entry = new LinkChangingOverTimeMismatchEntry(assoc2, linkLatest2);
        assertTrue(entry.isMovingLink());
    }
}
