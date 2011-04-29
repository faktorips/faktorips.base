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
