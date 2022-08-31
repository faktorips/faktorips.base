/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.junit.After;
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

    private AutoCloseable openMocks;

    @Before
    public void setUp() throws Exception {
        openMocks = MockitoAnnotations.openMocks(this);
        ILabelProvider labelProvider = mock(ILabelProvider.class);
        when(viewer.getLabelProvider()).thenReturn(labelProvider);
        when(labelProvider.getText(ipsObject)).thenReturn(existingIpsElementName);
        filter = new TypeSelectionFilter();
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
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
