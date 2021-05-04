/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Arrays;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.model.INamedValue;

/**
 * An edit field for the Combo control whose items are derived from an Java Enum (introduced in Java
 * 5.0).
 * <p>
 * Instead of using this {@link EnumField} it is better to use a {@link ComboViewerField}, because
 * there can be used a labelProvider.
 * 
 * @see EditField for details about generic type T
 * 
 * @author Roman Grutza
 * @author Stefan Widmaier
 */
public class EnumField<T extends Enum<T>> extends ComboField<T> {

    private T[] usedEnumValues;
    private final Class<? extends T> javaEnumClass;

    public EnumField(Combo combo, Class<T> javaEnum) {
        super(combo);
        this.javaEnumClass = javaEnum;
        initComboItems(combo, javaEnum);
    }

    public EnumField(Combo combo, T[] enumValues) {
        super(combo);
        @SuppressWarnings("unchecked")
        /*
         * getComponentType() is native. Thus it can't return the correct type. At this point we
         * know that the arrays elements must be of type T. The cast can be made without problems.
         */
        Class<T> clazz = (Class<T>)enumValues.getClass().getComponentType();
        javaEnumClass = clazz;
        initComboItems(combo, enumValues);
    }

    protected void initComboItems(Combo combo, Class<T> clazz) {
        T[] enumConstants = clazz.getEnumConstants();
        initComboItems(combo, enumConstants);
    }

    protected void initComboItems(Combo combo, T[] enumConstants) {
        usedEnumValues = enumConstants;
        String[] allEnumValues = Arrays.stream(enumConstants)
                .map(INamedValue::getName)
                .toArray(String[]::new);
        combo.setItems(allEnumValues);
    }

    public T getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index == -1) {
            return null;
        }
        return usedEnumValues[index];
    }

    @Override
    public T parseContent() {
        return getEnumValue();
    }

    public void setEnumValue(Enum<?> newValue) {
        getCombo().setText(newValue == null
                ? "" //$NON-NLS-1$
                : INamedValue.getName(newValue));
    }

    @Override
    public void setValue(T newValue) {
        if (javaEnumClass.isAssignableFrom(newValue.getClass())) {
            setEnumValue(javaEnumClass.cast(newValue));
        }
    }
}
