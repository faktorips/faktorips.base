/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Combo;

/**
 * An edit field for the Combo control whose items are derived from an Java Enum (introduced in Java
 * 5.0).
 * 
 * @author Roman Grutza
 */
public class EnumField<T extends Enum<T>> extends ComboField {

    private List<T> usedEnumValues;
    private final Class<? extends T> javaEnumClass;

    public EnumField(Combo combo, Class<? extends T> javaEnum) {
        super(combo);
        this.javaEnumClass = javaEnum;
        String[] items = combo.getItems();
        T[] allEnumConstants = javaEnum.getEnumConstants();

        usedEnumValues = new ArrayList<T>();
        for (String item : items) {
            T enumValue = getEnumValue(allEnumConstants, item);
            if (enumValue == null) {
                throw new RuntimeException("Not enum value for combo box item " + enumValue); //$NON-NLS-1$
            } else {
                usedEnumValues.add(enumValue);
            }
        }
    }

    private T getEnumValue(T[] allEnumConstants, String name) {
        for (T allEnumConstant : allEnumConstants) {
            if (allEnumConstant.toString().equals(name)) {
                return allEnumConstant;
            }
        }
        return null;
    }

    public T getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index == -1) {
            return null;
        }
        return usedEnumValues.get(index);
    }

    @Override
    public Object parseContent() {
        return getEnumValue();
    }

    public void setEnumValue(Enum<?> newValue) {
        getCombo().setText(newValue == null ? "" : newValue.toString()); //$NON-NLS-1$
    }

    @Override
    public void setValue(Object newValue) {
        if (javaEnumClass.isAssignableFrom(newValue.getClass())) {
            setEnumValue(javaEnumClass.cast(newValue));
        }
    }
}
