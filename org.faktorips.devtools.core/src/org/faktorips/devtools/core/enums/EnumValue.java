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

package org.faktorips.devtools.core.enums;

/**
 * An <code>EnumValue</code> represents a value in an <tt>EnumType</tt>, e.g. male and female are
 * values in the type gender.
 * <p>
 * Two <tt>EnumValue</tt>s are considered equal if they belong to the same type and have the same
 * ID.
 * 
 * @author Jan Ortmann
 * 
 * @deprecated We used this kind of EnumValue before Java 5 enums were introduced.
 */
@Deprecated
public interface EnumValue extends Comparable<Object> {

    /**
     * Returns the <tt>EnumType</tt> this value belongs to.
     */
    public EnumType getType();

    /**
     * Returns the <tt>EnumValue</tt>'s s identification in the <tt>EnumType</tt>.
     */
    public String getId();

    /**
     * Returns the value's human readable name in the default locale.
     */
    public String getName();

    /**
     * Returns the type's ID followed by a dot followed by the value's ID, e.g.
     * <code>Gender.male</code>
     */
    @Override
    public abstract String toString();

}
