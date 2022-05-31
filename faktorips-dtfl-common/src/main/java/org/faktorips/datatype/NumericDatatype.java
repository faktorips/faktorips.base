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

/**
 * Special value datatype representing numbers.
 * 
 * @author Jan Ortmann
 */
public interface NumericDatatype extends ValueDatatype {

    /**
     * Subtraction. This method is used to validate numeric ranges, see RangeValueSet as an example.
     * 
     * @param minuend The value to be reduced.
     * @param subtrahend The value to be used to reduce the minuend
     * @return The result of "minuend - subtrahend".
     * 
     * @throws NullPointerException if at least one of minuend and subtrahend is <code>null</code>
     * @throws NumberFormatException if at least one of minuend and subtrahend can not be converted
     *             into a number of this datatype.
     */
    public String subtract(String minuend, String subtrahend);

    /**
     * Decides whether the given dividend can be divided by the divisor without remainder or not.
     * This method is used to validate numeric ranges, see RangeValueSet as an example.
     * 
     * @param dividend The value to be divided
     * @param divisor The value to be used to divide the dividend
     * @return <code>true</code> if dividend can be divided by the divisor without remainder,
     *         <code>false</code> otherwise.
     * 
     * @throws NullPointerException if at least one of dividend and divisor is <code>null</code>
     * @throws NumberFormatException if at least one of dividend and divisor can not be converted
     *             into a number of this datatype.
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor);

    public boolean hasDecimalPlaces();

}
