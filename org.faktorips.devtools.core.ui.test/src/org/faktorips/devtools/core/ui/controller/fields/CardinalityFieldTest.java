/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Jan Ortmann
 */
public class CardinalityFieldTest extends TestCase {

    private Shell shell;

    @Override
    protected void setUp() {
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
    }

    public void testSetText() {
        Text text = new Text(shell, SWT.NONE);
        CardinalityField field = new CardinalityField(text);

        field.setText("1");
        assertEquals("1", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(new Integer(1), field.getValue());

        field.setText("*");
        assertEquals("*", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(new Integer(Integer.MAX_VALUE), field.getValue());

        field.setText("a");
        assertEquals("a", field.getText());
        assertFalse(field.isTextContentParsable());
        assertNull(field.getValue());
    }

    public void testSetValue() {
        Text text = new Text(shell, SWT.NONE);
        CardinalityField field = new CardinalityField(text);

        field.setValue(new Integer(1));
        assertEquals("1", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(new Integer(1), field.getValue());

        field.setValue(new Integer(Integer.MAX_VALUE));
        assertEquals("*", field.getText());
        assertTrue(field.isTextContentParsable());
        assertEquals(new Integer(Integer.MAX_VALUE), field.getValue());
    }

}
