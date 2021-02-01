/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * Represents a collection of enum values. This class is used during the process of reading data of
 * enum value sets from an xml representation into memory and create
 * <code>org.faktorips.valueset.EnumValueSet</code> objects.
 * 
 * @author Peter Erzberger
 */
public class EnumValues {

    private String[] values;
    private boolean containsNull;

    /**
     * Creates a new EnumValues object.
     */
    public EnumValues(String[] values, boolean containsNull) {
        super();
        this.values = values;
        this.containsNull = containsNull;
    }

    /**
     * @return Returns of the value null is contained in the values
     */
    public boolean containsNull() {
        return containsNull;
    }

    /**
     * @return Returns the value at the indexed position
     */
    public String getValue(int index) {
        return values[index];
    }

    /**
     * @return Returns the number of values
     */
    public int getNumberOfValues() {
        return values.length;
    }

}
