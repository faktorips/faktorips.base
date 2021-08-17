/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetControl;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BooleanValueSetFieldTest {

    @Mock
    private BooleanValueSetControl control;

    @Mock
    private Checkbox trueBox;

    @Mock
    private Checkbox falseBox;

    @Mock
    private Checkbox nullBox;

    @Mock
    private IConfiguredValueSet configValueSet;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IEnumValueSet enumValue;

    @Mock
    private IIpsProject ipsProject;

    private String id = "ID";

    private BooleanValueSetField field;

    @Before
    public void setUp() {
        when(control.getTrueCheckBox()).thenReturn(trueBox);
        when(control.getFalseCheckBox()).thenReturn(falseBox);
        when(control.getNullCheckBox()).thenReturn(nullBox);
        when(configValueSet.getIpsModel()).thenReturn(ipsModel);
        when(ipsModel.getNextPartId(configValueSet)).thenReturn(id);

        field = new BooleanValueSetField(configValueSet, control);
    }

    @Test
    public void testParseContent() throws Exception {
        when(trueBox.isChecked()).thenReturn(true);
        when(falseBox.isChecked()).thenReturn(false);
        when(nullBox.isChecked()).thenReturn(true);

        IEnumValueSet fieldValueSet = field.parseContent();
        List<String> valuesAsList = fieldValueSet.getValuesAsList();

        assertThat(valuesAsList, hasItem(Boolean.TRUE.toString()));
        assertThat(valuesAsList, hasItem((String)null));
        assertEquals(2, valuesAsList.size());
    }

    @Test
    public void testParseContent_noNullCheckbox() throws Exception {
        when(trueBox.isChecked()).thenReturn(false);
        when(falseBox.isChecked()).thenReturn(true);
        nullBox = null;

        field = new BooleanValueSetField(configValueSet, control);

        IEnumValueSet fieldValueSet = field.parseContent();
        List<String> valuesAsList = fieldValueSet.getValuesAsList();

        assertThat(valuesAsList, hasItem(Boolean.FALSE.toString()));
        assertEquals(1, valuesAsList.size());
    }

    @Test
    public void testSetValue() throws CoreException {
        when(enumValue.getIpsProject()).thenReturn(ipsProject);
        when(enumValue.containsValue(Boolean.TRUE.toString(), ipsProject)).thenReturn(true);
        when(enumValue.containsValue(Boolean.FALSE.toString(), ipsProject)).thenReturn(true);
        when(enumValue.isContainsNull()).thenReturn(true);

        field.setValue(enumValue);

        verify(trueBox).setChecked(true);
        verify(falseBox).setChecked(true);
        verify(nullBox).setChecked(true);
    }

    @Test
    public void testSetValue_NullCheckboxIsNull() throws CoreException {
        when(enumValue.getIpsProject()).thenReturn(ipsProject);
        when(enumValue.containsValue(Boolean.TRUE.toString(), ipsProject)).thenReturn(false);
        when(enumValue.containsValue(Boolean.FALSE.toString(), ipsProject)).thenReturn(true);
        when(control.getNullCheckBox()).thenReturn(null);

        field.setValue(enumValue);

        verify(falseBox).setChecked(true);
        verifyZeroInteractions(nullBox);
    }
}
