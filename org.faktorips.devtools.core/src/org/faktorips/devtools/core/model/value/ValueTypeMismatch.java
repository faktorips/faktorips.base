/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.value;

/**
 * Enum for the result of the check of {@link ValueType}
 * 
 * @author frank
 * @since 3.9
 */
public enum ValueTypeMismatch {

    NO_MISMATCH,
    STRING_TO_INTERNATIONAL_STRING,
    INTERNATIONAL_STRING_TO_STRING;

    public static ValueTypeMismatch getMismatch(IValue<?> value, boolean multilingual) {
        if (ValueType.getValueType(value).equals(ValueType.STRING) && multilingual) {
            return STRING_TO_INTERNATIONAL_STRING;
        } else if (ValueType.getValueType(value).equals(ValueType.INTERNATIONAL_STRING) && !multilingual) {
            return INTERNATIONAL_STRING_TO_STRING;
        } else {
            return NO_MISMATCH;
        }
    }
}