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

package org.faktorips.util.message;

/**
 * An instance of this class identifies a property in an object, e.g. the name property of a
 * specific person.
 */
public class ObjectProperty {

    private final Object object;
    private final String property;
    private final int index;
    private int hashCode;

    /**
     * Creates a new ObjectProperty. If the property is a list or an array the index can specify the
     * position within the property. An index smaller than 0 indicates that it is not an indexed
     * property.
     */
    public ObjectProperty(Object object, String property, int index) {
        this.object = object;
        this.property = property;
        this.index = index;
        createHashCode();
    }

    private void createHashCode() {
        hashCode = (object == null ? 0 : object.hashCode()) + index;
        hashCode = property == null ? hashCode : hashCode + property.hashCode();
    }

    public ObjectProperty(Object object, String property) {
        this(object, property, -1);
    }

    public Object getObject() {
        return object;
    }

    public String getProperty() {
        return property;
    }

    public int getIndex() {
        return index;
    }

    public boolean hasIndex() {
        return index >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectProperty) {
            ObjectProperty other = (ObjectProperty)obj;
            return object.equals(other.object)
                    && index == other.index
                    && ((property == null && other.property == null) || (property != null && other.property != null && property
                            .equals(other.property)));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        if (object == null) {
            return "null." + property;
        }
        return object.toString() + "." + property;
    }

}
