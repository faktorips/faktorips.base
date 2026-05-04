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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import java.util.Set;

import org.junit.Test;

public class IntegerRangeTest {

    @Test
    public void testEmpty() {
        IntegerRange range = IntegerRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf() {
        IntegerRange range = IntegerRange.valueOf(5, 10);

        assertThat(range.getLowerBound(), is(5));
        assertThat(range.getUpperBound(), is(10));
    }

    @Test
    public void testValueOf_EmptyLimits() {
        assertThat(IntegerRange.valueOf("", ""), is(IntegerRange.valueOf((Integer)null, (Integer)null)));
    }

    @Test
    public void testValueOf_NullLimits() {
        assertThat(IntegerRange.valueOf((String)null, (String)null),
                is(IntegerRange.valueOf((Integer)null, (Integer)null)));
    }

    @Test
    public void testValueOf_StepZero() {
        assertThrows(IllegalArgumentException.class,
                () -> IntegerRange.valueOf(Integer.valueOf(0), Integer.valueOf(100), Integer.valueOf(0), false));
    }

    @Test
    public void testSize() {
        IntegerRange range = IntegerRange.valueOf(5, 10);

        assertThat(range.size(), is(6));
    }

    @Test
    public void testSize_TooLarge() {
        IntegerRange range = IntegerRange.valueOf(-2, Integer.MAX_VALUE - 1);

        assertThrows(RuntimeException.class, range::size);
    }

    @Test
    public void testSize_NoLower() {
        IntegerRange range = IntegerRange.valueOf(null, Integer.valueOf(10));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoUpper() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(10), null);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoLimit() {
        IntegerRange range = IntegerRange.valueOf((Integer)null, (Integer)null);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_Step() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(0), Integer.valueOf(100), 10);

        assertThat(range.size(), is(11));
    }

    @Test
    public void testSize_Step_NoLower() {
        IntegerRange range = IntegerRange.valueOf(null, Integer.valueOf(100), 10);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_Step_NoUpper() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(10), null, 10);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testContains() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(50), Integer.valueOf(100), 10);

        assertThat(range.contains(30), is(false));
        assertThat(range.contains(50), is(true));
        assertThat(range.contains(80), is(true));
        assertThat(range.contains(100), is(true));
        assertThat(range.contains(110), is(false));
        assertThat(range.contains(35), is(false));
    }

    @Test
    public void testContains_NoLower() {
        IntegerRange range = IntegerRange.valueOf(null, Integer.valueOf(100), 10);

        assertThat(range.contains(30), is(true));
        assertThat(range.contains(100), is(true));
        assertThat(range.contains(110), is(false));
        assertThat(range.contains(35), is(false));
    }

    @Test
    public void testContains_NoUpper() {
        IntegerRange range = IntegerRange.valueOf(Integer.valueOf(10), null, 10);

        assertThat(range.contains(30), is(true));
        assertThat(range.contains(10), is(true));
        assertThat(range.contains(-10), is(false));
        assertThat(range.contains(44), is(false));
    }

    @Test
    public void testContains_Null() {
        IntegerRange range = IntegerRange.valueOf(10, 100, 10, true);

        assertThat(range.contains(null), is(true));
    }

    @Test
    public void testContains_Empty() {
        IntegerRange range = IntegerRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(1), is(false));
    }

    @Test
    public void testGetValues() {
        IntegerRange range = IntegerRange.valueOf(0, 100, 20);

        Set<Integer> values = range.getValues(false);

        assertThat(range.size(), is(6));
        assertThat(values.contains(Integer.valueOf(0)), is(true));
        assertThat(values.contains(Integer.valueOf(20)), is(true));
        assertThat(values.contains(Integer.valueOf(40)), is(true));
        assertThat(values.contains(Integer.valueOf(60)), is(true));
        assertThat(values.contains(Integer.valueOf(80)), is(true));
        assertThat(values.contains(Integer.valueOf(100)), is(true));

        assertThat(values.contains(Integer.valueOf(-10)), is(false));
        assertThat(values.contains(Integer.valueOf(50)), is(false));
        assertThat(values.contains(Integer.valueOf(110)), is(false));
        assertThat(values.contains(Integer.valueOf(120)), is(false));
    }

    @Test
    public void testGetValues_WithNull() {
        IntegerRange range = IntegerRange.valueOf(0, 100, 20, true);

        Set<Integer> values = range.getValues(false);

        assertThat(range.size(), is(7));
        assertThat(values.contains(Integer.valueOf(0)), is(true));
        assertThat(values.contains(Integer.valueOf(20)), is(true));
        assertThat(values.contains(Integer.valueOf(40)), is(true));
        assertThat(values.contains(Integer.valueOf(60)), is(true));
        assertThat(values.contains(Integer.valueOf(80)), is(true));
        assertThat(values.contains(Integer.valueOf(100)), is(true));
        assertThat(values.contains(Integer.valueOf(-10)), is(false));
        assertThat(values.contains(Integer.valueOf(50)), is(false));
        assertThat(values.contains(Integer.valueOf(110)), is(false));
        assertThat(values.contains(Integer.valueOf(120)), is(false));
        assertThat(values.contains(null), is(true));
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, true, false);

        assertThat(range.getLowerBound(), is(Integer.valueOf(5)));
        assertThat(range.getUpperBound(), is(Integer.valueOf(10)));
        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(5), is(false));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(10), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, true, true);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(true));
        assertThat(range.contains(5), is(false));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
    }

    @Test
    public void testGetValues_WithBothOpenBounds() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, true, true);

        Set<Integer> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(5), is(false));
        assertThat(values.contains(6), is(true));
        assertThat(values.contains(9), is(true));
        assertThat(values.contains(10), is(false));
    }

    @Test
    public void testGetValues_WithLowerOpenBound() {
        IntegerRange range = IntegerRange.valueOf("0", "10", "2", false, true, false);

        Set<Integer> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(0), is(false));
        assertThat(values.contains(2), is(true));
        assertThat(values.contains(10), is(true));
    }

    @Test
    public void testGetValues_WithUpperOpenBound() {
        IntegerRange range = IntegerRange.valueOf("0", "10", "2", false, false, true);

        Set<Integer> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(0), is(true));
        assertThat(values.contains(8), is(true));
        assertThat(values.contains(10), is(false));
    }

    @Test
    public void testSize_WithBothOpenBounds() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndNull() {
        IntegerRange range = IntegerRange.valueOf("5", "10", "1", true, true, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testContains_WithStepAndOpenLowerBound() {
        IntegerRange range = IntegerRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.contains(0), is(false));
        assertThat(range.contains(3), is(true));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
        assertThat(range.contains(4), is(false));
    }

    @Test
    public void testContains_WithStepAndOpenUpperBound() {
        IntegerRange range = IntegerRange.valueOf("0", "10", "3", false, false, true);

        assertThat(range.contains(0), is(true));
        assertThat(range.contains(3), is(true));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        IntegerRange range = IntegerRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> IntegerRange.valueOf(Integer.valueOf(0), Integer.valueOf(10), Integer.valueOf(3), false));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(IntegerRange.valueOf(0, 100, 20));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(IntegerRange.valueOf("5", "10", "1", false, true, true));
    }

    @Test
    public void testSerializable_WithLowerOpenBound() throws Exception {
        TestUtil.testSerializable(IntegerRange.valueOf("0", "10", "1", true, true, false));
    }

}
