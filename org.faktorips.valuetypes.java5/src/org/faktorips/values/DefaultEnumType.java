/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.values;

/**
 * Default implementation of enum type. XXX Changing this to Java Enum would require many changes
 * throughout devtools.core, so we wait till we drop Java 1.4 compatibility.
 */
public class DefaultEnumType implements EnumType {

    private final String name;
    private final Class<?> valueClass; // Java class representing the values.
    private DefaultEnumValue[] values = new DefaultEnumValue[0];

    /**
     * Creates a new enum type.
     * 
     * @param name The type's name.
     * @param valueClass Java class the values are instances of.
     * 
     * @throws IllegalArgumentException if name is null or if the valueClass is not a subclass of
     *             DefaultEnumValue.
     */
    public DefaultEnumType(String name, Class<?> valueClass) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!DefaultEnumValue.class.isAssignableFrom(valueClass)) {
            throw new IllegalArgumentException(valueClass + " is not a subclass of " + DefaultEnumValue.class);
        }
        this.name = name;
        this.valueClass = valueClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    /**
     * Adds the value to the type.
     * 
     * @throws IllegalArgumentException if the type contains already an id with the given id.
     */
    void addValue(DefaultEnumValue newValue) {
        if (containsValue(newValue.getId())) {
            throw new IllegalArgumentException("The enum type " + this + " contains already a value " + newValue);
        }
        DefaultEnumValue[] newValues = new DefaultEnumValue[values.length + 1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = newValue;
        values = newValues;
    }

    public EnumValue[] getValues() {
        DefaultEnumValue[] copy = new DefaultEnumValue[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }

    public String[] getValueIds() {
        String[] ids = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            ids[i] = values[i].getId();
        }
        return ids;
    }

    public boolean containsValue(String id) {
        for (DefaultEnumValue value : values) {
            if (value.getId() == null) {
                if (id == null) {
                    return true;
                }
            } else {
                if (value.getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public EnumValue getEnumValue(String id) throws IllegalArgumentException {
        for (DefaultEnumValue value : values) {
            if (value.getId() == null) {
                if (id == null) {
                    return value;
                }
            } else {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
        }
        return null;
    }

    public boolean contains(Object value) {
        if (!(value instanceof EnumValue)) {
            return false;
        }
        return contains(value);
    }

    public boolean containsNull() {
        return contains((String)null);
    }

    public int getNumOfValues() {
        return values.length;
    }

    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public String toString() {
        return name;
    }

    public Object[] getValues(String[] value) {
        EnumValue[] elements = new EnumValue[value.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = getEnumValue(value[i]);
        }
        return elements;
    }

}
