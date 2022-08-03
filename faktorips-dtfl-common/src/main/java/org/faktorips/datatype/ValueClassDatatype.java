/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * A datatype that represents a Java class representing a value, for example java.lang.String.
 * 
 * @deprecated since 3.17 the datatype does not need to know its java class name. All concerns of
 *                 the generated code are part of the corresponding datatype helper. You could
 *                 simply use the supertype {@link ValueClassNameDatatype} to have the same
 *                 functionality.
 * 
 */
@Deprecated
public abstract class ValueClassDatatype extends ValueClassNameDatatype {

    /**
     * Creates a new instance using the given type. The name is provided from the given class name.
     * 
     * @param clazz The java class that should be represented by this datatype
     */
    public ValueClassDatatype(Class<?> clazz) {
        super(clazz.getSimpleName());
    }

    /**
     * Creates a new instance using the given class and name.
     * 
     * @param clazz The java class that should be represented by this datatype
     * @param name The name of the datatype
     */
    public ValueClassDatatype(Class<?> clazz, String name) {
        super(name);
    }

}
