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

package org.faktorips.values;

/**
 * An EnumType is a Datatype that represents an enumeration of values e.g. gender, payment mode.
 * 
 * @author Jan Ortmann
 */
public interface EnumType {

    /**
     * Returns the type's values or an empty array if the type has no values.
     */
    public EnumValue[] getValues();

    /**
     * Returns the type's value ids as string array.
     */
    public String[] getValueIds();

    /**
     * Returns true if the id identifies an EnumValue, otherwise false.
     */
    public boolean containsValue(String id);

    /**
     * Returns the EnumValue identified by the given id. If the id does not represent an EnumValue,
     * <code>null</code> is returned.
     */
    public EnumValue getEnumValue(String id);

    /**
     * Returns the EnumValue at the given index.
     * 
     * @throws IllegalArgumentException if the index is out of bounce.
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the number of possible values
     */
    public int getNumOfValues();

}
