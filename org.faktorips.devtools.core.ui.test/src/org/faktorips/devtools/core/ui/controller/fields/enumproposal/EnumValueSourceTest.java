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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueSourceTest {

    @Mock
    private IValueSetOwner valueSetOwner;
    @Mock
    private IEnumValueSet valueSet;
    @Mock
    private EnumDatatype datatype;
    private EnumValueSource enumValueSource;

    @Before
    public void setUp() {
        enumValueSource = new EnumValueSource(valueSetOwner, datatype);

        when(valueSetOwner.getValueSet()).thenReturn(valueSet);
        when(valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);
        when(datatype.isEnum()).thenReturn(true);

        List<String> valueSetValues = new ArrayList<>();
        valueSetValues.add("eins");
        valueSetValues.add("zwei");
        valueSetValues.add("drei");
        when(valueSet.getValuesAsList()).thenReturn(valueSetValues);

        String[] enumDatatypeValues = new String[] { "A", "BB", "CCC" };
        when(datatype.getAllValueIds(true)).thenReturn(enumDatatypeValues);
    }

    @Test
    public void testGetValues_fromEnumValueSet() {
        List<String> values = enumValueSource.getValues();
        assertThat(values, hasItem("eins"));
        assertThat(values, hasItem("zwei"));
        assertThat(values, hasItem("drei"));
    }

    @Test
    public void testGetValues_fromEnumDatatype() {
        when(valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(false);

        List<String> values = enumValueSource.getValues();
        assertThat(values, hasItem("A"));
        assertThat(values, hasItem("BB"));
        assertThat(values, hasItem("CCC"));
    }

    @Test
    public void testGetValues_noValues() {
        when(valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(false);
        when(datatype.isEnum()).thenReturn(false);

        List<String> values = enumValueSource.getValues();
        assertEquals(0, values.size());
    }
}
