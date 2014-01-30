/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controller.fields.BooleanComboField;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class BooleanComboFieldTest {

    private Shell shell;

    @Before
    public void setUp() throws Exception {
        this.shell = new Shell(Display.getCurrent());
    }

    public void tearDownExtension() {
        shell.dispose();
    }

    @Test
    public void testSetText() {
        Combo c = new Combo(shell, SWT.READ_ONLY);
        c.setItems(new String[] { "true", "false" });
        BooleanComboField field = new BooleanComboField(c, "true", "false");

        field.setText("false");
        assertEquals("false", field.getValue()); // in this case get value returns a string
        assertTrue(field.isTextContentParsable());

        field.setText("true");
        assertEquals("true", field.getValue());
        assertTrue(field.isTextContentParsable());

        field.setText("unkown");
        assertEquals("false", field.getValue());
        assertTrue(field.isTextContentParsable());

        c = new Combo(shell, SWT.READ_ONLY);
        c.setItems(new String[] { "<null>", "Yes", "No" });
        field = new BooleanComboField(c, "Yes", "No");

        field.setText("Yes");
        assertEquals("true", field.getValue());

        field.setText("No");
        assertEquals("false", field.getValue());

        field.setText("unknown");
        assertEquals("false", field.getValue());

        field.setText("<null>");
        assertEquals(null, field.getValue());
    }

    @Test
    public void testSetValue() {
        Combo c = new Combo(shell, SWT.READ_ONLY);
        c.setItems(new String[] { "true", "false" });
        BooleanComboField field = new BooleanComboField(c, "true", "false");

        field.setValue("false");
        assertEquals("false", field.getValue()); // in this case get value returns a string
        assertTrue(field.isTextContentParsable());

        field.setValue("true");
        assertEquals("true", field.getValue());
        assertTrue(field.isTextContentParsable());

        field.setValue("unkown");
        assertEquals("false", field.getValue());
        assertTrue(field.isTextContentParsable());
    }

}
