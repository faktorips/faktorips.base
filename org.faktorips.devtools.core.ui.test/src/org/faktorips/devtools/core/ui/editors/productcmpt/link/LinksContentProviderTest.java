/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
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
}
