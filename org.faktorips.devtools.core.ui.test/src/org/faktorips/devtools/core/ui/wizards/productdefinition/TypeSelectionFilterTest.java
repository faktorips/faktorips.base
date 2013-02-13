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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TypeSelectionFilterTest {

    private String existingIpsElementName = "TarifHausrat";

    @Mock
    private IIpsObject iipsObject;

    @Mock
    private IpsObject ipsObject;

    @Mock
    private TypeSelectionFilter filter;

    @Mock
    private StructuredViewer viewer;

    @Mock
    private Object parentObject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ILabelProvider labelProvider = mock(ILabelProvider.class);
        when(viewer.getLabelProvider()).thenReturn(labelProvider);
        when(labelProvider.getText(ipsObject)).thenReturn(existingIpsElementName);
        filter = new TypeSelectionFilter();
    }

    @Test
    public void testNoInput() {
        filter.setSearchText("");
        assertTrue(filter.select(null, null, iipsObject));
    }

    @Test
    public void testStaringString_WithoutSpecialCharacter() {
        filter.setSearchText("Tarif");
        assertTrue(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testSubString_WithoutSpecialCharacter() {
        filter.setSearchText("rif");
        assertFalse(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testWildcatsWithOutStarsAndQuestionMarks() {
        filter.setSearchText("!");
        assertFalse(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testQuestionMarks_Start() {
        filter.setSearchText("?a");
        assertTrue(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testQuestionMarks_StartAndMiddle() {
        filter.setSearchText("?a?i");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("?b?i");
        assertFalse(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testQuestionMarks_NotStart() {
        filter.setSearchText("t?");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("r?");
        assertFalse(filter.select(viewer, parentObject, ipsObject));
    }

    @Test
    public void testObjectStar_Start() {
        filter.setSearchText("T*f*");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("T*b*");
        assertFalse(filter.select(viewer, parentObject, ipsObject));

    }

    @Test
    public void testObjectStar_NotStart() {
        filter.setSearchText("*ar*f*");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("*af*b*");
        assertFalse(filter.select(viewer, parentObject, ipsObject));

    }

    @Test
    public void testCamelCase_OnlyUpperCase() {
        filter.setSearchText("TH");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("TO");
        assertFalse(filter.select(viewer, parentObject, ipsObject));

    }

    @Test
    public void testCamelCase_UpperAndLowerCase() {
        filter.setSearchText("TarHa");
        assertTrue(filter.select(viewer, parentObject, ipsObject));

        filter.setSearchText("TarO");
        assertFalse(filter.select(viewer, parentObject, ipsObject));

    }

    @Test
    public void testSelect() throws Exception {

    }
}
