/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
