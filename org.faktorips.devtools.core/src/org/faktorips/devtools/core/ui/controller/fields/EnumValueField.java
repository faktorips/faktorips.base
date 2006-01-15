package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.EnumType;
import org.faktorips.values.EnumValue;


/**
 *
 */
public class EnumValueField extends ComboField {
    
    private EnumType enumType;
    
    /**
     * @param combo
     */
    public EnumValueField(Combo combo, EnumType type) {
        super(combo);
        ArgumentCheck.notNull(type);
        enumType = type;
    }
    
    public EnumValue getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index==-1) {
            return null;
        }
        return enumType.getEnumValue(index);
    }

    public void setEnumValue(EnumValue newValue) {
        getCombo().setText(newValue.getName());
    }
    
    public Object getValue() {
        return getEnumValue();
    }
    
    public void setValue(Object newValue) {
        setEnumValue((EnumValue)newValue);
    }

}
