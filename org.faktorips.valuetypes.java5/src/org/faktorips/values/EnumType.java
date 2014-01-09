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

package org.faktorips.values;

/**
 * An EnumType is a Datatype that represents an enumeration of values e.g. gender, payment mode.
 * 
 * @deprecated This interface is deprecated since version 3.9 and will be removed in version 3.10.
 *             Please remove all dependencies to this interface. Therefore create a Faktor-IPS
 *             Enumeration Type for each dependency.
 * 
 * @author Jan Ortmann
 */
@Deprecated
public interface EnumType {

    /**
     * Returns the type's values or an empty array if the type has no values.
     */
    public EnumValue[] getValues();

    /**
     * Returns the type's value ids as string array.
     */
    public String[] getValueIds();

    /**
     * Returns true if the id identifies an EnumValue, otherwise false.
     */
    public boolean containsValue(String id);

    /**
     * Returns the EnumValue identified by the given id. If the id does not represent an EnumValue,
     * <code>null</code> is returned.
     */
    public EnumValue getEnumValue(String id);

    /**
     * Returns the EnumValue at the given index.
     * 
     * @throws IllegalArgumentException if the index is out of bounce.
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the number of possible values
     */
    public int getNumOfValues();

}
