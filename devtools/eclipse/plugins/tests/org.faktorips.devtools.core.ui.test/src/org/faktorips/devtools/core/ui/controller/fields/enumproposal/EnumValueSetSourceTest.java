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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class EnumValueSetSourceTest {


    @Mock
    private IValueSetOwner valueSetOwner;

    @Mock
    private IValueSet valueSet;

    @Mock
    private EnumValueSet enumValueSet;

    private MockitoSession mockito;

    private EnumValueSetSource valueSource;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        valueSource = new EnumValueSetSource(valueSetOwner);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testIsApplicable_true() {
        when(valueSetOwner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);

        assertTrue(valueSource.isApplicable());
    }

    @Test
    public void testIsApplicable_false() {
        when(valueSetOwner.getValueSet()).thenReturn(valueSet);

        boolean isApplicable = valueSource.isApplicable();

        assertFalse(isApplicable);
    }

    @Test
    public void testIsApplicable_ValueSetOwnerIsNull() {
        EnumValueSetSource valueSourceNull = new EnumValueSetSource(null);

        boolean isApplicable = valueSourceNull.isApplicable();

        assertFalse(isApplicable);
    }

    @Test
    public void testGetValues_EmptyList() {
        when(valueSetOwner.getValueSet()).thenReturn(valueSet);

        List<String> values = valueSource.getValues();

        assertNotNull(values);
        assertEquals(0, values.size());
    }

    @Test
    public void testGetValues() {
        when(valueSetOwner.getValueSet()).thenReturn(enumValueSet);
        when(enumValueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);
        when(enumValueSet.getValuesAsList()).thenReturn(Arrays.asList("value1", "value2"));

        List<String> values = valueSource.getValues();

        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

}
