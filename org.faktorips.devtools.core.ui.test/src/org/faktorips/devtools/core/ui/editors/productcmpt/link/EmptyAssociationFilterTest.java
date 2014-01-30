/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
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
    public void testFiltering() {
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);

        String association = "TeilkaskoLvbArt";
        AbstractAssociationViewItem viewItem = mock(AbstractAssociationViewItem.class);
        when(viewItem.getAssociationName()).thenReturn(association);

        IProductCmptLink link = mock(IProductCmptLink.class);

        when(generation.getLinks(association)).thenReturn(new IProductCmptLink[] { link });
        assertTrue(filter.select(viewer, generation, viewItem));
    }

    @Test
    public void testNotFiltering() {
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);

        String emptyAssociation = "EmptyArt";

        AbstractAssociationViewItem viewItem = mock(AbstractAssociationViewItem.class);
        when(viewItem.getAssociationName()).thenReturn(emptyAssociation);

        when(generation.getLinks(emptyAssociation)).thenReturn(new IProductCmptLink[0]);

        assertFalse(filter.select(viewer, generation, viewItem));
    }
}
