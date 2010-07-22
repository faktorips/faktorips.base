/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.IpsPlugin;

public class TextFieldTest extends TestCase {

    private Shell shell;

    @Override
    protected void setUp() {
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
    }

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

        try {
            field.setText(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

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
