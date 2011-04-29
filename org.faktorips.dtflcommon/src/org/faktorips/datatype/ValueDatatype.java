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

package org.faktorips.datatype;

import org.faktorips.util.message.MessageList;

/**
 * A datatype representing values (in contrast to reference objects).
 */
public interface ValueDatatype extends Datatype {

    /**
     * If this datatype represents a primitive type, this method returns the datatype that
     * represents the wrapper class. Returns <code>null</code> if this datatype does not represent a
     * primitive.
     */
    public ValueDatatype getWrapperType();

    /**
     * Returns <code>true</code> if the given string can be parsed to a value of this datatype.
     * Returns <code>false</code> otherwise.
     */
    public boolean isParsable(String value);

    /**
     * Returns <code>true</code> if the given string is <code>null</code> or the representation of
     * the NullObject (if the datatype value class makes use of the null object pattern.) Returns
     * <code>false</code> otherwise.
     */
    public boolean isNull(String value);

    /**
     * Returns <code>true</code> if this is a mutable datatype, <code>false</code> if it is an
     * immutable datatype.
     * 
     * @return s.above
     */
    public boolean isMutable();

    /**
     * Returns <code>true</code> if this is an immutable datatype, <code>false</code> otherwise.
     * 
     * @return s.above
     */
    public boolean isImmutable();

    /**
     * Returns the datatype's default value. For datatypes representing objects the method returns
     * <code>null</code>. For datatypes representing Java primitives the Java default value is
     * returned, e.g. 0 for int.
     * 
     * @throws UnsupportedOperationException if this datatype is the Datatype Void.
     * 
     * @see Void
     */
    public String getDefaultValue();

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents.
     * 
     * Use with caution: During development time Faktor-IPS maintains all values with their string
     * representation. This allows to change the value's datatype without the need to convert the
     * value from one class to another (e.g. if the string representation is 42 you can change the
     * datatype from integer to string without converting the integer object to a string object.
     * 
     * @param value string represenation of the value
     * @return The value as instance of the class this datatype represents.
     * 
     * 
     */
    public Object getValue(String value);

    /**
     * @return <code>true</code> if this datatype is able to compare two values.
     */
    public boolean supportsCompare();

    /**
     * @param valueA The value to compare to valueB
     * @param valueB The value to compare to valueA
     * @return A value less than 0 if valueA is less than valueB, 0 if valueA is equal to valueB and
     *         a value greater than 0 if valueA is greater than valueB.
     * @throws UnsupportedOperationException if compare is not supported by this datatype.
     * @see #supportsCompare()
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException;

    /**
     * @return <code>true</code> if the both given strings represent the same value defined by this
     *         datatype. The String " 1" (a blank followed by the char '1') and "1" (just the char
     *         '1') are equal if the datatype is an Integer, but will not be equal if the datatype
     *         is a String.
     * @throws IllegalArgumentException if one of the parameter values doesn't exist in the value
     *             set of this datatype.
     */
    public boolean areValuesEqual(String valueA, String valueB);

    /**
     * Validates the value datatype and returns a message list containing error messages if the
     * datatype is invalid. If the datatype is valid an empty list is returned.
     * <p>
     * Value datatypes like the predefined datatypes (defined by the constants in this class) are
     * always valid. However generic datatypes that implement this interface might be invalid.
     */
    public MessageList checkReadyToUse();

}
