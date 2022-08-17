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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.junit.Before;
import org.junit.Test;

public class EmptyAssociationFilterTest {

    private ViewerFilter filter;
    private Viewer viewer;

    @Before
    public void setUp() {
        filter = new EmptyAssociationFilter();
        viewer = mock(Viewer.class);
    }

    @Test
    public void testSomeNode() {
        assertTrue(filter.select(viewer, "TeilkaskoLvbArt", null));
    }

    @Test
    public void testSelect_normalAssociations() {
        String association = "TeilkaskoLvbArt";
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);
        mockAssociationAndLinks(association, generation, mock(IProductCmptLink.class), mock(IProductCmptLink.class));
        AbstractAssociationViewItem viewItem = mockViewItemWithAssociationName(association, generation);

        assertTrue(filter.select(viewer, generation, viewItem));
    }

    @Test
    public void testSelect_filterEmptyAssociations() {
        String emptyAssociation = "EmptyArt";
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);
        mockAssociationAndLinks(emptyAssociation, generation);
        AbstractAssociationViewItem viewItem = mockViewItemWithAssociationName(emptyAssociation, generation);

        assertFalse(filter.select(viewer, generation, viewItem));
    }

    @Test
    public void testSelect_staticAssociations() {
        String association = "TeilkaskoLvbArt";
        IProductCmpt prodCmpt = mock(IProductCmpt.class);
        mockAssociationAndLinks(association, prodCmpt, mock(IProductCmptLink.class), mock(IProductCmptLink.class));
        AbstractAssociationViewItem viewItem = mockViewItemWithAssociationName(association, prodCmpt);

        assertTrue(filter.select(viewer, prodCmpt, viewItem));
    }

    @Test
    public void testSelect_filterEmptyStaticAssociations() {
        String emptyAssociation = "EmptyArt";
        IProductCmpt prodCmpt = mock(IProductCmpt.class);
        mockAssociationAndLinks(emptyAssociation, prodCmpt);
        AbstractAssociationViewItem viewItem = mockViewItemWithAssociationName(emptyAssociation, prodCmpt);

        assertFalse(filter.select(viewer, prodCmpt, viewItem));
    }

    private void mockAssociationAndLinks(String association,
            IProductCmptLinkContainer container,
            IProductCmptLink... links) {
        when(container.getLinksAsList(association)).thenReturn(Arrays.asList(links));
    }

    private AbstractAssociationViewItem mockViewItemWithAssociationName(String association,
            IProductCmptLinkContainer linkContainer) {
        AbstractAssociationViewItem viewItem = mock(AbstractAssociationViewItem.class);
        when(viewItem.getAssociationName()).thenReturn(association);
        when(viewItem.getLinkContainer()).thenReturn(linkContainer);
        return viewItem;
    }
}
