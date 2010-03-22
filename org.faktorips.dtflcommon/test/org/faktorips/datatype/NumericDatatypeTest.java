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

package org.faktorips.datatype;

import junit.framework.TestCase;

import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;

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
        } catch (NullPointerException e) {
            // success
        }
        try {
            datatype.divisibleWithoutRemainder(null, "2");
            fail();
        } catch (NullPointerException e) {
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
        } catch (NumberFormatException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder("10", null);
            fail();
        } catch (NullPointerException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder("", "2");
            fail();
        } catch (NumberFormatException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder(null, "2");
            fail();
        } catch (NullPointerException e) {
            // success
        }
    }

    public void testHasDecimalPlaces() {
        assertHasDecimalPlaces(true, new DecimalDatatype());
        assertHasDecimalPlaces(true, new DoubleDatatype());
        assertHasDecimalPlaces(false, new IntegerDatatype());
        assertHasDecimalPlaces(false, new LongDatatype());
        assertHasDecimalPlaces(true, new MoneyDatatype());
        assertHasDecimalPlaces(false, new PrimitiveIntegerDatatype());
        assertHasDecimalPlaces(false, new PrimitiveLongDatatype());
    }

    private void assertHasDecimalPlaces(boolean hasDecimalPlaces, NumericDatatype numericDatatype) {
        if (hasDecimalPlaces) {
            assertTrue(numericDatatype.hasDecimalPlaces());
        } else {
            assertFalse(numericDatatype.hasDecimalPlaces());
        }

    }

}
