/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * Speical value datatype representing numbers.
 * 
 * @author Jan Ortmann
 */
public interface NumericDatatype extends ValueDatatype {

    
    /**
     * Subtraction
     * @param minuend The value to be reduced.
     * @param subtrahend The value to be used to reduce the minuend
     * @return The result of "minuend - subtrahend".
     * 
     * @throws NullPointerException if at least one of minuend and subtrahend is <code>null</code>
     * @throws NumberFormatException if at least one of minuend and subtrahend can not be converted
     * into a number of this datatype. 
     */
    public String substract(String minuend, String subtrahend);

    /**
     * @param dividend The value to be devided
     * @param divisor The value to be used to divide the dividend
     * @return <code>true</code> if dividend can be divided by the divisor without remainder, 
     * <code>false</code> otherwise.
     * 
     * @throws NullPointerException if at least one of dividend and divisor is <code>null</code>
     * @throws NumberFormatException if at least one of dividend and divisor can not be converted
     * into a number of this datatype. 
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor);
    
}
