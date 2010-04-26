/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.eclipse.swt.widgets.Combo;

/**
 * An edit field for the Combo control whose items are derived from an Java Enum (introduced in Java
 * 5.0).
 * 
 * @author Roman Grutza
 */
public class EnumField extends ComboField {

    private Enum[] usedEnumValues;

    public EnumField(Combo combo, Class<? extends Enum> javaEnum) {
        super(combo);
        String[] items = combo.getItems();
        Enum[] allEnumConstants = javaEnum.getEnumConstants();

        usedEnumValues = new Enum[items.length];
        for (int i = 0; i < items.length; i++) {
            usedEnumValues[i] = getEnumValue(allEnumConstants, items[i]);
            if (usedEnumValues[i] == null) {
                throw new RuntimeException("Not enum value for combo box item " + items[i]); //$NON-NLS-1$
            }
        }
    }

    private Enum getEnumValue(Enum[] allEnumConstants, String name) {
        for (Enum allEnumConstant : allEnumConstants) {
            if (allEnumConstant.toString().equals(name)) {
                return allEnumConstant;
            }
        }
        return null;
    }

    public Enum getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index == -1) {
            return null;
        }
        return usedEnumValues[index];
    }

    @Override
    public Object parseContent() {
        return getEnumValue();
    }

    public void setEnumValue(Enum newValue) {
        getCombo().setText(newValue == null ? "" : newValue.toString());
    }

    @Override
    public void setValue(Object newValue) {
        setEnumValue((Enum)newValue);
    }
}
