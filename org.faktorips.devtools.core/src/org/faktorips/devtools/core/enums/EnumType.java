/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.enums;

/**
 * An EnumType is a data type that represents an enumeration of values e.g. gender, payment mode.
 * 
 * @author Jan Ortmann
 */
public interface EnumType {

    /**
     * Returns the type's values or an empty array if the type has no values.
     */
    public EnumValue[] getValues();

    /**
     * Returns the type's value IDs as string array.
     */
    public String[] getValueIds();

    /**
     * Returns <tt>true</tt> if the ID identifies an <tt>EnumValue</tt>, otherwise <tt>false</tt>.
     */
    public boolean containsValue(String id);

    /**
     * Returns the <tt>EnumValue</tt> identified by the given ID. If the ID does not represent an
     * <tt>EnumValue</tt>, <code>null</code> will be returned.
     */
    public EnumValue getEnumValue(String id);

    /**
     * Returns the <tt>EnumValue</tt> at the given index.
     * 
     * @throws IllegalArgumentException If the index is out of bounds.
     */
    public EnumValue getEnumValue(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the number of values.
     */
    public int getNumOfValues();

}
