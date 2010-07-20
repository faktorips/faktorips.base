/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.util.StringUtil;
import org.faktorips.values.NullObjectSupport;

/**
 * A datatype that represents a Java class representing a value, for example java.lang.String.
 * 
 * @author Jan Ortmann
 */
public abstract class ValueClassDatatype extends AbstractDatatype implements ValueDatatype {

    private Class<?> clazz;
    private String name;

    public ValueClassDatatype(Class<?> clazz) {
        this(clazz, StringUtil.unqualifiedName(clazz.getName()));
    }

    public ValueClassDatatype(Class<?> clazz, String name) {
        this.clazz = clazz;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return name;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isMutable() {
        return false;
    }

    public final boolean isImmutable() {
        return !isMutable();
    }

    public boolean isValueDatatype() {
        return true;
    }

    public String getDefaultValue() {
        return null;
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    public Class<?> getJavaClass() {
        return clazz;
    }

    public String getJavaClassName() {
        return clazz.getName();
    }

    /**
     * Returns the string representation of the given value. The String value returned by this
     * method must be parsable by the {@link #getValue(String)} method.
     * 
     * @see #getValue(String)
     * 
     * @deprecated see {@link #getValue(String)}
     */
    @Deprecated
    public String valueToString(Object value) {
        return value.toString();
    }

    public boolean isNull(String valueString) {
        Object value;
        try {
            value = getValue(valueString);
        } catch (Exception e) {
            return false; // => value can't be parsed, so it's also not null
        }
        if (value == null) {
            return true;
        }

        if (!(value instanceof NullObjectSupport)) {
            return false;
        }
        return ((NullObjectSupport)value).isNull();
    }

    public boolean isParsable(String value) {
        try {
            if ("".equals(value)) { //$NON-NLS-1$
                /*
                 * by default the empty space is not parsable. This has to be handled explicitly as
                 * most value classes assume that the value of the string "" is null. This is
                 * however more a convenience. In the IDE context it is bothering if null can be
                 * represented by null or the string "".
                 */
                return false;
            }
            if (isNull(value)) {
                return true;
            }
            getValue(value);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(getValue(valueA), getValue(valueB));
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("Datatype " + getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison of values"); //$NON-NLS-1$
        }
        Comparable<Object> valA = (Comparable<Object>)getValue(valueA);
        if (valA == null) {
            return -1;
        }
        Object valB = getValue(valueB);
        if (valB == null) {
            return 1;
        }
        return valA.compareTo(valB);
    }

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents.
     * 
     * @param value string represenation of the value
     * @return The value as instance of the class this datatype represents.
     * 
     * @see #valueToString(Object)
     * 
     * @deprecated During development time Faktor-IPS maintains all values with their string
     *             representation. This allows to change the value's datatype without the need to
     *             convert the value from one class to another (e.g. if the string representation is
     *             42 you can change the datatype from integer to string without converting the
     *             integer object to a string object.
     */
    @Deprecated
    public abstract Object getValue(String value);

}
