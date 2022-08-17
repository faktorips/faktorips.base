/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumDatatypeValueSourceTest {

    @Mock
    private ValueDatatype valueDatatype;

    @Mock
    private EnumDatatype enumDatatype;

    private EnumDatatypeValueSource valueSource;

    @Mock
    private IValueSetOwner valueSetOwner;

    // @Before
    // public void setUp() {
    // valueSource = new EnumDatatypeValueSource(datatype);
    // }

    @Test
    public void testIsApplicable_true() {
        valueSource = new EnumDatatypeValueSource(enumDatatype);
        when(enumDatatype.isEnum()).thenReturn(true);

        boolean isApplicable = valueSource.isApplicable();

        assertTrue(isApplicable);
    }

    @Test
    public void testIsApplicable_false() {
        valueSource = new EnumDatatypeValueSource(valueDatatype);
        when(valueDatatype.isEnum()).thenReturn(false);

        boolean isApplicable = valueSource.isApplicable();

        assertFalse(isApplicable);
    }

    @Test
    public void testGetValues_EmptyList() {
        valueSource = new EnumDatatypeValueSource(valueDatatype);
        when(valueDatatype.isEnum()).thenReturn(false);

        List<String> values = valueSource.getValues();

        assertNotNull(values);
        assertEquals(0, values.size());
    }

    @Test
    public void testGetValues() {
        valueSource = new EnumDatatypeValueSource(enumDatatype);
        when(enumDatatype.isEnum()).thenReturn(true);
        when(enumDatatype.getAllValueIds(true)).thenReturn(new String[] { "value1", "value2" });

        List<String> values = valueSource.getValues();

        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

}
