/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.internal.productcmpt.deltaentries.LinkChangingOverTimeMismatchEntry;
import org.faktorips.devtools.model.internal.productcmpt.deltaentries.LinkWithoutAssociationEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptGenerationToTypeDeltaTest {

    @Mock
    private IProductCmptGeneration gen;
    @Mock
    private IProductCmpt prodCmpt;
    @Mock
    private IProductCmpt target1;
    @Mock
    private IProductCmpt target2;
    @Mock
    private IProductCmpt target3;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProductCmptLink link1;
    @Mock
    private IProductCmptLink link2;
    @Mock
    private IProductCmptLink link3;
    @Mock
    private IProductCmptLink staticLink1;
    @Mock
    private IProductCmptLink staticLink2;
    @Mock
    private IProductCmptType type;
    @Mock
    private IProductCmptTypeAssociation staticAssociation;
    @Mock
    private IProductCmptTypeAssociation assoc1;
    @Mock
    private IProductCmptTypeAssociation assoc2;
    private ProductCmptGenerationToTypeDelta delta;

    @Before
    public void setUp() {
        delta = spy(new ProductCmptGenerationToTypeDelta(gen, ipsProject));

        List<IProductCmptLink> genLinks = new ArrayList<>();
        genLinks.add(link1);
        genLinks.add(link2);
        genLinks.add(link3);
        when(gen.getLinksAsList()).thenReturn(genLinks);
        List<IProductCmptLink> cmptLinks = new ArrayList<>();
        cmptLinks.add(staticLink1);
        cmptLinks.add(staticLink2);
        when(prodCmpt.getLinksAsList()).thenReturn(cmptLinks);

        setUpLink(link1, "assoc1", gen, target1);
        setUpLink(link2, "assoc2", gen, target2);
        setUpLink(link3, "assoc2", gen, target3);
        setUpLink(staticLink1, "staticAssociation", prodCmpt, target1);
        setUpLink(staticLink2, "staticAssociation", prodCmpt, target2);

        doReturn(type).when(delta).getProductCmptType();
        doReturn(ipsProject).when(delta).getIpsProject();

        when(type.findAssociation("assoc1", ipsProject)).thenReturn(assoc1);
        when(type.findAssociation("assoc2", ipsProject)).thenReturn(assoc2);
        when(type.findAssociation("staticAssociation", ipsProject)).thenReturn(staticAssociation);
        when(gen.isContainerFor(assoc1)).thenReturn(true);
        when(gen.isContainerFor(assoc2)).thenReturn(true);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
    }

    private void setUpLink(IProductCmptLink link,
            String association,
            IProductCmptLinkContainer container,
            IProductCmpt target) {
        when(link.getAssociation()).thenReturn(association);
        when(link.getProductCmptLinkContainer()).thenReturn(container);
        when(link.findTarget(any(IIpsProject.class))).thenReturn(target);
        when(link.getTarget()).thenReturn("anyTargetName");
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry1() {
        verifyAddEntryForLink("assoc1", link1);
    }

    @Test
    public void testCreateLinkWithoutAssociationEntry2() {
        verifyAddEntryForLink("assoc2", link2, link3);
    }

    @Test
    public void testChangingOverTimeMismatch() {
        when(gen.isContainerFor(assoc1)).thenReturn(false);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);

        delta.createEntriesForLinks();

        verify(delta).addEntry(captor.capture());
        assertEquals(link1, captor.getValue().getLink());
    }

    @Test
    public void testChangingOverTimeMismatch2() {
        when(gen.isContainerFor(assoc2)).thenReturn(false);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(true);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);

        delta.createEntriesForLinks();

        verify(delta, times(2)).addEntry(captor.capture());
        assertEquals(link2, captor.getAllValues().get(0).getLink());
        assertEquals(link3, captor.getAllValues().get(1).getLink());
    }

    @Test
    public void testChangingOverTimeMismatchStaticAssociation() {
        when(gen.isContainerFor(staticAssociation)).thenReturn(true);
        when(prodCmpt.isContainerFor(staticAssociation)).thenReturn(false);
        ArgumentCaptor<LinkChangingOverTimeMismatchEntry> captor = ArgumentCaptor
                .forClass(LinkChangingOverTimeMismatchEntry.class);
        // fake prod cmpt as link container so mismatch entries will be created for static links
        doReturn(prodCmpt).when(delta).getLinkContainer();

        delta.createEntriesForLinks();

        verify(delta, times(2)).addEntry(captor.capture());
        assertEquals(staticLink1, captor.getAllValues().get(0).getLink());
        assertEquals(staticLink2, captor.getAllValues().get(1).getLink());
    }

    private void verifyAddEntryForLink(String assocName, IProductCmptLink... links) {
        when(type.findAssociation(assocName, ipsProject)).thenReturn(null);
        ArgumentCaptor<LinkWithoutAssociationEntry> captor = ArgumentCaptor.forClass(LinkWithoutAssociationEntry.class);

        delta.createEntriesForLinks();

        verify(delta, times(links.length)).addEntry(captor.capture());
        for (int i = 0; i < links.length; i++) {
            assertEquals(links[i], captor.getAllValues().get(i).getLink());
        }
    }
}
