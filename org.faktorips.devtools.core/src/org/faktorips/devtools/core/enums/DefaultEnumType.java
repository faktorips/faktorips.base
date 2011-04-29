/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.enums;

/**
 * Default implementation of <tt>EnumType</tt>.
 * 
 * @see EnumType
 */
public class DefaultEnumType implements EnumType {

    private String name;

    private Class<?> valueClass; // Java class representing the values.

    private DefaultEnumValue[] values = new DefaultEnumValue[0];

    /**
     * Creates a new default enumeration type.
     * 
     * @param name The enumeration type's name.
     * @param valueClass The Java class the values are instances of.
     * 
     * @throws IllegalArgumentException If <tt>name</tt> is <tt>null</tt> or if the
     *             <tt>valueClass</tt> is not a subclass of <tt>DefaultEnumValue</tt>.
     */
    public DefaultEnumType(String name, Class<?> valueClass) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!(DefaultEnumValue.class.isAssignableFrom(valueClass))) {
            throw new IllegalArgumentException(valueClass + " is not a subclass of " + DefaultEnumValue.class); //$NON-NLS-1$
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
     * @throws IllegalArgumentException If the type contains already an id with the given id.
     */
    void addValue(DefaultEnumValue newValue) {
        if (containsValue(newValue.getId())) {
            throw new IllegalArgumentException("The enum type " + this + " contains already a value " + newValue); //$NON-NLS-1$ //$NON-NLS-2$
        }
        DefaultEnumValue[] newValues = new DefaultEnumValue[values.length + 1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[values.length] = newValue;
        values = newValues;
    }

    @Override
    public EnumValue[] getValues() {
        DefaultEnumValue[] copy = new DefaultEnumValue[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }

    @Override
    public String[] getValueIds() {
        String[] ids = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            ids[i] = values[i].getId();
        }
        return ids;
    }

    @Override
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

    @Override
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

    @Override
    public int getNumOfValues() {
        return values.length;
    }

    @Override
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
