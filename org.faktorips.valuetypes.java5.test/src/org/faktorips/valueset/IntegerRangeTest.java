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

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

public class IntegerRangeTest {

    @Test
    public void testConstructor() {
        IntegerRange range = new IntegerRange(5, 10);
        assertEquals(range.getLowerBound().intValue(), 5);
        assertEquals(range.getUpperBound().intValue(), 10);
    }

    @Test
    public void testSize() {
        IntegerRange range = new IntegerRange(5, 10);
        assertEquals(6, range.size());

        range = new IntegerRange(null, new Integer(10));
        assertEquals(Integer.MAX_VALUE, range.size());

        range = new IntegerRange(new Integer(10), null);
        assertEquals(Integer.MAX_VALUE, range.size());

        range = new IntegerRange(null, null);
        assertEquals(Integer.MAX_VALUE, range.size());

        range = IntegerRange.valueOf(new Integer(0), new Integer(100), 10);
        assertEquals(11, range.size());

        range = IntegerRange.valueOf(null, new Integer(100), 10);
        assertEquals(Integer.MAX_VALUE, range.size());

        range = IntegerRange.valueOf(new Integer(10), null, 10);
        assertEquals(Integer.MAX_VALUE, range.size());

    }

    @Test
    public void testValueOf() {
        assertEquals(new IntegerRange(2, 5), IntegerRange.valueOf("2", "5"));
        assertEquals(new IntegerRange(null, null), IntegerRange.valueOf("", ""));
        assertEquals(new IntegerRange(null, null), IntegerRange.valueOf(null, null));

        try {
            IntegerRange.valueOf(new Integer(0), new Integer(100), new Integer(0), false);
            fail("Expect to fail since zero step size is not allowed.");
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void testContains() {
        IntegerRange range = IntegerRange.valueOf(null, new Integer(100), 10);
        assertTrue(range.contains(30));
        assertTrue(range.contains(100));
        assertFalse(range.contains(110));
        assertFalse(range.contains(35));

        range = IntegerRange.valueOf(new Integer(10), null, 10);
        assertTrue(range.contains(30));
        assertTrue(range.contains(10));
        assertFalse(range.contains(-10));
        assertFalse(range.contains(44));
    }

    @Test
    public void testGetValues() {
        IntegerRange range = IntegerRange.valueOf(0, 100, 20);
        Set<Integer> values = range.getValues(false);
        assertEquals(6, range.size());
        assertTrue(values.contains(new Integer(0)));
        assertTrue(values.contains(new Integer(20)));
        assertTrue(values.contains(new Integer(40)));
        assertTrue(values.contains(new Integer(60)));
        assertTrue(values.contains(new Integer(80)));
        assertTrue(values.contains(new Integer(100)));

        assertFalse(values.contains(new Integer(-10)));
        assertFalse(values.contains(new Integer(50)));
        assertFalse(values.contains(new Integer(110)));
        assertFalse(values.contains(new Integer(120)));

        range = IntegerRange.valueOf(0, 100, 20, true);
        values = range.getValues(false);
        assertEquals(7, range.size());
        assertTrue(values.contains(null));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(IntegerRange.valueOf(0, 100, 20));
    }

}
