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
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * An edit field for the Combo control whose items are derived from an Java Enum (introduced in Java
 * 5.0).
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
        String[] allEnumValues = new String[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            allEnumValues[i] = enumConstants[i].toString();
        }
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
        getCombo().setText(newValue == null ? "" : newValue.toString()); //$NON-NLS-1$
    }

    @Override
    public void setValue(T newValue) {
        if (javaEnumClass.isAssignableFrom(newValue.getClass())) {
            setEnumValue(javaEnumClass.cast(newValue));
        }
    }
}
