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

import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;

/**
 * Defines enum types for the {@link IValue}
 * 
 * @author frank
 * @since 3.9
 */
public enum ValueType {

    STRING,
    INTERNATIONAL_STRING;

    /**
     * Returns the type of IValue. if the class is {@link StringValue} the method returns the
     * ValueType STRING. If the class is {@link InternationalStringValue} the method returns the
     * ValueType INTERNATIONAL_STRING. if value is null ValueType is STRING. Other implementation of
     * IValue throws an IllegalArgumentException
     * 
     * @param value the IValue
     * @throws IllegalArgumentException if IValue not supported
     */
    public static ValueType getValueType(IValue<?> value) {
        if (value == null) {
            return STRING;
        }
        if (value instanceof StringValue) {
            return STRING;
        } else if (value instanceof InternationalStringValue) {
            return INTERNATIONAL_STRING;
        }
        throw new IllegalArgumentException("IValue-type not supported: " + value); //$NON-NLS-1$
    }
}
