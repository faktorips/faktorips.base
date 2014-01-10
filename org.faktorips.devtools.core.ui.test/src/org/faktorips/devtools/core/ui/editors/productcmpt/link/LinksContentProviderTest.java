/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptLink;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
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
        links = new ArrayList<ProductCmptLink>();
        links.add(link1);
        links.add(link2);
        links.add(link3);
        staticLinks = new ArrayList<ProductCmptLink>();
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
        List<IProductCmptTypeAssociation> listAssociations = new ArrayList<IProductCmptTypeAssociation>();
        IProductCmptTypeAssociation asso1 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation asso2 = mock(IProductCmptTypeAssociation.class);
        when(asso1.isRelevant()).thenReturn(false);
        when(asso2.isRelevant()).thenReturn(true);
        listAssociations.add(asso1);
        listAssociations.add(asso2);
        when(type.findAllNotDerivedAssociations(ipsProject)).thenReturn(listAssociations);
        when(gen.isContainerFor(asso2)).thenReturn(true);

        AssociationViewItem[] associationItems = provider.getAssociationItems(type, ipsProject, gen);
        assertTrue(associationItems.length == 1);
        assertEquals(asso2, associationItems[0].getAssociation());
    }
}
