/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.enums;

/**
 * Default implementation of <tt>EnumValue</tt>.
 * 
 * @see EnumValue
 */
public class DefaultEnumValue implements EnumValue {

    private DefaultEnumType type;

    private String id;

    private String name;

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

    @Override
    public EnumType getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
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
        return type.toString() + '.' + id;
    }

    @Override
    public int compareTo(Object o) {
        EnumValue other = (EnumValue)o;
        return id.compareTo(other.getId());
    }

}
