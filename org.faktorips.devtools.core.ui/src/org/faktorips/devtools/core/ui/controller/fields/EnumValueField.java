/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.enums.EnumValue;

public class EnumValueField extends ComboField<EnumValue> {

    /** array that contains all values used in the the combo box in the same order */
    private EnumValue[] usedEnumValues;

    public EnumValueField(Combo combo, EnumType enumType) {
        super(combo);
        String[] items = combo.getItems();
        EnumValue[] allEnumValues = enumType.getValues();
        usedEnumValues = new EnumValue[items.length];
        for (int i = 0; i < items.length; i++) {
            usedEnumValues[i] = getEnumValue(allEnumValues, items[i]);
            if (usedEnumValues[i] == null) {
                throw new RuntimeException("Not enum value for combo box item " + items[i]); //$NON-NLS-1$
            }
        }
    }

    private EnumValue getEnumValue(EnumValue[] allValues, String name) {
        for (EnumValue allValue : allValues) {
            if (allValue.getName().equals(name)) {
                return allValue;
            }
        }
        return null;
    }

    public EnumValue getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index == -1) {
            return null;
        }
        return usedEnumValues[index];
    }

    @Override
    public EnumValue parseContent() {
        return getEnumValue();
    }

    public void setEnumValue(EnumValue newValue) {
        getCombo().setText(newValue == null ? "" : newValue.getName()); //$NON-NLS-1$
    }

    @Override
    public void setValue(EnumValue newValue) {
        setEnumValue(newValue);
    }

}
