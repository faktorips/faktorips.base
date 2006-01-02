package org.faktorips.datatype;

import java.math.BigDecimal;

import junit.framework.TestCase;

/**
 *
 */
public class DecimalTest extends TestCase {

    public void testDoubleValue() {
        Decimal d = Decimal.valueOf("5.312");
        assertEquals(5.312, d.doubleValue(), 0);
    }

    public void testFloatValue() {
        Decimal d = Decimal.valueOf("123.98");
        assertEquals(123.98, d.floatValue(), 0.001);
    }

    public void testIntValue() {
        Decimal d = Decimal.valueOf("56");
        assertEquals(56, d.intValue());

        d = Decimal.valueOf("65.77");
        assertEquals(65, d.intValue());
    }

    public void testLongValue() {
        Decimal d = Decimal.valueOf("56");
        assertEquals(56, d.longValue());

        d = Decimal.valueOf("65.77");
        assertEquals(65, d.longValue());
    }

    /*
     * Class under test for Decimal valueOf(String)
     */
    public void testValueOfString() {
        Decimal d = Decimal.valueOf("3.45");
        assertEquals(Decimal.valueOf(345, 2), d);

        d = Decimal.valueOf("-3.45");
        assertEquals(Decimal.valueOf(-345, 2), d);

        assertTrue(Decimal.valueOf((String)null).isNull());
    }

    /*
     * Class under test for Decimal valueOf(String)
     */
    public void testValueOfInteger() {
        Decimal expected = Decimal.valueOf("42");
        assertEquals(expected, Decimal.valueOf(new Integer(42)));

        expected = Decimal.valueOf("-42");
        assertEquals(expected, Decimal.valueOf(new Integer(-42)));

        assertTrue(Decimal.valueOf((Integer)null).isNull());
    }

    /*
     * Class under test for Decimal valueOf(BigDecimal)
     */
    public void testValueOfBigDecimal() {
        Decimal d = Decimal.valueOf(BigDecimal.valueOf(93245, 4));
        assertEquals(Decimal.valueOf(93245, 4), d);

        assertTrue(Decimal.valueOf((BigDecimal)null).isNull());
        
    }

    /*
     * Class under test for Decimal valueOf(long, int)
     */
    public void testValueOflongint() {
        Decimal d = Decimal.valueOf(6541, 1);
        assertEquals(Decimal.valueOf("654.1"), d);
    }

    public void testIsNull() {
        Decimal d = Decimal.valueOf(6541, 1);
        assertFalse(d.isNull());
        
        assertTrue(Decimal.NULL.isNull());
    }

    public void testCompareToDecimal() {
        Decimal d1 = Decimal.valueOf("3.45"); 
        Decimal d2 = Decimal.valueOf("3.46"); 
        assertTrue(d1.compareTo(d2)<0);
        assertTrue(d2.compareTo(d1)>0);
        assertTrue(d1.compareTo(Decimal.valueOf("3.45"))==0);
    }

    /*
     * Class under test for boolean equals(Object)
     */
    public void testEqualsObject() {
        Decimal d = Decimal.valueOf("3.45"); 
        
        assertFalse(d.equals(this));
        assertFalse(d.equals(null));
        assertFalse(d.equals(Decimal.NULL));
        assertFalse(d.equals(Decimal.valueOf("3.46")));
        assertFalse(Decimal.NULL.equals(Decimal.NULL));

        assertFalse(d.equals(Decimal.valueOf("3.450")));
        assertTrue(d.equals(Decimal.valueOf("3.45")));
    }

    /*
     * Class under test for String toString()
     */
    public void testToString() {
        assertEquals("3", Decimal.valueOf("3").toString());
        assertEquals("3.50", Decimal.valueOf("3.50").toString());
    }
    
    public void testAdd_Decimal() {
        Decimal d1 = Decimal.valueOf("1.340");
        Decimal d2 = Decimal.valueOf("8.66");
        assertEquals(Decimal.valueOf("10.000"), d1.add(d2));
        assertEquals(Decimal.valueOf("10.000"), d2.add(d1));
        
        assertTrue(d1.add((Decimal)null).isNull());
        assertTrue(d1.add(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.add(d1).isNull());
    }

    public void testAdd_Integer() {
        Decimal d1 = Decimal.valueOf("1.340");
        assertEquals(Decimal.valueOf("3.340"), d1.add(new Integer(2)));
        
        assertTrue(d1.add((Integer)null).isNull());
        assertTrue(Decimal.NULL.add(new Integer(2)).isNull());
    }

    public void testSubtract() {
        Decimal d1 = Decimal.valueOf("10");
        Decimal d2 = Decimal.valueOf("8.66");
        assertEquals(Decimal.valueOf("1.34"), d1.subtract(d2));
        assertEquals(Decimal.valueOf("-1.34"), d2.subtract(d1));
        
        assertTrue(d1.subtract(null).isNull());
        assertTrue(d1.subtract(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.subtract(d1).isNull());
    }
    
    public void testMultiply_Decimal() {
        Decimal d1 = Decimal.valueOf("1.1");
        Decimal d2 = Decimal.valueOf("2.20");
        assertEquals(Decimal.valueOf("2.420"), d1.multiply(d2));
        assertEquals(Decimal.valueOf("2.420"), d2.multiply(d1));
        
        assertTrue(d1.multiply((Decimal)null).isNull());
        assertTrue(d1.multiply(Decimal.NULL).isNull());
        assertTrue(Decimal.NULL.multiply(d1).isNull());
    }
    
    public void testMultiply_Integer() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply(new Integer(2)));
        
        assertTrue(d1.multiply((Integer)null).isNull());
        assertTrue(Decimal.NULL.multiply(new Integer(2)).isNull());
    }
    
    public void testMultiply_int() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply(2));
        
        assertTrue(Decimal.NULL.multiply(2).isNull());
    }
    
    public void testMultiply_long() {
        Decimal d1 = Decimal.valueOf("1.1");
        assertEquals(Decimal.valueOf("2.2"), d1.multiply((long)2));
        
        assertTrue(Decimal.NULL.multiply((long)2).isNull());
    }
    
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
    
    public void testLessThan() {
        Decimal d = Decimal.valueOf("3.45");
        assertTrue(d.lessThan(Decimal.valueOf("3.46")));
        assertTrue(d.lessThan(Decimal.valueOf("3.4501")));
        
        assertFalse(d.lessThan(Decimal.valueOf("3.45")));
        assertFalse(d.lessThan(Decimal.valueOf("3.44")));
        
        assertFalse(d.lessThan(Decimal.NULL));
        assertFalse(d.lessThan(null));
    }
    
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
    
    public void testSum() throws Exception {
        assertTrue(Decimal.sum(null).isNull());
        assertEquals(Decimal.valueOf(0, 0), Decimal.sum(new Decimal[0]));
        Decimal[] values = new Decimal[]{Decimal.valueOf(10, 0), Decimal.valueOf(32, 0)};
        assertEquals(Decimal.valueOf(42, 0), Decimal.sum(values));
    }
    
    public void testSetScale() {
        Decimal d = Decimal.valueOf(4215, 2);
        assertEquals(Decimal.valueOf(42, 0), d.setScale(0, BigDecimal.ROUND_HALF_UP));
        assertEquals(Decimal.valueOf(422, 1), d.setScale(1, BigDecimal.ROUND_HALF_UP));

        assertTrue(Decimal.NULL.setScale(0, BigDecimal.ROUND_HALF_UP).isNull());
    }
    
}
