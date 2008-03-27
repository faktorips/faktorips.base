/**
 * 
 */
package org.faktorips.runtime.internal.java5;

import java.util.Iterator;

/**
 * 
 * @author Daniel Hohenberger
 */
public class EnumValues implements Iterable<String> {

    private final org.faktorips.runtime.internal.EnumValues enumValues;
    
    /**
     * Creates a new EnumValues object.
     */
    public EnumValues(String[] values, boolean containsNull) {
        this.enumValues = new org.faktorips.runtime.internal.EnumValues(values, containsNull);
    }
    
    /**
     * Creates a new EnumValues object, wrapping the org.faktorips.runtime.internal.EnumValues object.
     */
    public EnumValues(final org.faktorips.runtime.internal.EnumValues enumValues) {
        this.enumValues = enumValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {

            private int index = -1;

            @Override
            public boolean hasNext() {
                return index < enumValues.getNumberOfValues() - 1;
            }

            @Override
            public String next() {
                return enumValues.getValue(++index);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * @return Returns if the value null is contained in the values
     */
    public boolean containsNull() {
        return enumValues.containsNull();
    }

    /**
     * @return Returns the value at the indexed position
     */
    public String getValue(int index) {
        return enumValues.getValue(index);
    }

    /**
     * @return Returns the number of values
     */
    public int getNumberOfValues() {
        return enumValues.getNumberOfValues();
    }

}
