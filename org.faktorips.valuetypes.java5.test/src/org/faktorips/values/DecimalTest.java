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

package org.faktorips.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class DecimalTest {
    @Test
    public void testDoubleValue() {
        Decimal d = Decimal.valueOf("5.312");
        assertEquals(5.312, d.doubleValue(), 0);
    }

    @Test
    public void testFloatValue() {
        Decimal d = Decimal.valueOf("123.98");
        assertEquals(123.98, d.floatValue(), 0.001);
    }

    @Test
    public void testIntValue() {
        Decimal d = Decimal.valueOf("56");
        assertEquals(56, d.intValue());

        d = Decimal.valueOf("65.77");
        assertEquals(65, d.intValue());
    }

    @Test
    public void testLongValue() {
        Decimal d = Decimal.valueOf("56");
        assertEquals(56L, d.longValue());

        d = Decimal.valueOf("65.77");
        assertEquals(65L, d.longValue());
    }

    @Test
    public void testValueOfString() {
        Decimal d = Decimal.valueOf("3.45");
        assertEquals(Decimal.valueOf(345, 2), d);

        d = Decimal.valueOf("3.");
        assertEquals(Decimal.valueOf(30, 1), d);

        d = Decimal.valueOf("-3.45");
        assertEquals(Decimal.valueOf(-345, 2), d);

        assertTrue(Decimal.valueOf((String)null).isNull());
    }

    @Test
    public void testSymmetryOfToStringAndValueOfForNull() {
        Decimal decNull = Decimal.NULL;
        assertTrue(Decimal.valueOf(decNull.toString()).isNull());
    }

    @Test
    public void testValueOfInteger() {
        Decimal expected = Decimal.valueOf("42");
        assertEquals(expected, Decimal.valueOf(new Integer(42)));

        expected = Decimal.valueOf("-42");
        assertEquals(expected, Decimal.valueOf(new Integer(-42)));

        assertTrue(Decimal.valueOf((Integer)null).isNull());
    }

    @Test
    public void testValueOfBigDecimal() {
        Decimal d = Decimal.valueOf(BigDecimal.valueOf(93245, 4));
        assertEquals(Decimal.valueOf(93245, 4), d);

        assertTrue(Decimal.valueOf((BigDecimal)null).isNull());

    }

    @Test
    public void testValueOflongint() {
        Decimal d = Decimal.valueOf(6541, 1);
        assertEquals(Decimal.valueOf("654.1"), d);
    }

    @Test
    public void testIsNull() {
        Decimal d = Decimal.valueOf(6541, 1);
        assertFalse(d.isNull());

        assertTrue(Decimal.NULL.isNull());
    }

    @Test
    public void testCompareToDecimal() {
        Decimal d1 = Decimal.valueOf("3.45");
        Decimal d2 = Decimal.valueOf("3.46");
        assertTrue(d1.compareTo(d2) < 0);
        assertTrue(d2.compareTo(d1) > 0);
        assertTrue(d1.compareTo(Decimal.valueOf("3.45")) == 0);
    }

    @Test
    public void testEqualsObject() {
        Decimal d = Decimal.valueOf("3.45");

        assertFalse(d.equals(this));
        assertFalse(d.equals(null));
        assertFalse(d.equals(Decimal.NULL));
        assertFalse(Decimal.NULL.equals(d));
        assertFalse(d.equals(Decimal.valueOf("3.46")));
        assertTrue(Decimal.NULL.equals(Decimal.NULL));

        assertTrue(d.equals(Decimal.valueOf("3.450")));
        assertTrue(Decimal.valueOf("3.450").equals(d));
        assertTrue(d.equals(Decimal.valueOf("3.45")));
        assertTrue(Decimal.valueOf("1").equals(Decimal.valueOf("1.0")));
    }

    @Test
    public void testHashCode() {
        assertEquals(Decimal.valueOf("1").hashCode(), Decimal.valueOf("1.0").hashCode());
        assertFalse(Decimal.valueOf("1").hashCode() == Decimal.valueOf("2").hashCode());
        assertFalse(Decimal.valueOf("1").hashCode() == Decimal.valueOf("10").hashCode());
        assertFalse(Decimal.valueOf("1.0").hashCode() == Decimal.valueOf("10").hashCode());
        assertEquals(Decimal.valueOf("1.0").hashCode(), Decimal.valueOf("1.00").hashCode());
        assertEquals(Decimal.valueOf("0.0").hashCode(), Decimal.valueOf("0").hashCode());

    }

    @Test
    public void testToString() {
        assertEquals("3", Decimal.valueOf("3").toString());
        assertEquals("3.50", Decimal.valueOf("3.50").toString());
    }

    @Test
    public void testAdd_Decimal() {
        Decimal d1 = Decimal.valueOf("1.340");
        Decimal d2 = Decimal.valueOf("8.66");
        assertEquals(Decimal.valueOf("10.000"), d1.add(d2));
        assertEquals(Decimal.valueOf("10.000"), d2.add(d1));

        assertTrue(d1.add((Decimal)null).isNull());
        assertTrue(d1.add(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.add(d1).isNull());
    }

    @Test
    public void testAdd_Integer() {
        Decimal d1 = Decimal.valueOf("1.340");
        assertEquals(Decimal.valueOf("3.340"), d1.add(new Integer(2)));

        assertTrue(d1.add((Integer)null).isNull());
        assertTrue(Decimal.NULL.add(new Integer(2)).isNull());
    }

    @Test
    public void testSubtract() {
        Decimal d1 = Decimal.valueOf("10");
        Decimal d2 = Decimal.valueOf("8.66");
        assertEquals(Decimal.valueOf("1.34"), d1.subtract(d2));
        assertEquals(Decimal.valueOf("-1.34"), d2.subtract(d1));

        assertTrue(d1.subtract(null).isNull());
        assertTrue(d1.subtract(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.subtract(d1).isNull());
    }

    @Test
    public void testMultiply_Decimal() {
        Decimal d1 = Decimal.valueOf("1.1");
        Decimal d2 = Decimal.valueOf("2.20");
        assertEquals(Decimal.valueOf("2.420"), d1.multiply(d2));
        assertEquals(Decimal.valueOf("2.420"), d2.multiply(d1));

        assertTrue(d1.multiply((Decimal)null).isNull());
        assertTrue(d1.multiply(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.multiply(d1).isNull());
    }

    @Test
    public void testMultiply_Money() {
        Decimal d = Decimal.valueOf("0.25");
        assertEquals(Money.euro(25), d.multiply(Money.euro(100), BigDecimal.ROUND_HALF_UP));

        d = Decimal.valueOf("0.3333");
        assertEquals(Money.euro(3, 33), d.multiply(Money.euro(10, 0), BigDecimal.ROUND_HALF_UP));

        d = Decimal.valueOf("0.3335");
        assertEquals(Money.euro(3, 34), d.multiply(Money.euro(10, 0), BigDecimal.ROUND_HALF_UP));

        assertTrue(d.multiply((Money)null, 2).isNull());
        assertTrue(d.multiply(Money.NULL, 2).isNull());
        assertTrue(Decimal.NULL.multiply(Money.euro(1), 2).isNull());
    }

    @Test
    public void testMultiply_Integer() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply(new Integer(2)));

        assertTrue(d1.multiply((Integer)null).isNull());
        assertTrue(Decimal.NULL.multiply(new Integer(2)).isNull());
    }

    @Test
    public void testMultiply_int() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply(2));

        assertTrue(Decimal.NULL.multiply(2).isNull());
    }

    @Test
    public void testMultiply_long() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply((long)2));

        assertTrue(Decimal.NULL.multiply((long)2).isNull());
    }

    @Test
    public void testGreaterThan() {
        Decimal d = Decimal.valueOf("10.11");
        assertTrue(d.greaterThan(Decimal.valueOf("10.10")));
        assertTrue(d.greaterThan(Decimal.valueOf("10.109")));
        assertTrue(d.greaterThan(Decimal.valueOf("10.1")));

        assertFalse(d.greaterThan(Decimal.valueOf("10.12")));
        assertFalse(d.greaterThan(Decimal.valueOf("10.11")));
        assertFalse(d.greaterThan(Decimal.valueOf("10.1101")));

        assertFalse(d.greaterThan(Decimal.NULL));
        assertFalse(d.greaterThan(null));
    }

    @Test
    public void testGreaterThanOrEquals() {
        Decimal d = Decimal.valueOf("10.11");
        assertTrue(d.greaterThanOrEqual(Decimal.valueOf("10.10")));
        assertTrue(d.greaterThanOrEqual(Decimal.valueOf("10.109")));
        assertTrue(d.greaterThanOrEqual(Decimal.valueOf("10.1")));
        assertTrue(d.greaterThanOrEqual(Decimal.valueOf("10.11")));
        assertTrue(d.greaterThanOrEqual(Decimal.valueOf("10.1100")));

        assertFalse(d.greaterThanOrEqual(Decimal.valueOf("10.12")));
        assertFalse(d.greaterThanOrEqual(Decimal.valueOf("10.1101")));

        assertFalse(d.greaterThanOrEqual(Decimal.NULL));
        assertFalse(d.greaterThanOrEqual(null));
    }

    @Test
    public void testLessThan() {
        Decimal d = Decimal.valueOf("3.45");
        assertTrue(d.lessThan(Decimal.valueOf("3.46")));
        assertTrue(d.lessThan(Decimal.valueOf("3.4501")));

        assertFalse(d.lessThan(Decimal.valueOf("3.45")));
        assertFalse(d.lessThan(Decimal.valueOf("3.44")));

        assertFalse(d.lessThan(Decimal.NULL));
        assertFalse(d.lessThan(null));
    }

    @Test
    public void testLessThanOrEqual() {
        Decimal d = Decimal.valueOf("3.45");
        assertTrue(d.lessThanOrEqual(Decimal.valueOf("3.46")));
        assertTrue(d.lessThanOrEqual(Decimal.valueOf("3.4501")));
        assertTrue(d.lessThanOrEqual(Decimal.valueOf("3.45")));
        assertTrue(d.lessThanOrEqual(Decimal.valueOf("3.4500")));

        assertFalse(d.lessThanOrEqual(Decimal.valueOf("3.44")));

        assertFalse(d.lessThanOrEqual(Decimal.NULL));
        assertFalse(d.lessThanOrEqual(null));
    }

    @SuppressWarnings("deprecation")
    // leave the test till the deprecated method is removed
    @Test
    public void testEqualsIgnoreScale() {
        Decimal d = Decimal.valueOf("100.67");
        assertTrue(d.equalsIgnoreScale(Decimal.valueOf("100.67")));
        assertTrue(d.equalsIgnoreScale(Decimal.valueOf("100.6700")));

        assertFalse(d.equalsIgnoreScale(Decimal.valueOf("100.68")));
        assertFalse(d.equalsIgnoreScale(Decimal.valueOf("100.69")));
        assertFalse(d.equalsIgnoreScale(Decimal.valueOf("100.6701")));

        assertFalse(d.equalsIgnoreScale(Decimal.NULL));
        assertFalse(d.equalsIgnoreScale(null));
    }

    @SuppressWarnings("deprecation")
    // leave the test till the deprecated method is removed
    @Test
    public void testNotEqualsIgnoreScale() {
        Decimal d = Decimal.valueOf("100.67");
        assertFalse(d.notEqualsIgnoreScale(Decimal.valueOf("100.67")));
        assertFalse(d.notEqualsIgnoreScale(Decimal.valueOf("100.6700")));

        assertTrue(d.notEqualsIgnoreScale(Decimal.valueOf("100.68")));
        assertTrue(d.notEqualsIgnoreScale(Decimal.valueOf("100.69")));
        assertTrue(d.notEqualsIgnoreScale(Decimal.valueOf("100.6701")));

        assertFalse(d.equalsIgnoreScale(Decimal.NULL));
        assertFalse(d.equalsIgnoreScale(null));
    }

    @Test
    public void testSum() throws Exception {
        assertTrue(Decimal.sum(null).isNull());
        assertEquals(Decimal.valueOf(0, 0), Decimal.sum(new Decimal[0]));
        Decimal[] values = new Decimal[] { Decimal.valueOf(10, 0), Decimal.valueOf(32, 0) };
        assertEquals(Decimal.valueOf(42, 0), Decimal.sum(values));
    }

    @Test
    public void testSetScale() {
        Decimal d = Decimal.valueOf(4215, 2);
        assertEquals(Decimal.valueOf(42, 0), d.setScale(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(Decimal.valueOf(422, 1), d.setScale(1, BigDecimal.ROUND_HALF_UP));

        assertTrue(Decimal.NULL.setScale(0, BigDecimal.ROUND_HALF_UP).isNull());
    }

    @Test
    public void testRound() {
        Decimal d = Decimal.valueOf(4215, 2);
        assertEquals(Decimal.valueOf(4200, 2), d.round(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(Decimal.valueOf(4220, 2), d.round(1, BigDecimal.ROUND_HALF_UP));

        assertEquals(Decimal.valueOf(4215, 2), d.round(3, BigDecimal.ROUND_HALF_UP));

        // null object
        assertTrue(Decimal.NULL.round(0, BigDecimal.ROUND_HALF_UP).isNull());
    }

    @Test
    public void testMax() {
        Decimal value1 = Decimal.valueOf(105, 2);
        Decimal value2 = Decimal.valueOf(205, 2);

        assertEquals(value2, value1.max(value2));
        assertEquals(value2, value2.max(value1));

        Decimal value3 = Decimal.valueOf(105, 2);
        assertSame(value1, value1.max(value3));
    }

    @Test
    public void testMin() {
        Decimal value1 = Decimal.valueOf(105, 2);
        Decimal value2 = Decimal.valueOf(205, 2);

        assertEquals(value1, value1.min(value2));
        assertEquals(value1, value2.min(value1));

        Decimal value3 = Decimal.valueOf(105, 2);
        assertSame(value1, value1.min(value3));
    }

}
