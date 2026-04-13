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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Set;

import org.junit.Test;

public class LongRangeTest {

    @Test
    public void testEmpty() {
        LongRange range = LongRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf() {
        LongRange range = LongRange.valueOf(5L, 10L);

        assertThat(range.getLowerBound().longValue(), is(5L));
        assertThat(range.getUpperBound().longValue(), is(10L));
    }

    @Test
    public void testValueOf_EmptyLimits() {
        assertThat(LongRange.valueOf("", ""), is(LongRange.valueOf((Long)null, (Long)null)));
    }

    @Test
    public void testValueOf_Step() {
        LongRange range = LongRange.valueOf(10L, 100L, 10L);

        assertThat(range.getLowerBound(), is(Long.valueOf(10)));
        assertThat(range.getUpperBound(), is(Long.valueOf(100)));
        assertThat(range.getStep(), is(Long.valueOf(10)));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class, () -> LongRange.valueOf(10L, 101L, 10L));
    }

    @Test
    public void testValueOf_WithNull() {
        LongRange range = LongRange.valueOf(Long.valueOf(10), Long.valueOf(100), Long.valueOf(10), true);

        assertThat(range.getLowerBound(), is(Long.valueOf(10)));
        assertThat(range.getUpperBound(), is(Long.valueOf(100)));
        assertThat(range.getStep(), is(Long.valueOf(10)));
        assertThat(range.containsNull(), is(true));
    }

    @Test
    public void testValueOf_StepMismatchWithNull() {
        assertThrows(IllegalArgumentException.class,
                () -> LongRange.valueOf(Long.valueOf(10), Long.valueOf(101), Long.valueOf(10), true));
    }

    @Test
    public void testValueOf_StepZero() {
        assertThrows(IllegalArgumentException.class,
                () -> LongRange.valueOf(Long.valueOf(10), Long.valueOf(101), Long.valueOf(0), true));
    }

    @Test
    public void testSize() {
        LongRange range = LongRange.valueOf(5L, 10L);

        assertThat(range.size(), is(6));
    }

    @Test
    public void testSize_NoLower() {
        LongRange range = LongRange.valueOf(null, Long.valueOf(10));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoUpper() {
        LongRange range = LongRange.valueOf(Long.valueOf(10), null);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoLimits() {
        LongRange range = LongRange.valueOf((Long)null, (Long)null);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_LargerThanIntMax() {
        LongRange range = LongRange.valueOf(1L, Integer.MAX_VALUE + 3L);

        assertThrows(RuntimeException.class, () -> range.size());
    }

    @Test
    public void testSize_Step() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L);

        assertThat(range.size(), is(6));
    }

    @Test
    public void testGetValues() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L);

        Set<Long> values = range.getValues(false);

