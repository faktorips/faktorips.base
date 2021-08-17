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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnsureContainsNullForConfigElementValueSourceTest {
    @Mock
    private IValueSource valueSource;
    @Mock
    private IValueSetOwner owner;
    @Mock
    private ValueDatatype valueDatatype;

    private List<String> valueList;

    @Before
    public void setUp() {
        valueList = new ArrayList<>();
        valueList.add("eins");
        valueList.add("zwei");
        valueList.add("drei");
        when(valueSource.getValues()).thenReturn(valueList);
    }

    @Test
    public void testGetValues_addNull() {
        owner = mock(IConfiguredValueSet.class);
        EnsureContainsNullForConfigElementValueSource ensureContainsNullValueSource = new EnsureContainsNullForConfigElementValueSource(
                owner, valueDatatype, valueSource);
        when(valueDatatype.isPrimitive()).thenReturn(false);
        List<String> values = ensureContainsNullValueSource.getValues();
        assertValues(values);
    }

    @Test
    public void testGetValues_doNotAddNull_forNonConfigElement() {
        when(valueDatatype.isPrimitive()).thenReturn(false);

        EnsureContainsNullForConfigElementValueSource ensureContainsNullValueSource = new EnsureContainsNullForConfigElementValueSource(
                owner, valueDatatype, valueSource);
        List<String> values = ensureContainsNullValueSource.getValues();

        assertEquals(3, values.size());
        assertThat(values, hasItem("eins"));
        assertThat(values, hasItem("zwei"));
        assertThat(values, hasItem("drei"));
    }

    @Test
    public void testGetValues_doNotAddNull_forPrimitiveDatatype() {
        owner = mock(IConfiguredValueSet.class);
        when(valueDatatype.isPrimitive()).thenReturn(true);

        EnsureContainsNullForConfigElementValueSource ensureContainsNullValueSource = new EnsureContainsNullForConfigElementValueSource(
                owner, valueDatatype, valueSource);
        List<String> values = ensureContainsNullValueSource.getValues();
        assertEquals(3, values.size());
        assertThat(values, hasItem("eins"));
        assertThat(values, hasItem("zwei"));
        assertThat(values, hasItem("drei"));
    }

    @Test
    public void testGetValues() {
        valueList.add(null);

        EnsureContainsNullForConfigElementValueSource ensureContainsNullValueSource = new EnsureContainsNullForConfigElementValueSource(
                owner, valueDatatype, valueSource);
        List<String> values = ensureContainsNullValueSource.getValues();
        assertValues(values);
    }

    private void assertValues(List<String> values) {
        assertEquals(4, values.size());
        assertThat(values, hasItem("eins"));
        assertThat(values, hasItem("zwei"));
        assertThat(values, hasItem("drei"));
        assertTrue(values.contains(null));
    }

}
