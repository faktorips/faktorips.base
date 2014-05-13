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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class TestAbstractEnumdatatypeBasedFieldTest extends AbstractIpsPluginTest {

    @Test
    public void testInvalidValue() {
        Composite parent = new Composite(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
                SWT.NONE);
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
            super(combo, datatype, false);
            reInitInternal();
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
