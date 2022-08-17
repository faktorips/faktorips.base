/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinksContentProviderTest {

    private IProductCmptGeneration gen;
    private IProductCmpt prodCmpt;
    @Mock
    private ProductCmptLink link1;
    @Mock
    private ProductCmptLink link2;
    @Mock
    private ProductCmptLink link3;
    @Mock
    private ProductCmptLink staticLink1;
    @Mock
    private ProductCmptLink staticLink2;
    @Mock
    private IProductCmptType type;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private LinkViewItem linkViewItem;

    private LinksContentProvider provider;
    private List<ProductCmptLink> links;
    private List<ProductCmptLink> staticLinks;

    @Before
    public void setUp() {
        gen = mock(ProductCmptGeneration.class, CALLS_REAL_METHODS);
        prodCmpt = mock(ProductCmpt.class, CALLS_REAL_METHODS);

        provider = new LinksContentProvider();

        when(link1.getAssociation()).thenReturn("dummyAssociation");
        when(link2.getAssociation()).thenReturn("dummyAssociation");
        when(link3.getAssociation()).thenReturn("dummyAssociation");
        when(staticLink1.getAssociation()).thenReturn("staticDummyAssociation");
        when(staticLink2.getAssociation()).thenReturn("staticDummyAssociation");
        links = new ArrayList<>();
        links.add(link1);
        links.add(link2);
        links.add(link3);
        staticLinks = new ArrayList<>();
        staticLinks.add(staticLink1);
        staticLinks.add(staticLink2);

        when(gen.getProductCmpt()).thenReturn(prodCmpt);
        doReturn(links).when(gen).getLinksAsList("dummyAssociation");
        doReturn(staticLinks).when(prodCmpt).getLinksAsList("staticDummyAssociation");
    }

    @Test
    public void testGetDetachedAssociationViewItems() {
        doReturn(links).when(gen).getLinksAsList();
        doReturn(staticLinks).when(prodCmpt).getLinksAsList();

        DetachedAssociationViewItem[] associationItems = provider.getDetachedAssociationViewItems(gen);
        assertEquals(2, associationItems.length);
        assertEquals("staticDummyAssociation", associationItems[0].getAssociationName());
        assertEquals("dummyAssociation", associationItems[1].getAssociationName());
    }

    @Test
    public void testGetAssociationItems() {
        List<IProductCmptTypeAssociation> listAssociations = new ArrayList<>();
        IProductCmptTypeAssociation asso1 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation asso2 = mock(IProductCmptTypeAssociation.class);
        when(asso1.isRelevant()).thenReturn(false);
        when(asso2.isRelevant()).thenReturn(true);
        listAssociations.add(asso1);
        listAssociations.add(asso2);
        when(type.findAllNotDerivedAssociations(ipsProject)).thenReturn(listAssociations);
        when(gen.isContainerFor(asso2)).thenReturn(true);
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(asso2.getProductCmptType()).thenReturn(productCmptType);
        when(productCmptType.isChangingOverTime()).thenReturn(true);

        AssociationViewItem[] associationItems = provider.getAssociationItems(type, ipsProject, gen);
        assertTrue(associationItems.length == 1);
        assertEquals(asso2, associationItems[0].getAssociation());
    }

    @Test
    public void testGetParent() {
        IProductCmptTypeAssociation asso1 = mock(IProductCmptTypeAssociation.class);
        when(asso1.isRelevant()).thenReturn(true);
        when(linkViewItem.getLink()).thenReturn(link1);
        when(link1.getIpsProject()).thenReturn(ipsProject);
        when(link1.findAssociation(ipsProject)).thenReturn(asso1);
        IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(gen, asso1);
        when(link1.getProductCmptLinkContainer()).thenReturn(container);
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(asso1.getProductCmptType()).thenReturn(productCmptType);
        when(productCmptType.isChangingOverTime()).thenReturn(true);

        AssociationViewItem expectedAssoViewItem = new AssociationViewItem(container, asso1);
        Object parent = provider.getParent(linkViewItem);

        assertTrue(parent instanceof AssociationViewItem);
        assertEquals(expectedAssoViewItem, parent);
    }

    @Test
    public void testGetParent_ParameterIsNotALinkViewItem() {
        IProductCmptTypeAssociation asso1 = mock(IProductCmptTypeAssociation.class);
        when(asso1.isRelevant()).thenReturn(true);
        when(linkViewItem.getLink()).thenReturn(link1);
        when(link1.getIpsProject()).thenReturn(ipsProject);
        when(link1.findAssociation(ipsProject)).thenReturn(asso1);
        IProductCmptLinkContainer container = LinkCreatorUtil.getLinkContainerFor(gen, asso1);
        when(link1.getProductCmptLinkContainer()).thenReturn(container);

        Object parent = provider.getParent(asso1);

        assertNull(parent);
    }
}
