/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.productcmpt.BooleanValueSetControl;
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
    private IConfigElement configElement;

    @Mock
    private IIpsModel ipsModel;

    private String id = "ID";

    private BooleanValueSetField field;

    @Before
    public void setUp() {
        when(control.getTrueCheckBox()).thenReturn(trueBox);
        when(control.getFalseCheckBox()).thenReturn(falseBox);
        when(control.getNullCheckBox()).thenReturn(nullBox);
        when(configElement.getIpsModel()).thenReturn(ipsModel);
        when(ipsModel.getNextPartId(configElement)).thenReturn(id);

    }

    @Test
    public void testParseContent() throws Exception {
        when(trueBox.isChecked()).thenReturn(true);
        when(falseBox.isChecked()).thenReturn(false);
        when(nullBox.isChecked()).thenReturn(true);

        field = new BooleanValueSetField(configElement, control);

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

        field = new BooleanValueSetField(configElement, control);

        IEnumValueSet fieldValueSet = field.parseContent();
        List<String> valuesAsList = fieldValueSet.getValuesAsList();

        assertThat(valuesAsList, hasItem(Boolean.FALSE.toString()));
        assertEquals(1, valuesAsList.size());
    }

}
