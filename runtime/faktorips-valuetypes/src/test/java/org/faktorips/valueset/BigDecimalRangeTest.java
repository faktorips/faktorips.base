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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.Test;

public class BigDecimalRangeTest {

    @Test
    public void testEmpty() {
        BigDecimalRange range = BigDecimalRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf_BoundsEmpty() {
        BigDecimalRange range = BigDecimalRange.valueOf("", "", "", false);

        assertThat(range, is(not(BigDecimalRange.empty())));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf() {
        BigDecimalRange range = BigDecimalRange.valueOf("1.25", "5.67");
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertThat(lower, is(BigDecimal.valueOf(125, 2)));
        assertThat(upper, is(BigDecimal.valueOf(567, 2)));
    }

    @Test
    public void testValueOf_UpperAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", null, null, false);

        assertThat(range, is(BigDecimalRange.valueOf(BigDecimal.valueOf(0), null)));
    }

    @Test
    public void testValueOf_LowerAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, "0", null, false);

        assertThat(range, is(BigDecimalRange.valueOf(null, BigDecimal.valueOf(0))));
    }

    @Test
    public void testValueOf_LowerAndUpperBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, null, "0", true);

        assertThat(range, is(BigDecimalRange.valueOf(null, null, BigDecimal.valueOf(0), true)));
    }

    @Test
    public void testValueOf_WithStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10, 0));

        assertThat(range.getLowerBound(), is(BigDecimal.valueOf(10)));
        assertThat(range.getUpperBound(), is(BigDecimal.valueOf(100)));
        assertThat(range.getStep(), is(BigDecimal.valueOf(10)));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf_WithStep_Cents() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(135, 2), BigDecimal.valueOf(108, 1),
                BigDecimal.valueOf(135, 2));

        assertThat(range.getLowerBound(), is(BigDecimal.valueOf(135, 2)));
        assertThat(range.getUpperBound(), is(BigDecimal.valueOf(108, 1)));
        assertThat(range.getStep(), is(BigDecimal.valueOf(135, 2)));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class, () -> BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100), BigDecimal.valueOf(12)));
    }

    @Test
    public void testValueOf_StepZero() {
        assertThrows(IllegalArgumentException.class, () -> BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100), BigDecimal.valueOf(0)));
    }

    @Test
    public void testContains() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100));

        assertThat(range.contains(BigDecimal.valueOf(30)), is(true));
        assertThat(range.contains(BigDecimal.valueOf(120)), is(false));
        assertThat(range.contains(BigDecimal.valueOf(5)), is(false));
    }

    @Test
    public void testContains_StepNull() {
        BigDecimalRange range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(10), BigDecimal.valueOf(100), null); // ?

        assertThat(range.contains(BigDecimal.valueOf(30)), is(true));
        assertThat(range.contains(BigDecimal.valueOf(120)), is(false));
        assertThat(range.contains(BigDecimal.valueOf(5)), is(false));
    }

    @Test
    public void testContains_Step() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10));

        assertThat(range.contains(BigDecimal.valueOf(30, 0)), is(true));
        assertThat(range.contains(BigDecimal.valueOf(35, 0)), is(false));
    }

    @Test
    public void testContainsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(0),
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), true);

        assertThat(range.contains(null), is(true));
    }

    @Test
    public void testGetValues_NoStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100));

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues_NullStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100), null);

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues_NoUpper() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10), null,
                BigDecimal.valueOf(10));

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10));

        Set<BigDecimal> values = range.getValues(false);

        assertThat(values.size(), is(10));
        assertThat(values.contains(BigDecimal.valueOf(100, 0)), is(true));
        assertThat(values.contains(BigDecimal.valueOf(70, 0)), is(true));
        assertThat(values.contains(BigDecimal.valueOf(10, 0)), is(true));
    }

    @Test
    public void testGetValues_WithNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10), true);

        Set<BigDecimal> values = range.getValues(false);

        assertThat(values.size(), is(11));
        assertThat(values.contains(BigDecimal.valueOf(100, 0)), is(true));
        assertThat(values.contains(BigDecimal.valueOf(70, 0)), is(true));
        assertThat(values.contains(BigDecimal.valueOf(10, 0)), is(true));
        assertThat(values.contains(null), is(true));
    }

    @Test
    public void testSize() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(5), BigDecimal.valueOf(10),
                BigDecimal.valueOf(1));

        assertThat(range.size(), is(6));
    }

    @Test
    public void testSize_TooLarge() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(0), BigDecimal.valueOf(Integer.MAX_VALUE),
                BigDecimal.valueOf(1, 2));

        assertThrows(RuntimeException.class, () -> range.size());
    }

    @Test
    public void testSize_NoLower() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, BigDecimal.valueOf(10),
                BigDecimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoUpper() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(5), null,
                BigDecimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoLimit() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, null,
                BigDecimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_Step() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(0), BigDecimal.valueOf(100),
                BigDecimal.valueOf(10));

        assertThat(range.size(), is(11));
    }

    @Test
    public void testSerializable() throws Exception {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10),
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), true);
        TestUtil.testSerializable(range);
    }

    @Test
    public void testGetValuesOfOfBigDecimalRangeWithoutStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        assertThat(range.getValues(true).contains(BigDecimal.valueOf(10)), is(true));
    }

    @Test
    public void testSizeOfBigDecimalRangeWithEqualBoundariesHavingDifferentPrecision() {
        BigDecimalRange range = BigDecimalRange.valueOf(new BigDecimal("10"), new BigDecimal("10.00"));
        assertThat(range.size(), is(1));
    }

    @Test
    public void testContains_Empty() {
        BigDecimalRange range = BigDecimalRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(new BigDecimal("10.00")), is(false));
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        BigDecimalRange range = BigDecimalRange.valueOf("5", "10", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(new BigDecimal("5")), is(false));
        assertThat(range.contains(new BigDecimal("5.01")), is(true));
        assertThat(range.contains(new BigDecimal("10")), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        BigDecimalRange range = BigDecimalRange.valueOf("5", "10", null, false, true, true);

        assertThat(range.contains(new BigDecimal("5")), is(false));
        assertThat(range.contains(new BigDecimal("5.01")), is(true));
        assertThat(range.contains(new BigDecimal("9.99")), is(true));
        assertThat(range.contains(new BigDecimal("10")), is(false));
    }

    @Test
    public void testGetValues_WithOpenBoundsAndStep() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "2", false, true, false);

        Set<BigDecimal> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(new BigDecimal("0")), is(false));
        assertThat(values.contains(new BigDecimal("2")), is(true));
        assertThat(values.contains(new BigDecimal("10")), is(true));
    }

    @Test
    public void testGetValues_WithBothOpenBoundsAndStep() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "2", false, true, true);

        Set<BigDecimal> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(new BigDecimal("0")), is(false));
        assertThat(values.contains(new BigDecimal("2")), is(true));
        assertThat(values.contains(new BigDecimal("8")), is(true));
        assertThat(values.contains(new BigDecimal("10")), is(false));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testValueOf_WithNullLowerBoundAndOpenFlag() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, "10", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.contains(new BigDecimal("5")), is(true));
        assertThat(range.contains(new BigDecimal("10")), is(true));
        assertThat(range.contains(new BigDecimal("11")), is(false));
    }

    @Test
    public void testValueOf_WithNullUpperBoundAndOpenFlag() {
        BigDecimalRange range = BigDecimalRange.valueOf("5", null, null, false, false, true);

        assertThat(range.isUpperBoundOpen(), is(true));
        assertThat(range.contains(new BigDecimal("4")), is(false));
        assertThat(range.contains(new BigDecimal("5")), is(true));
        assertThat(range.contains(new BigDecimal("100")), is(true));
    }

    @Test
    public void testContains_WithUpperOpenBound() {
        BigDecimalRange range = BigDecimalRange.valueOf("5", "10", null, false, false, true);

        assertThat(range.contains(new BigDecimal("4.99")), is(false));
        assertThat(range.contains(new BigDecimal("5")), is(true));
        assertThat(range.contains(new BigDecimal("9.99")), is(true));
        assertThat(range.contains(new BigDecimal("10")), is(false));
    }

    @Test
    public void testContains_WithLowerOpenBound() {
        BigDecimalRange range = BigDecimalRange.valueOf("5", "10", null, false, true, false);

        assertThat(range.contains(new BigDecimal("4.99")), is(false));
        assertThat(range.contains(new BigDecimal("5")), is(false));
        assertThat(range.contains(new BigDecimal("5.01")), is(true));
        assertThat(range.contains(new BigDecimal("10")), is(true));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndStep() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "2", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "2", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", "10", "2", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(BigDecimalRange.valueOf("5", "10", null, false, true, true));
    }

}
