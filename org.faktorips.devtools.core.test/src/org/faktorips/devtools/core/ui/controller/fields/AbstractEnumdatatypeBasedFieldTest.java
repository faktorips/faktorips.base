/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Thorsten Guenther
 */
public class AbstractEnumdatatypeBasedFieldTest extends AbstractIpsPluginTest {

    public void testInvalidValue() {
        Composite parent = new Composite(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.NONE);
        Combo combo = new Combo(parent, SWT.READ_ONLY);
        
        MyField field = new MyField(combo, Datatype.INTEGER);
        field.init();
        
        assertNull(field.getInvalidValue());
        field.setValue("1");
        assertNull(field.getInvalidValue());

        field.setValue("2");
        assertEquals("2", field.getInvalidValue());
    }
    
    private class MyField extends AbstractEnumDatatypeBasedField {

        public MyField(Combo combo, ValueDatatype datatype) {
            super(combo, datatype);
        }

        protected void reInitInternal() {
        }
        
        public String getInvalidValue() {
            return super.getInvalidValue();
        }
        
        public void init() {
            super.initialize(new String[] {"1"}, new String[0]);
        }
    }
}
