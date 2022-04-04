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
 * A value datatype representing an enumeration of values.
 */
public interface EnumDatatype extends NamedDatatype {

    /**
     * Returns the IDs of all values defined in the enum type.
     * 
     * @param includeNull {@code true} to get the ID for the NULL-Value included, {@code false} for
     *            not include the NULL-Value. Note that the NULL-Value can be the Java {@code null}
     *            or a special case NULL-value ID.
     */
    public String[] getAllValueIds(boolean includeNull);

}
