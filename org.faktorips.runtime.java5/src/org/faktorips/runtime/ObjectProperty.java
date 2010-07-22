/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.Serializable;

/**
 * An instance of this class identifies a property in an object, e.g. the name property of a
 * specific person.
 */
public class ObjectProperty implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3407760096164658253L;

    private Object object;
    private String property;
    private int index;
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
        hashCode = object.hashCode() + index;
        hashCode = property == null ? hashCode : hashCode + property.hashCode();
    }

    public ObjectProperty(Object object, String property) {
        this(object, property, -1);
    }

    /**
     * Creates an ObjectProperty that characterizes only the object but not a specific property of
     * it.
     */
    public ObjectProperty(Object object) {
        this(object, null, -1);
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
