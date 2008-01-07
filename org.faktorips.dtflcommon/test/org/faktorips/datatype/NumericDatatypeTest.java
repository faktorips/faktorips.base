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

import junit.framework.TestCase;

import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;

/**
 * 
 * @author Thorsten Guenther
 */
public class NumericDatatypeTest extends TestCase {

    public void testDivisibleWithoutRemainderPrimitiveInteger() {
        PrimitiveIntegerDatatype datatype = new PrimitiveIntegerDatatype();
        defaultTests(datatype);
    }
    
    public void testDivisibleWithoutRemainderDouble() {
        DoubleDatatype datatype = new DoubleDatatype();
        defaultTests(datatype);
        
        assertTrue(datatype.divisibleWithoutRemainder("100", "0.25"));
        assertTrue(datatype.divisibleWithoutRemainder("100", "0.2"));
    }
    
    public void testDivisibleWithoutRemainderInteger() {
        IntegerDatatype datatype = new IntegerDatatype();
        defaultTests(datatype);
    }

    public void testDivisibleWithoutRemainderLong() {
        LongDatatype datatype = new LongDatatype();
        defaultTests(datatype);
    }
    
    public void testDivisibleWithoutRemainderDecimal() {
        DecimalDatatype datatype = new DecimalDatatype();
        assertTrue(datatype.divisibleWithoutRemainder("10", "2"));
        assertFalse(datatype.divisibleWithoutRemainder("9", "2"));

        assertFalse(datatype.divisibleWithoutRemainder("10", "0"));
        assertTrue(datatype.divisibleWithoutRemainder("10", ""));
        assertTrue(datatype.divisibleWithoutRemainder("", "2"));
        
        assertTrue(datatype.divisibleWithoutRemainder("2.4", "1.2"));
        assertFalse(datatype.divisibleWithoutRemainder("2.41", "1.2"));
        
        try {
            datatype.divisibleWithoutRemainder("10", null);
            fail();
        } 
        catch (NullPointerException e) {
            // success
        }
        try {
            datatype.divisibleWithoutRemainder(null, "2");
            fail();
        } 
        catch (NullPointerException e) {
            // success
        }
    }
    

    private void defaultTests(NumericDatatype datatype) {
        assertTrue(datatype.divisibleWithoutRemainder("10", "2"));
        assertFalse(datatype.divisibleWithoutRemainder("9", "2"));

        assertFalse(datatype.divisibleWithoutRemainder("10", "0"));
        
        try {
            datatype.divisibleWithoutRemainder("10", "");
            fail();
        } 
        catch (NumberFormatException e) {
            // success
        }
        
        try {
            datatype.divisibleWithoutRemainder("10", null);
            fail();
        } 
        catch (NullPointerException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder("", "2");
            fail();
        } 
        catch (NumberFormatException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder(null, "2");
            fail();
        } 
        catch (NullPointerException e) {
            // success
        }
    }
    
}
