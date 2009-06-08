/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

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
public class TestAbstractEnumdatatypeBasedFieldTest extends AbstractIpsPluginTest {

    public void testInvalidValue() {
        Composite parent = new Composite(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.NONE);
        Combo combo = new Combo(parent, SWT.READ_ONLY);
        
        MyField field = new MyField(combo, Datatype.INTEGER);
        
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
        
        @Override
        public String getDisplayTextForValue(String id) {
            return id;
        }

        @Override
        protected List<String> getDatatypeValueIds() {
            ArrayList<String> ids = new ArrayList<String>();
            ids.add("1");
            return ids;
        }
    }
}
