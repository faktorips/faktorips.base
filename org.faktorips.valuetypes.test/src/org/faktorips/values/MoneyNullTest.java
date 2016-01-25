package org.faktorips.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Currency;

import org.junit.Test;

public class MoneyNullTest {

    private static final Money ONE_HUNDRED = new Money(100, Currency.getInstance("EUR"));

    @Test(expected = NullPointerException.class)
    public void testCompareTo_null() {
        Money.NULL.compareTo(null);
    }

    @Test
    public void testCompareTo_MoneyNull() {
        assertEquals(0, Money.NULL.compareTo(Money.NULL));
        assertEquals(0, Money.NULL.compareTo(new MoneyNull()));
    }

    @Test
    public void testCompareTo_Money() {
        assertEquals(-1, Money.NULL.compareTo(ONE_HUNDRED));
        assertEquals(1, ONE_HUNDRED.compareTo(Money.NULL));
    }

    @Test
    public void testEquals_null() {
        assertFalse(Money.NULL.equals(null));
    }

    @Test
    public void testEquals_MoneyNull() {
        assertTrue(Money.NULL.equals(Money.NULL));
        assertTrue(Money.NULL.equals(new MoneyNull()));
    }

    @Test
    public void testEquals_Money() {
        assertFalse(Money.NULL.equals(ONE_HUNDRED));
        assertFalse(ONE_HUNDRED.equals(Money.NULL));
    }

    @Test
    public void testHashCode_multipleMoneyNullInstances() {
        assertEquals(Money.NULL.hashCode(), new MoneyNull().hashCode());
    }

    @Test
    public void testHashCode_otherInstances() {
        assertTrue(ONE_HUNDRED.hashCode() != Money.NULL.hashCode());
    }
}
