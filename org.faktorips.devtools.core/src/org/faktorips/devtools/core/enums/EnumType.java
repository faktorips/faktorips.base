/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.enums;

/**
 * An EnumType is a data type that represents an enumeration of values e.g. gender, payment mode.
 *
 * @deprecated Deprecated since we use Java 5 enums. <br>
 *             Important notice: To migrate enums using this interface to native Java 5 enums, you
 *             need to provide an additional ID. Be careful with the refactoring because some
 *             framework components may use the ID for example to persist the enum value to XML.
 *
 */
@Deprecated
public interface EnumType {

    /**
     * Returns the type's values or an empty array if the type has no values.
     */
    public EnumValue[] getValues();

    /**
     * Returns the type's value IDs as string array.
     */
    public String[] getValueIds();

    /**
     * Returns <tt>true</tt> if the ID identifies an <tt>EnumValue</tt>, otherwise <tt>false</tt>.
     */
    public boolean containsValue(String id);

    /**
     * Returns the <tt>EnumValue</tt> identified by the given ID. If the ID does not represent an
     * <tt>EnumValue</tt>, <code>null</code> will be returned.
     */
    public EnumValue getEnumValue(String id);

    /**
     * Returns the <tt>EnumValue</tt> at the given index.
     *
     * @throws IllegalArgumentException If the index is out of bounds.
     */
    public EnumValue getEnumValue(int index);

    /**
     * Returns the number of values.
     */
    public int getNumOfValues();

}
