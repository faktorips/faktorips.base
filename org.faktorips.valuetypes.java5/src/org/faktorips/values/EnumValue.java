/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

/**
 * An <code>EnumValue</code> represents a value in an enum type, e.g. male and female are values in
 * the type gender.
 * <p>
 * Two EnumValues are considered equal if they belong to the same type and have the same id.
 * 
 * @deprecated This interface is deprecated since version 3.9 and will be removed in version 3.10.
 *             Please remove all dependencies to this interface. Therefore create a Faktor-IPS
 *             Enumeration Type for each {@link EnumType} this interface is related.
 * 
 * @author Jan Ortmann
 */
@Deprecated
public interface EnumValue extends Comparable<EnumValue> {

    /**
     * Returns the EnumType this value belongs to.
     */
    public EnumType getType();

    /**
     * Returns the enum value's identfication in the enum type.
     */
    public String getId();

    /**
     * Returns the value's human readable name in the default locale.
     */
    public String getName();

    /**
     * Returns the type's id followed by a dot followed by the value's id, e.g.
     * <code>Gender.male</code>
     */
    public abstract String toString();

}
