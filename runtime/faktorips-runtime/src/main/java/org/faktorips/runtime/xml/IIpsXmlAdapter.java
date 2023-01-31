/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml;

/**
 * Adapts a Java type for XML marshaling.
 * 
 * @param <BoundType> the Java type to be mapped
 * @param <ValueType> the type to be used in the XML
 */
public interface IIpsXmlAdapter<ValueType, BoundType> {

    /**
     * Convert a value type to a bound type.
     *
     * @param v The value to be converted. Can be null.
     * @throws Exception if there's an error during the conversion.
     */
    BoundType unmarshal(ValueType v) throws Exception;

    /**
     * Convert a bound type to a value type.
     *
     * @param v The value to be converted. Can be null.
     * @throws Exception if there's an error during the conversion.
     */
    ValueType marshal(BoundType v) throws Exception;
}
