/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.editors.pctype.ValidationRuleMarkerPMO.MarkerViewItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleMarkerPMOTest {

    @Mock
    private IEnumType markerDefinition;

    @Mock
    private IValidationRule rule;

    private List<IEnumValue> enumValues = new ArrayList<IEnumValue>();

    @Mock
    private IEnumValue value1;

    @Mock
    private IEnumValue value2;

    @Captor
    private ArgumentCaptor<List<String>> stringListCaptor;

    private ValidationRuleMarkerPMO markerPMO;

    @Before
    public void setUp() throws Exception {
        when(value1.getId()).thenReturn("id1");
        when(value2.getId()).thenReturn("id2");
        when(value1.getName()).thenReturn("label1");
        when(value2.getName()).thenReturn("label2");
        enumValues.add(value1);
        enumValues.add(value2);
        when(markerDefinition.getEnumValues()).thenReturn(enumValues);
        markerPMO = new ValidationRuleMarkerPMO(rule, markerDefinition);
    }

    @Test
    public void testGetItems_defaultValues() {
        List<MarkerViewItem> items = markerPMO.getItems();

        assertEquals(2, items.size());
        assertEquals("label1", items.get(0).getLabel());
        assertEquals("id1", items.get(0).getId());
        assertEquals(false, items.get(0).isChecked());

        assertEquals("label2", items.get(1).getLabel());
        assertEquals("id2", items.get(1).getId());
        assertEquals(false, items.get(1).isChecked());
    }

    @Test
    public void testGetItems() {
        when(rule.getMarkers()).thenReturn(Arrays.asList(new String[] { "id1" }));
        markerPMO = new ValidationRuleMarkerPMO(rule, markerDefinition);

        List<MarkerViewItem> items = markerPMO.getItems();

        assertEquals(2, items.size());
        assertTrue(items.get(0).isChecked());
        assertFalse(items.get(1).isChecked());
    }

    @Test
    public void testUpdateCheckedState() {
        when(rule.getMarkers()).thenReturn(Arrays.asList(new String[] { "id1" }));
        doNothing().when(rule).setMarkers(stringListCaptor.capture());

        markerPMO = new ValidationRuleMarkerPMO(rule, markerDefinition);

        List<MarkerViewItem> items = markerPMO.getItems();
        items.get(1).updateCheckedState();

        List<String> markers = stringListCaptor.getValue();
        assertEquals(2, markers.size());
        assertEquals("id1", markers.get(0));
        assertEquals("id2", markers.get(1));
    }
}
