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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class LongRangeTest {

    @Test
    public void testEmpty() {
        LongRange range = LongRange.empty();

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

    @Test
    public void testValueOf() {
        LongRange range = LongRange.valueOf(5L, 10L);

        assertEquals(range.getLowerBound().longValue(), 5L);
        assertEquals(range.getUpperBound().longValue(), 10L);
    }

    @Test
    public void testValueOf_EmptyLimits() {
        assertEquals(LongRange.valueOf((Long)null, (Long)null), LongRange.valueOf("", ""));
    }

    @Test
    public void testValueOf_Step() {
        LongRange range = LongRange.valueOf(10L, 100L, 10L);

        assertEquals(Long.valueOf(10), range.getLowerBound());
        assertEquals(Long.valueOf(100), range.getUpperBound());
        assertEquals(Long.valueOf(10), range.getStep());
        assertFalse(range.containsNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepMismatch() {
        LongRange.valueOf(10L, 101L, 10L);
    }

    @Test
    public void testValueOf_WithNull() {
        LongRange range = LongRange.valueOf(Long.valueOf(10), Long.valueOf(100), Long.valueOf(10), true);

        assertEquals(Long.valueOf(10), range.getLowerBound());
        assertEquals(Long.valueOf(100), range.getUpperBound());
        assertEquals(Long.valueOf(10), range.getStep());
        assertTrue(range.containsNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepMismatchWithNull() {
        LongRange.valueOf(Long.valueOf(10), Long.valueOf(101), Long.valueOf(10), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepZero() {
        LongRange.valueOf(Long.valueOf(10), Long.valueOf(101), Long.valueOf(0), true);
    }

    @Test
    public void testSize() {
        LongRange range = LongRange.valueOf(5L, 10L);

        assertEquals(6, range.size());
    }

    @Test
    public void testSize_NoLower() {
        LongRange range = LongRange.valueOf(null, Long.valueOf(10));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoUpper() {
        LongRange range = LongRange.valueOf(Long.valueOf(10), null);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoLimits() {
        LongRange range = LongRange.valueOf((Long)null, (Long)null);

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test(expected = RuntimeException.class)
    public void testSize_LargerThanIntMax() {
        LongRange range = LongRange.valueOf(1L, Integer.MAX_VALUE + 3L);

        range.size();
    }

    @Test
    public void testSize_Step() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L);

        assertEquals(6, range.size());
    }

    @Test
    public void testGetValues() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L);

        Set<Long> values = range.getValues(false);

        assertEquals(6, range.size());
        assertTrue(values.contains(Long.valueOf(100)));
        assertTrue(values.contains(Long.valueOf(300)));
        assertTrue(values.contains(Long.valueOf(500)));
        assertTrue(values.contains(Long.valueOf(1100)));
        assertFalse(values.contains(Long.valueOf(200)));
        assertFalse(values.contains(Long.valueOf(1200)));
        assertFalse(values.contains(Long.valueOf(0)));
    }

    @Test
    public void testGetValues_WithNull() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L, true);

        Set<Long> values = range.getValues(false);

        assertEquals(7, range.size());
        assertTrue(values.contains(Long.valueOf(100)));
        assertTrue(values.contains(Long.valueOf(300)));
        assertTrue(values.contains(Long.valueOf(500)));
        assertTrue(values.contains(Long.valueOf(1100)));
        assertFalse(values.contains(Long.valueOf(200)));
        assertFalse(values.contains(Long.valueOf(1200)));
        assertFalse(values.contains(Long.valueOf(0)));
        assertTrue(values.contains(null));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(LongRange.valueOf(Long.valueOf(10), Long.valueOf(100), Long.valueOf(10)));
    }

    @Test
    public void testContains() {
        assertThat(LongRange.valueOf(1L, null, 1L, true).contains(null), is(true));
        assertThat(LongRange.valueOf(1L, null, 1L, false).contains(42L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, null, false).contains(3L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, 3L, false).contains(3L), is(false));
    }

    @Test
    public void testCheckIfValueCompliesToStepIncrement() {
        assertThat(LongRange.valueOf(1L, 10L, null, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, 1L, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, 3L, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(false));
    }

}
