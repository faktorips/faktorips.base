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
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;
import org.junit.Before;
import org.junit.Test;

public class TextFieldTest {

    private Shell shell;

    @Before
    public void setUp() {
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
    }

    @Test
    public void testSetText() {
        Text text = new Text(shell, SWT.NONE);
        TextField field = new TextField(text);
        field.setText("abc");
        assertEquals("abc", field.getText());
        assertEquals("abc", field.getValue());
        assertTrue(field.isTextContentParsable());

        String nullRep = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        field.setText(nullRep);
        assertEquals(nullRep, field.getText());
        assertEquals(null, field.getValue());
        assertTrue(field.isTextContentParsable());

    }

    @Test
    public void testSetValue() {
        Text text = new Text(shell, SWT.NONE);
        TextField field = new TextField(text);

        field.setValue("abc");
        assertEquals("abc", field.getText());
        assertEquals("abc", field.getValue());
        assertTrue(field.isTextContentParsable());

        String nullRep = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        field.setValue(nullRep);
        assertEquals(nullRep, field.getText());
        assertEquals(null, field.getValue());
        assertTrue(field.isTextContentParsable());

        field.setValue(null);
        assertEquals(nullRep, field.getText());
        assertEquals(null, field.getValue());
        assertTrue(field.isTextContentParsable());
    }
}
