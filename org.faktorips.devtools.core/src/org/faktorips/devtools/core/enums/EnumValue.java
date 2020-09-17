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
 * An <code>EnumValue</code> represents a value in an <code>EnumType</code>, e.g. male and female are
 * values in the type gender.
 * <p>
 * Two <code>EnumValue</code>s are considered equal if they belong to the same type and have the same
 * ID.
 * 
 * @author Jan Ortmann
 * 
 * @deprecated We used this kind of EnumValue before Java 5 enums were introduced.
 */
@Deprecated
public interface EnumValue extends Comparable<Object> {

    /**
     * Returns the <code>EnumType</code> this value belongs to.
     */
    public EnumType getType();

    /**
     * Returns the <code>EnumValue</code>'s s identification in the <code>EnumType</code>.
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
