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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.swt.widgets.Button;
import org.junit.Before;
import org.junit.Test;

public class ButtonFieldTest {

    private ButtonField selectOnTrueField;
    private ButtonField selectOnFalseField;
    private Button button1;
    private Button button2;

    @Before
    public void setUp() {
        button1 = mock(Button.class);
        button2 = mock(Button.class);
        selectOnTrueField = new ButtonField(button1, true);
        selectOnFalseField = new ButtonField(button2, false);
    }

    @Test
    public void selectOnSetValue() {
        selectOnTrueField.setValue(true);
        verify(button1).setSelection(true);

        selectOnFalseField.setValue(false);
        verify(button1).setSelection(true);
    }

    @Test
    public void deselectOnSetValue() {
        selectOnTrueField.setValue(false);
        verify(button1).setSelection(false);

        selectOnFalseField.setValue(true);
        verify(button1).setSelection(false);
    }

    @Test
    public void selectOnSetText() {
        selectOnTrueField.setText("true");
        selectOnTrueField.setText("true");
        verify(button1, times(2)).setSelection(true);

        selectOnFalseField.setText("false");
        selectOnFalseField.setText("???");
        verify(button1, times(2)).setSelection(true);
    }

    @Test
    public void deselectOnSetText() {
        selectOnTrueField.setText("false");
        selectOnTrueField.setText("???");
        verify(button1, times(2)).setSelection(false);

        selectOnFalseField.setText("true");
        selectOnFalseField.setText("true");
        verify(button1, times(2)).setSelection(false);
    }

    @Test
    public void returnTrueOnGetValue() {
        when(button1.getSelection()).thenReturn(true);
        when(button2.getSelection()).thenReturn(false);

        assertTrue(selectOnTrueField.getValue());
        assertTrue(selectOnFalseField.getValue());
    }

    @Test
    public void returnFalseOnGetValue() {
        when(button1.getSelection()).thenReturn(false);
        when(button2.getSelection()).thenReturn(true);

        assertFalse(selectOnTrueField.getValue());
        assertFalse(selectOnFalseField.getValue());
    }

    @Test
    public void returnTrueOnGetText() {
        when(button1.getSelection()).thenReturn(true);
        when(button2.getSelection()).thenReturn(false);

        assertEquals("true", selectOnTrueField.getText());
        assertEquals("true", selectOnFalseField.getText());
    }

    @Test
    public void returnFalseOnGetText() {
        when(button1.getSelection()).thenReturn(false);
        when(button2.getSelection()).thenReturn(true);

        assertEquals("false", selectOnTrueField.getText());
        assertEquals("false", selectOnFalseField.getText());
    }
}
