/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;


/**
 * A datatype representing values (in contrast to reference objects).
 */
public interface ValueDatatype extends Datatype {

    /**
     * If this datatype represents a primitive type, this method returns the
     * datatype that represents the wrapper class. Returns <code>null</code> if this datatype
     * does not represent a primitive. 
     */
    public Datatype getWrapperType();
    
	/**
	 * Returns <code>true</code> if the given string can be parsed to a value of this datatype.
	 * Returns <code>false</code> otherwise.
	 */
	public boolean isParsable(String value);
	
	/**
	 * Returns <code>true</code> if the given string is <code>null</code> or the representation of the NullObject 
     * (if the datatype value class makes use of the null object pattern.
	 * Returns <code>false</code> otherwise.
	 */
	public boolean isNull(String value);
    
    /**
     * Returns the datatype's default value. For datatypes representing objects the method returns <code>null</code>.
     * For datatypes representing Java primitives the Java default value is returned, e.g. 0 for int.
     * 
     * @throws UnsupportedOperationException if this datatype is the Datatype Void.
     * 
     * @see Void
     */
    public String getDefaultValue();
    
    /**
     * @return <code>true</code> if this datatype is able to compare two values.
     */
    public boolean supportsCompare();
    
    /**
     * @param valueA The value to compare to valueB
     * @param valueB The value to compare to valueA
     * @return A value less than 0 if valueA is less than valueB, 0 if valueA is equal to valueB 
     * and a value greater than 0 if valueA is greater than valueB.
     * @throws UnsupportedOperationException if compare is not supported by this datatype. 
     * @see supportsCompare()
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException;
    
    /**
     * @return <code>true</code> if the both given strings represent the same value defined by this datatype.
     * The String " 1" (a blank followed by the char '1') and "1" (just the char '1') are equal if the 
     * datatype is an Integer, but will not be equal if the datatype is a String.
     */
    public boolean areValuesEqual(String valueA, String valueB);
}
