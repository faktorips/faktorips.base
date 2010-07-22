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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * 
 * @author Jan Ortmann
 */
public class EnumValueSetFieldTest extends AbstractIpsPluginTest {

    private EnumDatatype datatype;
    private IEnumValueSet valueSet;
    private EnumValueSetField field;
    private String nullRepresentation;
    private Shell shell;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(project, "Policy");
        IPolicyCmptTypeAttribute a = type.newPolicyCmptTypeAttribute();
        datatype = new PaymentMode();
        a.setValueSetType(ValueSetType.ENUM);
        valueSet = (IEnumValueSet)a.getValueSet();

        nullRepresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
        assertNotNull(shell);
    }

    public void testWithDisplayTypeId_ValueSetDoesntContainNull() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.ID);
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumValueSetField(c, valueSet, datatype);

        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(PaymentMode.ANNUAL_ID, items[0]);
        assertEquals(PaymentMode.MONTHLY_ID, items[1]);
        assertEquals(nullRepresentation, items[2]);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_ID, field.getText());

        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());

        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(PaymentMode.ANNUAL_ID, items[0]);
        assertEquals(PaymentMode.MONTHLY_ID, items[1]);
        assertEquals(nullRepresentation, items[2]);
        assertEquals("unknownValue", items[3]);

        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }

    public void testWithDisplayTypeId_ValueSetContainsNull() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.ID);
        valueSet.addValue(null);
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumValueSetField(c, valueSet, datatype);

        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_ID, items[1]);
        assertEquals(PaymentMode.MONTHLY_ID, items[2]);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_ID, field.getText());

        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());

        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(nullRepresentation, items[0]);
        assertEquals(PaymentMode.ANNUAL_ID, items[1]);
        assertEquals(PaymentMode.MONTHLY_ID, items[2]);
        assertEquals("unknownValue", items[3]);

        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }

    public void testWithDisplayTypeName() {
        IpsPlugin.getDefault().getIpsPreferences().setEnumTypeDisplay(EnumTypeDisplay.NAME);
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumValueSetField(c, valueSet, datatype);

        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(PaymentMode.ANNUAL_NAME, items[0]);
        assertEquals(PaymentMode.MONTHLY_NAME, items[1]);
        assertEquals(nullRepresentation, items[2]);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(PaymentMode.ANNUAL_NAME, field.getText());

        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());

        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(PaymentMode.ANNUAL_NAME, items[0]);
        assertEquals(PaymentMode.MONTHLY_NAME, items[1]);
        assertEquals(nullRepresentation, items[2]);
        assertEquals("unknownValue", items[3]);

        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }

    public void testWithDisplayTypeNameAndId() {
        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
        DatatypeFormatter formatter = prefs.getDatatypeFormatter();
        valueSet.addValue(PaymentMode.ANNUAL_ID);
        valueSet.addValue(PaymentMode.MONTHLY_ID);
        prefs.setEnumTypeDisplay(EnumTypeDisplay.NAME_AND_ID);
        Combo c = new Combo(shell, SWT.READ_ONLY);
        field = new EnumValueSetField(c, valueSet, datatype);

        String annualNameAndId = formatter.formatValue(datatype, PaymentMode.ANNUAL_ID);
        String monthlyNameAndId = formatter.formatValue(datatype, PaymentMode.MONTHLY_ID);

        String[] items = field.getCombo().getItems();
        assertEquals(3, items.length);
        assertEquals(annualNameAndId, items[0]);
        assertEquals(monthlyNameAndId, items[1]);
        assertEquals(nullRepresentation, items[2]);

        field.setValue(PaymentMode.ANNUAL_ID);
        assertEquals(PaymentMode.ANNUAL_ID, field.getValue());
        assertEquals(annualNameAndId, field.getText());

        field.setValue(nullRepresentation);
        assertEquals(null, field.getValue());
        assertEquals(nullRepresentation, field.getText());

        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
        assertEquals(annualNameAndId, items[0]);
        assertEquals(monthlyNameAndId, items[1]);
        assertEquals(nullRepresentation, items[2]);
        assertEquals("unknownValue", items[3]);

        field.setValue(PaymentMode.ANNUAL_ID);
        field.setValue("unknownValue");
        items = c.getItems();
        assertEquals(4, items.length);
    }

}
