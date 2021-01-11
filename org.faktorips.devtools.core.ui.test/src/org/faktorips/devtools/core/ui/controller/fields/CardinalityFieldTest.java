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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class CardinalityFieldTest {

    private Shell shell;

    @Before
    public void setUp() {
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
    }

    @Test
    public void testSetText() {
        Text text = new Text(shell, SWT.NONE);
        CardinalityField field = new CardinalityField(text);

        field.setText("1");
        assertEquals("1", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(Integer.valueOf(1), field.getValue());

        field.setText("*");
        assertEquals("*", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), field.getValue());

        field.setText("a");
        assertEquals("a", field.getText());
        assertFalse(field.isTextContentParsable());
        assertNull(field.getValue());
    }

    @Test
    public void testSetValue() {
        Text text = new Text(shell, SWT.NONE);
        CardinalityField field = new CardinalityField(text);

        field.setValue(Integer.valueOf(1));
        assertEquals("1", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(Integer.valueOf(1), field.getValue());

        field.setValue(Integer.valueOf(Integer.MAX_VALUE));
        assertEquals("*", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), field.getValue());
    }

}
