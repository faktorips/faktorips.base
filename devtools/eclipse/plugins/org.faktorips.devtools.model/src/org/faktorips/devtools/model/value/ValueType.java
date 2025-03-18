/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.value;

import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;

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
     * Returns the type of {@link IValue}. if the class is {@code StringValue} the method returns
     * the {@link ValueType} {@link #STRING}. If the class is {@code InternationalStringValue} the
     * method returns the {@link ValueType} {@link #INTERNATIONAL_STRING}. if value is {@code null}
     * {@link ValueType} is {@link #STRING}. Other implementations of {@link IValue} throw an
     * {@link IllegalArgumentException}
     *
     * @param value the IValue
     * @throws IllegalArgumentException if {@link IValue} is not supported
     */
    public static ValueType getValueType(IValue<?> value) {
        return switch (value) {
            case null -> STRING;
            case StringValue $ -> STRING;
            case InternationalStringValue $ -> INTERNATIONAL_STRING;
            default -> throw new IllegalArgumentException("IValue-type not supported: " + value); //$NON-NLS-1$
        };
    }

}
