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

package org.faktorips.runtime.internal;

/**
 * Represents a collection of enum values. This class is used during the process of reading data of
 * enum value sets from an xml representation into memory and create
 * <code>org.faktorips.valueset.EnumValueSet</code> objects.
 * 
 * @author Peter Erzberger
 */
public class EnumValues {

    private String[] values;
    private boolean containsNull;

    /**
     * Creates a new EnumValues object.
     */
    public EnumValues(String[] values, boolean containsNull) {
        super();
        this.values = values;
        this.containsNull = containsNull;
    }

    /**
     * @return Returns of the value null is contained in the values
     */
    public boolean containsNull() {
        return containsNull;
    }

    /**
     * @return Returns the value at the indexed position
     */
    public String getValue(int index) {
        return values[index];
    }

    /**
     * @return Returns the number of values
     */
    public int getNumberOfValues() {
        return values.length;
    }
}