        assertThat(range.size(), is(6));
        assertThat(values.contains(Long.valueOf(100)), is(true));
        assertThat(values.contains(Long.valueOf(300)), is(true));
        assertThat(values.contains(Long.valueOf(500)), is(true));
        assertThat(values.contains(Long.valueOf(1100)), is(true));
        assertThat(values.contains(Long.valueOf(200)), is(false));
        assertThat(values.contains(Long.valueOf(1200)), is(false));
        assertThat(values.contains(Long.valueOf(0)), is(false));
    }

    @Test
    public void testGetValues_WithNull() {
        LongRange range = LongRange.valueOf(100L, 1100L, 200L, true);

        Set<Long> values = range.getValues(false);

        assertThat(range.size(), is(7));
        assertThat(values.contains(Long.valueOf(100)), is(true));
        assertThat(values.contains(Long.valueOf(300)), is(true));
        assertThat(values.contains(Long.valueOf(500)), is(true));
        assertThat(values.contains(Long.valueOf(1100)), is(true));
        assertThat(values.contains(Long.valueOf(200)), is(false));
        assertThat(values.contains(Long.valueOf(1200)), is(false));
        assertThat(values.contains(Long.valueOf(0)), is(false));
        assertThat(values.contains(null), is(true));
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
    public void testContains_Empty() {
        LongRange range = LongRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(1L), is(false));
    }

    @Test
    public void testCheckIfValueCompliesToStepIncrement() {
        assertThat(LongRange.valueOf(1L, 10L, null, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, 1L, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(true));
        assertThat(LongRange.valueOf(1L, 10L, 3L, false).checkIfValueCompliesToStepIncrement(3L, 10L), is(false));
    }

    @Test
    public void testValueOf_WithOpenBounds_TypedFactory() {
        LongRange range = LongRange.valueOf(5L, 10L, 1L, false, true, false);

        assertThat(range.getLowerBound(), is(Long.valueOf(5)));
        assertThat(range.getUpperBound(), is(Long.valueOf(10)));
        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(5L), is(false));
        assertThat(range.contains(6L), is(true));
        assertThat(range.contains(10L), is(true));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndNull() {
        LongRange range = LongRange.valueOf("5", "10", "1", true, true, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testContains_WithStepAndOpenUpperBound() {
        LongRange range = LongRange.valueOf("0", "10", "3", false, false, true);

        assertThat(range.contains(0L), is(true));
        assertThat(range.contains(3L), is(true));
        assertThat(range.contains(6L), is(true));
        assertThat(range.contains(9L), is(true));
        assertThat(range.contains(10L), is(false));
    }

    @Test
    public void testSerializable_WithLowerOpenBound() throws Exception {
        TestUtil.testSerializable(LongRange.valueOf("0", "10", "1", true, true, false));
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        LongRange range = LongRange.valueOf("5", "10", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(5L), is(false));
        assertThat(range.contains(6L), is(true));
        assertThat(range.contains(10L), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        LongRange range = LongRange.valueOf("5", "10", null, false, true, true);

        assertThat(range.contains(5L), is(false));
        assertThat(range.contains(6L), is(true));
        assertThat(range.contains(9L), is(true));
        assertThat(range.contains(10L), is(false));
    }

    @Test
    public void testGetValues_WithLowerOpenBoundAndStep() {
        LongRange range = LongRange.valueOf("0", "10", "2", false, true, false);

        Set<Long> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(0L), is(false));
        assertThat(values.contains(2L), is(true));
        assertThat(values.contains(10L), is(true));
    }

    @Test
    public void testGetValues_WithBothOpenBoundsAndStep() {
        LongRange range = LongRange.valueOf("0", "10", "2", false, true, true);

        Set<Long> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(0L), is(false));
        assertThat(values.contains(2L), is(true));
        assertThat(values.contains(8L), is(true));
        assertThat(values.contains(10L), is(false));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndStep() {
        LongRange range = LongRange.valueOf("5", "10", "1", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        LongRange range = LongRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testContains_WithUpperOpenBound() {
        LongRange range = LongRange.valueOf("5", "10", null, false, false, true);

        assertThat(range.contains(4L), is(false));
        assertThat(range.contains(5L), is(true));
        assertThat(range.contains(9L), is(true));
        assertThat(range.contains(10L), is(false));
    }

    @Test
    public void testContains_WithStepAndLowerOpenBound() {
        LongRange range = LongRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.contains(0L), is(false));
        assertThat(range.contains(3L), is(true));
        assertThat(range.contains(6L), is(true));
        assertThat(range.contains(9L), is(true));
        assertThat(range.contains(10L), is(false));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        LongRange range = LongRange.valueOf("5", "10", "1", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        LongRange range = LongRange.valueOf("5", "10", "1", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testGetValues_WithUpperOpenBoundAndStep() {
        LongRange range = LongRange.valueOf("0", "10", "2", false, false, true);

        Set<Long> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(0L), is(true));
        assertThat(values.contains(8L), is(true));
        assertThat(values.contains(10L), is(false));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(LongRange.valueOf("5", "10", "1", false, true, true));
    }

}
