/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

public class IntegerRangeTest {

    @Test
    public void testDefaultConstructor() {
        IntegerRange range = new IntegerRange();

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

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
    }

    @Test(expected = RuntimeException.class)
    public void testSize_TooLarge() {
        IntegerRange range = new IntegerRange(-2, Integer.MAX_VALUE - 1);

        range.size();
    }

    @Test
    public void testSize_NoLower() {
        IntegerRange range = new IntegerRange(null, Integer.valueOf(10));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoUpper() {
        IntegerRange range = new IntegerRange(Integer.valueOf(10), null);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoLimit() {
        IntegerRange range = new IntegerRange(null, null);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_Step() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(0), Integer.valueOf(100), 10);

        assertEquals(11, range.size());
    }

    @Test
    public void testSize_Step_NoLower() {
        IntegerRange range = IntegerRange.valueOf(null, Integer.valueOf(100), 10);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_Step_NoUpper() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(10), null, 10);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testValueOf() {
        assertEquals(new IntegerRange(2, 5), IntegerRange.valueOf("2", "5"));
    }

    @Test
    public void testValueOf_EmptyLimits() {
        assertEquals(new IntegerRange(null, null), IntegerRange.valueOf("", ""));
    }

    @Test
    public void testValueOf_NullLimits() {
        assertEquals(new IntegerRange(null, null), IntegerRange.valueOf((String)null, (String)null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepZero() {
        IntegerRange.valueOf(Integer.valueOf(0), Integer.valueOf(100), Integer.valueOf(0), false);
        fail("Expect to fail since zero step size is not allowed.");
    }

    @Test
    public void testContains() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(50), Integer.valueOf(100), 10);

        assertFalse(range.contains(30));
        assertTrue(range.contains(50));
        assertTrue(range.contains(80));
        assertTrue(range.contains(100));
        assertFalse(range.contains(110));
        assertFalse(range.contains(35));
    }

    @Test
    public void testContains_NoLower() {
        IntegerRange range = IntegerRange.valueOf(null, Integer.valueOf(100), 10);

        assertTrue(range.contains(30));
        assertTrue(range.contains(100));
        assertFalse(range.contains(110));
        assertFalse(range.contains(35));
    }

    @Test
    public void testContains_NoUpper() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(10), null, 10);

        assertTrue(range.contains(30));
        assertTrue(range.contains(10));
        assertFalse(range.contains(-10));
        assertFalse(range.contains(44));
    }

    @Test
    public void testContains_Null() {
        IntegerRange range = IntegerRange.valueOf(10, 100, 10, true);
        assertTrue(range.contains(null));
    }

    @Test
    public void testGetValues() {
        IntegerRange range = IntegerRange.valueOf(0, 100, 20);

        Set<Integer> values = range.getValues(false);

        assertEquals(6, range.size());
        assertTrue(values.contains(Integer.valueOf(0)));
        assertTrue(values.contains(Integer.valueOf(20)));
        assertTrue(values.contains(Integer.valueOf(40)));
        assertTrue(values.contains(Integer.valueOf(60)));
        assertTrue(values.contains(Integer.valueOf(80)));
        assertTrue(values.contains(Integer.valueOf(100)));

        assertFalse(values.contains(Integer.valueOf(-10)));
        assertFalse(values.contains(Integer.valueOf(50)));
        assertFalse(values.contains(Integer.valueOf(110)));
        assertFalse(values.contains(Integer.valueOf(120)));
    }

    @Test
    public void testGetValues_WithNull() {
        IntegerRange range = IntegerRange.valueOf(0, 100, 20, true);

        Set<Integer> values = range.getValues(false);

        assertEquals(7, range.size());
        assertTrue(values.contains(Integer.valueOf(0)));
        assertTrue(values.contains(Integer.valueOf(20)));
        assertTrue(values.contains(Integer.valueOf(40)));
        assertTrue(values.contains(Integer.valueOf(60)));
        assertTrue(values.contains(Integer.valueOf(80)));
        assertTrue(values.contains(Integer.valueOf(100)));
        assertFalse(values.contains(Integer.valueOf(-10)));
        assertFalse(values.contains(Integer.valueOf(50)));
        assertFalse(values.contains(Integer.valueOf(110)));
        assertFalse(values.contains(Integer.valueOf(120)));
        assertTrue(values.contains(null));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(IntegerRange.valueOf(0, 100, 20));
    }

}
