/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

/**
 * Default implementation of enum value.
 * 
 * @deprecated This interface is deprecated since version 3.9 and will be removed in version 3.10.
 *             Please remove all dependencies to this interface. Therefore create a Faktor-IPS
 *             Enumeration Type for each {@link EnumType} this interface is related.
 */
@Deprecated
public class DefaultEnumValue implements EnumValue {

    private final DefaultEnumType type;
    private final String id;
    private final String name;

    public DefaultEnumValue(DefaultEnumType type, String id) {
        this(type, id, id);
    }

    public DefaultEnumValue(DefaultEnumType type, String id, String name) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.id = id;
        this.name = name;
        type.addValue(this);
    }

    public EnumType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EnumValue)) {
            return false;
        }
        EnumValue other = (EnumValue)o;
        return id.equals(other.getId()) && type.equals(other.getType());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return type.toString() + "." + id;
    }

    public int compareTo(EnumValue other) {
        return id.compareTo(other.getId());
    }

}
