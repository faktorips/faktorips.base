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

import java.util.Comparator;
import java.util.Objects;

import org.faktorips.values.NullObjectComparator;
import org.faktorips.values.NullObjectSupport;

/**
 * A datatype that represents a Java class representing a value, for example
 * {@code java.lang.String}. The class name is held as a String.
 * 
 * @author Jan Ortmann
 */
public abstract class ValueClassNameDatatype extends AbstractDatatype implements ValueDatatype {

    private String name;

    public ValueClassNameDatatype(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getQualifiedName() {
        return name;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public final boolean isImmutable() {
        return !isMutable();
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * Returns the string representation of the given value. The String value returned by this
     * method must be parsable by the {@link #getValue(String)} method.
     * 
     * @see #getValue(String)
     */
    @Override
    public String valueToString(Object value) {
        return value.toString();
    }

    @Override
    public boolean isNull(String valueString) {
        Object value;
        try {
            value = getValue(valueString);
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            // => value can't be parsed, so it's also not null
            return false;
        }
        if (value == null) {
            return true;
        }

        if (!(value instanceof NullObjectSupport)) {
            return false;
        }
        return ((NullObjectSupport)value).isNull();
    }

    @Override
    public boolean isParsable(String value) {
        try {
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

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return Objects.equals(getValue(valueA), getValue(valueB));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int compare(String valueA, String valueB) {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("Datatype " + getQualifiedName() //$NON-NLS-1$
            + " does not support comparison of values"); //$NON-NLS-1$
        }
        Comparable<?> valA = (Comparable<Object>)getValue(valueA);
        Comparable<?> valB = (Comparable<Object>)getValue(valueB);
        if (hasNullObject()) {
            return compareNullObjects(valA, valB);
        } else {
            return Comparator.nullsFirst(Comparator.<Comparable> naturalOrder()).compare(valA, valB);
        }
    }

    @SuppressWarnings("unchecked")
    private static <C extends NullObjectSupport & Comparable<C>> int compareNullObjects(Comparable<?> valA,
            Comparable<?> valB) {
        return NullObjectComparator.<C> nullsFirst().compare((C)valA, (C)valB);
    }

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents.
     * 
     * Use with caution: During development time Faktor-IPS maintains all values with their string
     * representation. This allows to change the value's datatype without the need to convert the
     * value from one class to another (e.g. if the string representation is 42 you can change the
     * datatype from integer to string without converting the integer object to a string object.
     * 
     * @param value string representation of the value
     * @return The value as instance of the class this datatype represents.
     * 
     * @see #valueToString(Object)
     * 
     */
    @Override
    public abstract Object getValue(String value);
}
