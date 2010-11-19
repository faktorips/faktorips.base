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

package org.faktorips.values;

import java.math.BigDecimal;
import java.util.Currency;

import junit.framework.TestCase;

public class MoneyTest extends TestCase {

    public void testValueOf_DecimalCurrency() {
        assertEquals(Money.valueOf("420EUR"), Money.valueOf(Decimal.valueOf("420"), Currency.getInstance("EUR")));
        assertEquals(Money.valueOf("13.42EUR"), Money.valueOf(Decimal.valueOf("13.42"), Currency.getInstance("EUR")));
        assertEquals(Money.valueOf("13.4EUR"), Money.valueOf(Decimal.valueOf("13.4"), Currency.getInstance("EUR")));
        try {
            Money.valueOf(Decimal.valueOf("13.413"), Currency.getInstance("EUR"));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        // null
        assertEquals(Money.NULL, Money.valueOf(null, Currency.getInstance("EUR")));
        assertEquals(Money.NULL, Money.valueOf(Decimal.NULL, Currency.getInstance("EUR")));
        assertEquals(Money.NULL, Money.valueOf(Decimal.valueOf(42, 0), null));
    }

    public void testValueOf_DecimalCurrencyInt() {
        assertEquals(Money.valueOf("420EUR"),
                Money.valueOf(Decimal.valueOf("420"), Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));
        assertEquals(Money.valueOf("13.42EUR"),
                Money.valueOf(Decimal.valueOf("13.42"), Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));
        assertEquals(Money.valueOf("13.4EUR"),
                Money.valueOf(Decimal.valueOf("13.4"), Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));
        assertEquals(Money.valueOf("13.42EUR"),
                Money.valueOf(Decimal.valueOf("13.415"), Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));

        // null
        assertEquals(Money.NULL, Money.valueOf(null, Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));
        assertEquals(Money.NULL, Money.valueOf(Decimal.NULL, Currency.getInstance("EUR"), BigDecimal.ROUND_HALF_UP));
        assertEquals(Money.NULL, Money.valueOf(Decimal.valueOf(42, 0), null, BigDecimal.ROUND_HALF_UP));
    }

    public void testValueOfString() {
        assertEquals(Money.euro(10, 12), Money.valueOf("10.12EUR"));
        assertEquals(Money.euro(10, 12), Money.valueOf("10.12 EUR"));
        assertEquals(Money.euro(10, 12), Money.valueOf(" 10.12  EUR "));
        assertEquals(Money.euro(-10, -12), Money.valueOf("-10.12EUR"));

        assertEquals(Money.euro(10, 0), Money.valueOf(" 10. EUR "));
        assertEquals(Money.euro(10, 0), Money.valueOf("10.EUR"));

        assertEquals(Money.euro(123, 0), Money.valueOf("123EUR"));
        assertEquals(Money.euro(123, 0), Money.valueOf("123 EUR"));
        assertEquals(Money.euro(123, 0), Money.valueOf(" 123  EUR "));
        assertEquals(Money.euro(-123, 0), Money.valueOf(" -123  EUR "));

        assertEquals(Money.euro(10, 10), Money.valueOf("10.1EUR"));
        assertEquals(Money.euro(10, 9), Money.valueOf("10.09EUR"));
        assertEquals(Money.euro(10, 90), Money.valueOf("10.90EUR"));
        assertEquals(Money.euro(10, 99), Money.valueOf("10.99EUR"));

        assertEquals(Money.NULL, Money.valueOf(""));
        assertEquals(Money.NULL, Money.valueOf(null));

        try {
            Money.valueOf("1");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            Money.valueOf("111");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            Money.valueOf("1a1EUR");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            Money.valueOf("1.123EUR");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testValueOflongintCurrency() {
        Money money = Money.valueOf(123, 74, Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("EUR"), money.getCurrency());
        assertEquals(Decimal.valueOf("123.74"), money.getAmount());

        money = Money.valueOf(-123, -74, Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("EUR"), money.getCurrency());
        assertEquals(Decimal.valueOf("-123.74"), money.getAmount());
    }

    public void testSymmetryOfToStringAndValueOfForNull() {
        Money moneyNull = Money.NULL;
        assertTrue(Money.valueOf(moneyNull.toString()).isNull());
    }

    public void testGetAmount() {
        assertEquals(Decimal.valueOf("7.31"), Money.euro(7, 31).getAmount());
        assertTrue(Money.NULL.getAmount().isNull());
    }

    public void testGetCurrency() {
        assertEquals(Currency.getInstance("EUR"), Money.euro(7, 31).getCurrency());
        assertNull(Money.NULL.getCurrency());
    }

    public void testIsNull() {
        assertFalse(Money.euro(0, 0).isNull());
        assertTrue(Money.NULL.isNull());
    }

    public void testAdd() {
        Money m1 = Money.euro(10, 0);
        Money m2 = Money.euro(12, 43);
        assertEquals(Money.euro(22, 43), m1.add(m2));
        assertEquals(Money.euro(22, 43), m2.add(m1));

        assertEquals(Money.NULL, m1.add(Money.NULL));
        assertEquals(Money.NULL, Money.NULL.add(m1));

        // different currencies
        try {
            m1.add(Money.usd(1, 0));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        // null
        try {
            m1.add(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.add(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testSubtract() {
        Money m1 = Money.euro(10, 0);
        Money m2 = Money.euro(12, 43);
        assertEquals(Money.euro(-2, -43), m1.subtract(m2));
        assertEquals(Money.euro(2, 43), m2.subtract(m1));

        assertEquals(Money.NULL, m1.add(Money.NULL));
        assertEquals(Money.NULL, Money.NULL.add(m1));

        // different currencies
        try {
            m1.add(Money.usd(1, 0));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        // null
        try {
            m1.subtract(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.subtract(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testMultiplyint() {
        Money m = Money.euro(10, 12);
        int factor = 2;
        assertEquals(Money.euro(20, 24), m.multiply(factor));
        assertTrue(Money.NULL.multiply(factor).isNull());
    }

    public void testMultiplylong() {
        Money m = Money.euro(10, 12);
        long factor = 2;
        assertEquals(Money.euro(20, 24), m.multiply(factor));
        assertTrue(Money.NULL.multiply(factor).isNull());
    }

    public void testMultiplyDecimal() {
        Money m = Money.euro(10, 12);
        // no rounding necessary
        assertEquals(Money.euro(20, 24), m.multiply(Decimal.valueOf("2"), BigDecimal.ROUND_HALF_UP));

        // rounding necessary (value is 22.264)
        assertEquals(Money.euro(22, 26), m.multiply(Decimal.valueOf("2.2"), BigDecimal.ROUND_HALF_UP));

        // null objects
        assertTrue(Money.NULL.multiply(Decimal.valueOf("2"), 2).isNull());
        assertTrue(m.multiply(Decimal.NULL, BigDecimal.ROUND_HALF_DOWN).isNull());

        // null
        try {
            m.multiply(null, BigDecimal.ROUND_HALF_UP);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.multiply(null, BigDecimal.ROUND_UP);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

    }

    public void testDivide_int() {
        Money m = Money.euro(10, 12);
        int divisor = 2;
        // no rounding necessary
        assertEquals(Money.euro(5, 6), m.divide(divisor, BigDecimal.ROUND_HALF_UP));

        // rounding necessary (value is 3.373333...)
        divisor = 3;
        assertEquals(Money.euro(3, 37), m.divide(divisor, BigDecimal.ROUND_HALF_UP));

        // null
        assertTrue(Money.NULL.divide(2, 2).isNull());
    }

    public void testDivide_long() {
        Money m = Money.euro(10, 12);
        long divisor = 2;
        // no rounding necessary
        assertEquals(Money.euro(5, 6), m.divide(divisor, BigDecimal.ROUND_HALF_UP));

        // rounding necessary (value is 3.373333...)
        divisor = 3;
        assertEquals(Money.euro(3, 37), m.divide(divisor, BigDecimal.ROUND_HALF_UP));

        // null
        assertTrue(Money.NULL.divide(2, 2).isNull());
    }

    public void testDivide_Decimal() {
        Money m = Money.euro(10, 12);
        // no rounding necessary
        assertEquals(Money.euro(5, 6), m.divide(Decimal.valueOf("2"), BigDecimal.ROUND_HALF_UP));

        // rounding necessary (value is 4.216666...)
        assertEquals(Money.euro(4, 22), m.divide(Decimal.valueOf("2.4"), BigDecimal.ROUND_HALF_UP));

        // null object
        assertTrue(Money.NULL.divide(Decimal.valueOf("2"), 2).isNull());
        assertTrue(m.divide(Decimal.NULL, BigDecimal.ROUND_HALF_DOWN).isNull());

        // null
        try {
            m.divide(null, BigDecimal.ROUND_HALF_UP);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.divide(null, BigDecimal.ROUND_UP);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testCompareTo() {
        Money m = Money.euro(10, 42);
        assertTrue(m.compareTo(Money.euro(10, 41)) > 0);
        assertTrue(m.compareTo(Money.euro(10, 42)) == 0);
        assertTrue(m.compareTo(Money.euro(10, 43)) < 0);

        try {
            m.compareTo(Money.usd(10, 42));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            m.compareTo(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            m.compareTo(Money.NULL);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.compareTo(m);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testGreaterThan() {
        Money m = Money.euro(10, 42);
        assertTrue(m.greaterThan(Money.euro(10, 41)));
        assertFalse(m.greaterThan(Money.euro(10, 42)));
        assertFalse(m.greaterThan(Money.euro(10, 43)));
        assertFalse(m.greaterThan(Money.NULL));
        assertFalse(Money.NULL.greaterThan(m));

        try {
            m.greaterThan(Money.usd(10, 42));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            m.greaterThan(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.greaterThan(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testGreaterThanOrEqual() {
        Money m = Money.euro(10, 42);
        assertTrue(m.greaterThanOrEqual(Money.euro(10, 41)));
        assertTrue(m.greaterThanOrEqual(Money.euro(10, 42)));
        assertFalse(m.greaterThanOrEqual(Money.euro(10, 43)));
        assertFalse(m.greaterThanOrEqual(Money.NULL));
        assertFalse(Money.NULL.greaterThanOrEqual(m));

        try {
            m.greaterThanOrEqual(Money.usd(10, 42));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            m.greaterThanOrEqual(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.greaterThanOrEqual(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testLessThan() {
        Money m = Money.euro(10, 42);
        assertFalse(m.lessThan(Money.euro(10, 41)));
        assertFalse(m.lessThan(Money.euro(10, 42)));
        assertTrue(m.lessThan(Money.euro(10, 43)));
        assertFalse(m.lessThan(Money.NULL));
        assertFalse(Money.NULL.lessThan(m));

        try {
            m.lessThan(Money.usd(10, 42));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            m.lessThan(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.lessThan(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testLessThanOrEqual() {
        Money m = Money.euro(10, 42);
        assertFalse(m.lessThanOrEqual(Money.euro(10, 41)));
        assertTrue(m.lessThanOrEqual(Money.euro(10, 42)));
        assertTrue(m.lessThanOrEqual(Money.euro(10, 43)));
        assertFalse(m.lessThanOrEqual(Money.NULL));
        assertFalse(Money.NULL.lessThanOrEqual(m));

        try {
            m.lessThanOrEqual(Money.usd(10, 42));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            m.lessThanOrEqual(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }

        try {
            Money.NULL.lessThanOrEqual(null);
            fail();
        } catch (NullPointerException e) {
            // Expected exception.
        }
    }

    public void testMax() {
        Money value1 = Money.euro(10, 5);
        Money value2 = Money.euro(20, 5);

        assertEquals(value2, value1.max(value2));
        assertEquals(value2, value2.max(value1));

        Money value3 = Money.euro(10, 5);
        assertSame(value1, value1.max(value3));
    }

    public void testMin() {
        Money value1 = Money.euro(10, 5);
        Money value2 = Money.euro(20, 5);

        assertEquals(value1, value1.min(value2));
        assertEquals(value1, value2.min(value1));

        Money value3 = Money.euro(10, 5);
        assertSame(value1, value1.min(value3));

    }

    public void testEqualsObject() {
        // different class
        assertFalse(Money.euro(0, 0).equals(this));

        // different currency
        assertFalse(Money.euro(0, 0).equals(Money.usd(0, 0)));

        // different major units
        assertFalse(Money.euro(0, 0).equals(Money.euro(1, 0)));

        // different minor units
        assertFalse(Money.euro(0, 12).equals(Money.euro(0, 11)));

        // other is Money.NULL
        assertFalse(Money.euro(0, 12).equals(Money.NULL));

        // this is Money.NULL
        assertFalse(Money.NULL.equals(Money.euro(1, 1)));

        // same
        assertTrue(Money.euro(3, 12).equals(Money.euro(3, 12)));

        // both Money.NULL
        assertTrue(Money.NULL.equals(Money.NULL));
    }

    public void testToString() {
        assertEquals("100.77 EUR", Money.euro(100, 77).toString());
    }

}
