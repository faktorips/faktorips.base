/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.valueset;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class TestAbstractRangeTest extends TestCase {

    public void testConstructor() {
        IntegerRange range = new IntegerRange(null, new Integer(10));
        assertNull(range.getLowerBound());
        assertEquals(new Integer(10), range.getUpperBound());

        range = new IntegerRange(new Integer(10), null);
        assertEquals(new Integer(10), range.getLowerBound());
        assertNull(range.getUpperBound());

        range = new IntegerRange(null, null);
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());

        range = IntegerRange.valueOf((Integer)null, null, null, true);
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());

        range = IntegerRange.valueOf(null, null, new Integer(10), true);
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());

        range = IntegerRange.valueOf(new Integer(0), null, 10);
        assertEquals(new Integer(0), range.getLowerBound());
        assertNull(range.getUpperBound());
        assertEquals(new Integer(10), range.getStep());

        range = IntegerRange.valueOf(null, new Integer(100), 10);
        assertNull(range.getLowerBound());
        assertEquals(new Integer(100), range.getUpperBound());
        assertEquals(new Integer(10), range.getStep());

        range = IntegerRange.valueOf(new Integer(0), new Integer(100), 10);
        assertEquals(new Integer(0), range.getLowerBound());
        assertEquals(new Integer(100), range.getUpperBound());
        assertEquals(new Integer(10), range.getStep());

        try {
            IntegerRange.valueOf(new Integer(0), new Integer(100), 7);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testIsEmpty() {
        // range 5-10 should not be empty
        IntegerRange range = new IntegerRange(5, 10);
        assertFalse(range.isEmpty());

        // range 10-10 should not be empty
        range = new IntegerRange(10, 10);
        assertFalse(range.isEmpty());

        // range 10-5 should be empty
        range = new IntegerRange(10, 5);
        assertTrue(range.isEmpty());

        // range unbounded-5 should be empty
        range = new IntegerRange(null, new Integer(5));
        assertFalse(range.isEmpty());

        // range 5-unbounded should be empty
        range = new IntegerRange(new Integer(5), null);
        assertFalse(range.isEmpty());
    }

    public void testContains() {
        IntegerRange range = new IntegerRange(5, 10);
        assertTrue(range.contains(5));
        assertTrue(range.contains(6));
        assertTrue(range.contains(10));
        assertFalse(range.contains(null));
        assertFalse(range.contains(4));
        assertFalse(range.contains(11));

        range = new IntegerRange(null, new Integer(10));
        assertTrue(range.contains(-100));
        assertTrue(range.contains(10));
        assertFalse(range.contains(11));

        range = new IntegerRange(new Integer(10), null);
        assertTrue(range.contains(100));
        assertTrue(range.contains(10));
        assertFalse(range.contains(9));
    }

    public void testEqual() {
        IntegerRange range1 = new IntegerRange(5, 10);
        assertTrue(range1.equals(new IntegerRange(5, 10)));
        assertFalse(range1.equals(new IntegerRange(6, 10)));
        assertFalse(range1.equals(new IntegerRange(5, 11)));
        assertFalse(range1.equals(new IntegerRange(null, new Integer(10))));
        assertFalse(range1.equals(new IntegerRange(new Integer(5), null)));
        assertFalse(range1.equals(new IntegerRange(null, null)));

        range1 = new IntegerRange(null, new Integer(10));
        assertFalse(range1.equals(new IntegerRange(5, 10)));
        assertFalse(range1.equals(new IntegerRange(6, 10)));
        assertFalse(range1.equals(new IntegerRange(5, 11)));
        assertTrue(range1.equals(new IntegerRange(null, new Integer(10))));
        assertFalse(range1.equals(new IntegerRange(new Integer(5), null)));
        assertFalse(range1.equals(new IntegerRange(null, null)));

        range1 = new IntegerRange(new Integer(5), null);
        assertFalse(range1.equals(new IntegerRange(5, 10)));
        assertFalse(range1.equals(new IntegerRange(6, 10)));
        assertFalse(range1.equals(new IntegerRange(5, 11)));
        assertFalse(range1.equals(new IntegerRange(null, new Integer(10))));
        assertTrue(range1.equals(new IntegerRange(new Integer(5), null)));
        assertFalse(range1.equals(new IntegerRange(null, null)));

        range1 = new IntegerRange(null, null);
        assertFalse(range1.equals(new IntegerRange(5, 10)));
        assertFalse(range1.equals(new IntegerRange(6, 10)));
        assertFalse(range1.equals(new IntegerRange(5, 11)));
        assertFalse(range1.equals(new IntegerRange(null, new Integer(10))));
        assertFalse(range1.equals(new IntegerRange(new Integer(5), null)));
        assertTrue(range1.equals(new IntegerRange(null, null)));

        range1 = IntegerRange.valueOf(new Integer(10), new Integer(60), 5);
        IntegerRange range2 = IntegerRange.valueOf(new Integer(10), new Integer(60), 5);
        assertEquals(range1, range2);

        range2 = IntegerRange.valueOf(new Integer(10), new Integer(60), 10);
        assertFalse(range1.equals(range2));
    }

    public void testHashCode() {
        IntegerRange range1 = IntegerRange.valueOf(new Integer(10), new Integer(60), 5);
        IntegerRange range2 = IntegerRange.valueOf(new Integer(10), new Integer(60), 5);
        assertEquals(range1.hashCode(), range2.hashCode());

        range2 = IntegerRange.valueOf(new Integer(10), new Integer(60), 10);
        assertFalse(range1.hashCode() == range2.hashCode());
    }

    public void testIsDiscrete() {
        IntegerRange range = IntegerRange.valueOf(new Integer(10), new Integer(60), null, true);
        assertFalse(range.isDiscrete());
        range = IntegerRange.valueOf(new Integer(10), new Integer(60), 5);
        assertTrue(range.isDiscrete());
    }

    public void testContainsNull() {
        IntegerRange range = IntegerRange.valueOf(new Integer(10), new Integer(60), null, false);
        assertFalse(range.containsNull());

        range = IntegerRange.valueOf(new Integer(10), new Integer(60), null, true);
        assertTrue(range.containsNull());
    }

}
