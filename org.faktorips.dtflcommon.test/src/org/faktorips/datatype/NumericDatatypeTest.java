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

package org.faktorips.datatype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.DoubleDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.datatype.classtypes.LongDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class NumericDatatypeTest {

    private class TestDatatypeWithDecinalPlaces<T extends NumericDatatype>  {

        private T datatype;

        public TestDatatypeWithDecinalPlaces(T datatype) {
            this.datatype = datatype;
            if (!datatype.hasDecimalPlaces()) {
                throw new RuntimeException("Not supported! Class tests only datatypes with decimal places."); //$NON-NLS-1$
            }
        }

        @Test
        public void testDivisibleWithoutRemainderDecimal() {
            assertTrue(datatype.divisibleWithoutRemainder("10", "2")); //$NON-NLS-1$ //$NON-NLS-2$
            assertFalse(datatype.divisibleWithoutRemainder("9", "2")); //$NON-NLS-1$ //$NON-NLS-2$

            assertFalse(datatype.divisibleWithoutRemainder("10", "0")); //$NON-NLS-1$ //$NON-NLS-2$

            assertTrue(datatype.divisibleWithoutRemainder("2.4", "1.2")); //$NON-NLS-1$ //$NON-NLS-2$
            assertFalse(datatype.divisibleWithoutRemainder("2.41", "1.2")); //$NON-NLS-1$ //$NON-NLS-2$

            try {
                datatype.divisibleWithoutRemainder("10", null); //$NON-NLS-1$
                fail();
            } catch (NullPointerException e) {
                // success
            }
            try {
                datatype.divisibleWithoutRemainder(null, "2"); //$NON-NLS-1$
                fail();
            } catch (NullPointerException e) {
                // success
            }
        }

    }

    @Test
    public void testDivisibleWithoutRemainderPrimitiveInteger() {
        PrimitiveIntegerDatatype datatype = new PrimitiveIntegerDatatype();
        defaultTests(datatype);
    }

    @Test
    public void testDivisibleWithoutRemainderDouble() {
        DoubleDatatype datatype = new DoubleDatatype();
        defaultTests(datatype);

        assertTrue(datatype.divisibleWithoutRemainder("100", "0.25")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(datatype.divisibleWithoutRemainder("100", "0.2")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testDivisibleWithoutRemainderInteger() {
        IntegerDatatype datatype = new IntegerDatatype();
        defaultTests(datatype);
    }

    @Test
    public void testDivisibleWithoutRemainderLong() {
        LongDatatype datatype = new LongDatatype();
        defaultTests(datatype);
    }

    @Test
    public void testDivisibleWithoutRemainderDecimal() {
        TestDatatypeWithDecinalPlaces<NumericDatatype> bigDecimalDatatypeTest = new TestDatatypeWithDecinalPlaces<NumericDatatype>(
                new BigDecimalDatatype());
        TestDatatypeWithDecinalPlaces<NumericDatatype> decimalDatatypeTest = new TestDatatypeWithDecinalPlaces<NumericDatatype>(
                new DecimalDatatype());

        decimalDatatypeTest.testDivisibleWithoutRemainderDecimal();
        bigDecimalDatatypeTest.testDivisibleWithoutRemainderDecimal();

        // decimal only special test case for the null value
        DecimalDatatype datatype = new DecimalDatatype();
        assertTrue(datatype.divisibleWithoutRemainder("10", "")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(datatype.divisibleWithoutRemainder("", "2")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void defaultTests(NumericDatatype datatype) {
        assertTrue(datatype.divisibleWithoutRemainder("10", "2")); //$NON-NLS-1$ //$NON-NLS-2$
        assertFalse(datatype.divisibleWithoutRemainder("9", "2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertFalse(datatype.divisibleWithoutRemainder("10", "0")); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            datatype.divisibleWithoutRemainder("10", ""); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder("10", null); //$NON-NLS-1$
            fail();
        } catch (NullPointerException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder("", "2"); //$NON-NLS-1$ //$NON-NLS-2$
            fail();
        } catch (NumberFormatException e) {
            // success
        }

        try {
            datatype.divisibleWithoutRemainder(null, "2"); //$NON-NLS-1$
            fail();
        } catch (NullPointerException e) {
            // success
        }
    }

    @Test
    public void testHasDecimalPlaces() {
        assertHasDecimalPlaces(true, new DecimalDatatype());
        assertHasDecimalPlaces(true, new DoubleDatatype());
        assertHasDecimalPlaces(false, new IntegerDatatype());
        assertHasDecimalPlaces(false, new LongDatatype());
        assertHasDecimalPlaces(true, new MoneyDatatype());
        assertHasDecimalPlaces(false, new PrimitiveIntegerDatatype());
        assertHasDecimalPlaces(false, new PrimitiveLongDatatype());
        assertHasDecimalPlaces(true, new BigDecimalDatatype());
    }

    private void assertHasDecimalPlaces(boolean hasDecimalPlaces, NumericDatatype numericDatatype) {
        if (hasDecimalPlaces) {
            assertTrue(numericDatatype.hasDecimalPlaces());
        } else {
            assertFalse(numericDatatype.hasDecimalPlaces());
        }

    }

}
