/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for a combo box.
 */
public class ComboFieldEditor extends FieldEditor {

    /**
     * The <code>Combo</code> widget.
     */
    private Combo combo;

    /**
     * The value (not the name) of the currently selected item in the Combo widget.
     */
    private String valueString;

    /**
     * The names (labels) and underlying values to populate the combo widget. These should be
     * arranged as: { {name1, value1}, {name2, value2}, ...}
     */
    private String[][] entryNamesAndValues;

    public ComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
        init(name, labelText);
        Assert.isTrue(checkArray(entryNamesAndValues));
        this.entryNamesAndValues = entryNamesAndValues;
        createControl(parent);
    }

    /**
     * Checks whether given <code>String[][]</code> is of "type" <code>String[][2]</code>.
     * 
     * @return <code>true</code> if it is ok, and <code>false</code> otherwise
     */
    private boolean checkArray(String[][] table) {
        if (table == null) {
            return false;
        }
        for (String[] array : table) {
            if (array == null || array.length != 2) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData)this.combo.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        /*
         * We only grab excess space if we have to If another field editor has more columns then we
         * assume it is setting the width.
         */
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);
        getComboBoxControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        combo.setLayoutData(gd);
    }

    @Override
    protected void doLoad() {
        updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
    }

    @Override
    protected void doLoadDefault() {
        updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
    }

    @Override
    protected void doStore() {
        if (valueString == null) {
            getPreferenceStore().setToDefault(getPreferenceName());
            return;
        }
        getPreferenceStore().setValue(getPreferenceName(), valueString);
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Lazily create and return the Combo control.
     */
    public Combo getComboBoxControl(Composite parent) {
        if (combo == null) {
            combo = new Combo(parent, SWT.READ_ONLY);
            for (int i = 0; i < entryNamesAndValues.length; i++) {
                combo.add(entryNamesAndValues[i][0], i);
            }
            combo.setFont(parent.getFont());
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent evt) {
                    String oldValue = valueString;
                    String name = combo.getText();
                    valueString = getValueForName(name);
                    setPresentsDefaultValue(false);
                    fireValueChanged(VALUE, oldValue, valueString);
                }
            });
        }
        return combo;
    }

    /**
     * Given the name (label) of an entry, return the corresponding value.
     */
    protected String getValueForName(String name) {
        for (String[] entry : entryNamesAndValues) {
            if (name.equals(entry[0])) {
                return entry[1];
            }
        }
        return entryNamesAndValues[0][0];
    }

    /**
     * Set the name in the combo widget to match the specified value.
     */
    protected void updateComboForValue(String value) {
        valueString = value;
        for (String[] entryNamesAndValue : entryNamesAndValues) {
            if (value.equals(entryNamesAndValue[1])) {
                combo.setText(entryNamesAndValue[0]);
                return;
            }
        }
        if (entryNamesAndValues.length > 0) {
            valueString = entryNamesAndValues[0][1];
            combo.setText(entryNamesAndValues[0][0]);
        }
    }

}
