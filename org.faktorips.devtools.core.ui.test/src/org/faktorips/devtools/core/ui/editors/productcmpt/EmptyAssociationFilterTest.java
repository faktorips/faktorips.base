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

package org.faktorips.devtools.core.ui.editors.productcmpt;

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
    public void testOtherNode() {
        assertTrue(filter.select(viewer, "TeilkaskoLvbArt", null));
    }

    @Test
    public void testProductCmptGeneration() {
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);

        String association = "TeilkaskoLvbArt";
        IProductCmptLink link = mock(IProductCmptLink.class);

        when(generation.getLinks(association)).thenReturn(new IProductCmptLink[] { link });
        assertTrue(filter.select(viewer, generation, association));

        String emptyAssociation = "EmptyArt";
        when(generation.getLinks(emptyAssociation)).thenReturn(new IProductCmptLink[0]);

        assertFalse(filter.select(viewer, generation, emptyAssociation));

    }
}
