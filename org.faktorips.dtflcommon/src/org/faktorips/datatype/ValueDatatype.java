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

import org.faktorips.runtime.MessageList;
import org.faktorips.values.NullObject;

/**
 * A datatype representing values (in contrast to reference objects).
 */
public interface ValueDatatype extends Datatype {

    /**
     * If this datatype represents a primitive type, this method returns the datatype that
     * represents the wrapper class. Returns {@code null} if this datatype does not represent a
     * primitive.
     */
    public ValueDatatype getWrapperType();

    /**
     * Returns {@code true} if the given string can be parsed to a value of this datatype. Returns
     * {@code false} otherwise.
     */
    public boolean isParsable(String value);

    /**
     * Returns {@code true} if the given string is {@code null} or the representation of the null
     * object (if the datatype value class makes use of the null object pattern.) Returns
     * {@code false} otherwise.
     *
     * @see NullObject
     */
    public boolean isNull(String value);

    /**
     * Returns {@code true} if this is a mutable datatype, {@code false} if it is an immutable
     * datatype.
     * 
     * @return whether this is a mutable datatype
     */
    public boolean isMutable();

    /**
     * Returns {@code true} if this is an immutable datatype, {@code false} otherwise.
     * 
     * @return whether this is an immutable datatype
     */
    public boolean isImmutable();

    /**
     * Returns the datatype's default value. For datatypes representing objects the method returns
     * {@code null}. For datatypes representing Java primitives the Java default value is returned,
     * e.g. 0 for int.
     * 
     * @throws UnsupportedOperationException if this datatype is the Datatype {@link Void}.
     * 
     * @see Void
     */
    public String getDefaultValue();

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents.
     * <p>
     * Use with caution: During development time Faktor-IPS maintains all values with their string
     * representation. This allows to change the value's datatype without the need to convert the
     * value from one class to another (e.g. if the string representation is 42 you can change the
     * datatype from integer to string without converting the integer object to a string object.
     * <p>
     * May throw different exceptions if the given string does not represent any value, for example
     * a {@link NumberFormatException} when {@code "twelve"} is passed to
     * {@link PrimitiveIntegerDatatype#getValue(String)}.
     * 
     * @param value the string representation of a value
     * @return the value as instance of the class this datatype represents
     */
    public Object getValue(String value);

    /**
     * @return {@code true} if this datatype is able to compare two values.
     */
    public boolean supportsCompare();

    /**
     * Compares the values created from the two given strings.
     * 
     * @param valueA The value to compare to valueB
     * @param valueB The value to compare to valueA
     * @return A value less than 0 if valueA is less than valueB, 0 if valueA is equal to valueB and
     *         a value greater than 0 if valueA is greater than valueB.
     * @throws UnsupportedOperationException if compare is not supported by this datatype.
     * @see #supportsCompare()
     * @see #getValue(String)
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException;

    /**
     * Returns {@code true} if both given strings represent the same value defined by this datatype.
     * The String " 1" (a blank followed by the char '1') and "1" (just the char '1') are equal if
     * the datatype is an {@link Integer}, but will not be equal if the datatype is a
     * {@link String}.
     * 
     * @param valueA The first parameter to compare
     * @param valueB The second parameter to compare
     * 
     * @return {@code true} if the two values are equal according to the datatype, returns
     *         {@code false} if they are different.
     * 
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
