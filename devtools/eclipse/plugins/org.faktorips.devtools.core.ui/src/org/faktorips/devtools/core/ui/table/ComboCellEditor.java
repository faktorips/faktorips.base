/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.Arrays;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controlfactories.BooleanControlFactory;

/**
 * 
 * @author Stefan Widmaier
 */
public class ComboCellEditor extends IpsCellEditor {

    private Combo comboControl;

    public ComboCellEditor(Combo comboControl) {
        super(comboControl);
        this.comboControl = comboControl;
    }

    /**
     * Returns the text of the currently selected item in the combobox (which is always a String).
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        Object data = comboControl.getData();
        if (data instanceof BooleanDatatype || data instanceof PrimitiveBooleanDatatype) {
            if (comboControl.getText().equals(BooleanControlFactory.getTrueValue())) {
                return Boolean.TRUE.toString();
            } else if (comboControl.getText()
                    .equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                return null;
            } else {
                return Boolean.FALSE.toString();
            }
        } else {
            return comboControl.getText();
        }
    }

    @Override
    protected void doSetFocus() {
        comboControl.setFocus();
    }

    /**
     * Selects the item in the combobox' list of items that equals the given value and thereby sets
     * the current text of the combobox to the given value. {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object value) {
        if ((comboControl != null) && (value instanceof String || value == null)) {
            Object data = comboControl.getData();
            if (data instanceof BooleanDatatype || data instanceof PrimitiveBooleanDatatype) {
                if (value == null) {
                    // TODO pk 08-06-2009 there is missing something
                    IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                } else if (Boolean.TRUE.toString().equals(value)) {
                    comboControl.select(getIndexForValue(BooleanControlFactory.getTrueValue()));
                } else if (Boolean.FALSE.toString().equals(value)) {
                    comboControl.select(getIndexForValue(BooleanControlFactory.getFalseValue()));
                } else {
                    comboControl.select(getIndexForValue((String)value));
                }
            } else {
                comboControl.select(getIndexForValue((String)value));
            }
        } else if (comboControl != null) {
            comboControl.select(0);
        }
    }

    @Override
    public boolean isMappedValue() {
        return true;
    }

    /*
     * Searches the combo's list of items for the given text and returns the first index at which an
     * equal item is found. If the given text cannot be found -1 is returned.
     */
    private int getIndexForValue(String text) {
        return Arrays.asList(comboControl.getItems()).indexOf(text);
    }
}
