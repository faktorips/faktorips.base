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
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class EnumerationFieldTest extends AbstractIpsPluginTest {

    private static final String UNKNOWN_VALUE = "unknownValue";
    private static final String NULL = "@myNullValue@";
    private EnumDatatype datatype;
    private IEnumValueSet valueSet;
    private EnumerationField field;
    private Shell shell;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(project, "Policy");
        IPolicyCmptTypeAttribute a = type.newPolicyCmptTypeAttribute();
        datatype = new PaymentMode();
        a.setValueSetType(ValueSetType.ENUM);
        valueSet = (IEnumValueSet)a.getValueSet();
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);

        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
        assertNotNull(shell);
    }

    @Test
    public void testWithDisplayTypeName() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.NAME);
        Text text = new Text(shell, SWT.READ_ONLY);
        field = new EnumerationField(text, datatype, NULL);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_NAME, field.getText());

        field.setValue(NULL);
        assertEquals(null, field.getValue());
        assertEquals(NULL, field.getText());

        field.setValue(UNKNOWN_VALUE);
        assertEquals(UNKNOWN_VALUE, field.getValue());
        assertEquals(UNKNOWN_VALUE, field.getText());
    }

    @Test
    public void testWithDisplayTypeId() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.ID);
        Text text = new Text(shell, SWT.READ_ONLY);
        field = new EnumerationField(text, datatype, NULL);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_ID, field.getText());

        field.setValue(NULL);
        assertEquals(null, field.getValue());
        assertEquals(NULL, field.getText());

        field.setValue(UNKNOWN_VALUE);
        assertEquals(UNKNOWN_VALUE, field.getValue());
        assertEquals(UNKNOWN_VALUE, field.getText());
    }

    @Test
    public void testWithDisplayTypeNameAndId() {
        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
        UIDatatypeFormatter formatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        prefs.setEnumTypeDisplay(EnumTypeDisplay.NAME_AND_ID);
        Text text = new Text(shell, SWT.READ_ONLY);
        field = new EnumerationField(text, datatype, NULL);

        String annualNameAndId = formatter.formatValue(datatype, PaymentMode.ANNUAL_ID);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(annualNameAndId, field.getText());

        field.setValue(NULL);
        assertEquals(null, field.getValue());
        assertEquals(NULL, field.getText());

        field.setValue(UNKNOWN_VALUE);
        assertEquals(UNKNOWN_VALUE, field.getValue());
        assertEquals(UNKNOWN_VALUE, field.getText());
    }

}
