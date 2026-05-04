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

import static org.faktorips.valueset.TestUtil.subsetOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.faktorips.values.Decimal;
import org.junit.Test;

public class DefaultRangeTest {

    @Test
    public void testIsDiscrete() {
        TestRange rangeWithStep = new TestRange(0, 10, 1);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 10, null);

        assertThat(rangeWithoutStep.isDiscrete(), is(false));
    }

    @Test
    public void testIsDiscrete_EmptyLegacy() {
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptyLegacyRangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_EmptyLegacyNoStep() {
        TestRange emptytLegacyRangeWithoutStep = new TestRange(10, 0, null);

        assertThat(emptytLegacyRangeWithoutStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Empty() {
        TestRange emptytRange = new TestRange();

        assertThat(emptytRange.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Size1() {
        TestRange rangeWithStep = new TestRange(0, 0, 1);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_Size1NoStep() {
        TestRange rangeWithoutStep = new TestRange(0, 0, null);

        assertThat(rangeWithoutStep.isDiscrete(), is(true));
    }

    @Test
    public void testIsDiscrete_NoBoundsAndNoStep() {
        TestRange rangeWithoutStep = new TestRange(null, null, null, true);

        assertThat(rangeWithoutStep.isDiscrete(), is(false));
    }

    @Test
    public void testIsDiscrete_NoBounds() {
        TestRange rangeWithStep = new TestRange(null, null, 10, true);

        assertThat(rangeWithStep.isDiscrete(), is(true));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds_ClosedBoundsThrow() {
        assertThrows(IllegalArgumentException.class, () -> new TestRange(0, 10, 3, false, false, false));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds_LowerOpenDoesNotThrow() {
        TestRange range = new TestRange(0, 10, 3, false, true, false);

        assertThat(range.getLowerBound(), is(0));
        assertThat(range.getUpperBound(), is(10));
        assertThat(range.getStep(), is(3));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds_UpperOpenDoesNotThrow() {
        TestRange range = new TestRange(0, 10, 3, false, false, true);

        assertThat(range.getLowerBound(), is(0));
        assertThat(range.getUpperBound(), is(10));
        assertThat(range.getStep(), is(3));
    }

    @Test
    public void testCheckIfStepFitsIntoBounds_BothOpenDoNotThrow() {
        TestRange range = new TestRange(0, 10, 3, false, true, true);

        assertThat(range.getLowerBound(), is(0));
        assertThat(range.getUpperBound(), is(10));
        assertThat(range.getStep(), is(3));
    }

    @Test
    public void testEquals_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptytRange, is(emptyLegacyRangeWithStep));
    }

    @Test
    public void testHashCode_LegacyAndNewEmpty() {
        TestRange emptytRange = new TestRange();
        TestRange emptyLegacyRangeWithStep = new TestRange(10, 0, 1);

        assertThat(emptytRange.hashCode(), is(emptyLegacyRangeWithStep.hashCode()));
    }

    @Test
    public void testEquals_WithAndWithoutNull() {
        TestRange rangeWithNull = new TestRange(1, 5, 2, true);
        TestRange rangeWithoutNull = new TestRange(1, 5, 2, false);

        assertThat(rangeWithNull, is(not(rangeWithoutNull)));

        // FIPS-10440
        assertThat(new TestRange(null, null, null, false), is(not(new TestRange())));
    }

    @Test
    public void testHashCode_WithAndWithoutNull() {
        TestRange rangeWithNull = new TestRange(1, 5, 2, true);
        TestRange rangeWithoutNull = new TestRange(1, 5, 2, false);

        assertThat(rangeWithNull.hashCode(), is(not(rangeWithoutNull.hashCode())));
    }

    @Test
    public void testHashCode_NullValues() {
        TestRange rangeWithNull = new TestRange(null, 5, null, true);
        TestRange rangeWithoutNull = new TestRange(0, 5, null, true);

        assertThat(rangeWithNull.hashCode(), is(not(rangeWithoutNull.hashCode())));
    }

    @Test
    public void testIsUnrestricted_EmptyRange_includesNull() {
        TestRange emptyRangeWithoutNull = new TestRange();

        assertThat(emptyRangeWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_EmptyRange_excludesNull() {
        TestRange emptyRangeWithoutNull = new TestRange();

        assertThat(emptyRangeWithoutNull.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNull_includesNull() {
        TestRange emptyWithNull = new TestRange(null, null, null, true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithNull_excludesNull() {
        TestRange emptyWithNull = new TestRange(null, null, null, true);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithLower_includesNull() {
        TestRange range = new TestRange(Integer.valueOf(1), null, null, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithLower_excludesNull() {
        TestRange range = new TestRange(Integer.valueOf(1), null, null, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithUpper_excludesNull() {
        TestRange range = new TestRange(null, Integer.valueOf(10), null, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithUpper_includesNull() {
        TestRange range = new TestRange(null, Integer.valueOf(10), null, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithStep_includesNull() {
        TestRange range = new TestRange(null, null, Integer.valueOf(10), true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithStep_excludesNull() {
        TestRange range = new TestRange(null, null, Integer.valueOf(10), true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_EmptyRangeWithNullObject_includesNull() {
        DecimalRange emptyRangeWithoutNull = DecimalRange.empty();

        assertThat(emptyRangeWithoutNull.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_EmptyRangeWithNullObject_excludesNull() {
        DecimalRange emptyRangeWithoutNull = DecimalRange.empty();

        assertThat(emptyRangeWithoutNull.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObject_includesNull() {
        DecimalRange emptyWithNull = DecimalRange.valueOf(Decimal.NULL, Decimal.NULL, Decimal.NULL, true);

        assertThat(emptyWithNull.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObject_excludesNull() {
        DecimalRange emptyWithNull = DecimalRange.valueOf(Decimal.NULL, Decimal.NULL, Decimal.NULL, true);

        assertThat(emptyWithNull.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithLower_includesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(1), Decimal.NULL, Decimal.NULL, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithLower_excludesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(1), Decimal.NULL, Decimal.NULL, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithUpper_excludesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.NULL, Decimal.valueOf(10), Decimal.NULL, true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithUpper_includesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.NULL, Decimal.valueOf(10), Decimal.NULL, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithStep_includesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.NULL, Decimal.NULL, Decimal.valueOf(10), true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_RangeWithNullObjectWithStep_excludesNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.NULL, Decimal.NULL, Decimal.valueOf(10), true);

        assertThat(range.isUnrestricted(true), is(false));
    }

    @Test
    public void testSizeForDiscreteValuesExcludingNull() {
        LocalDate now = LocalDate.now();
        TestRangeForDiscreteValues dateRange = new TestRangeForDiscreteValues(now, now, null, false);

        assertThat(dateRange.sizeForDiscreteValuesExcludingNull(), is(1));
    }

    @Test
    public void testSizeForDiscreteValuesExcludingNull_withStep() {
        LocalDate now = LocalDate.now();

        assertThrows(RuntimeException.class, () -> new TestRangeForDiscreteValues(now, now, now, false));
    }

    @Test
    public void testSizeForDiscreteValuesExcludingNull_UnequalBounds() {
        LocalDate now = LocalDate.now();

        assertThrows(RuntimeException.class,
                () -> new TestRangeForDiscreteValues(null, now, null, false).sizeForDiscreteValuesExcludingNull());
        assertThrows(RuntimeException.class,
                () -> new TestRangeForDiscreteValues(now, null, null, false).sizeForDiscreteValuesExcludingNull());
    }

    @Test
    public void testGetValues_UpperIsLower() {
        assertThat(new TestRange(5, 5, null).getValues(true), hasItems(Integer.valueOf(5)));
        assertThat(new TestRange(5, 5, null, true).getValues(true), hasItems(Integer.valueOf(5)));
        assertThat(new TestRange(5, 5, null, true).getValues(false), hasItems((Integer)null, Integer.valueOf(5)));
    }

    @Test
    public void testGetValues_SortedValues() {

        BigDecimalRange range = BigDecimalRange.valueOf("1", "5", "0.5");

        Set<BigDecimal> values = range.getValues(true);

        assertThat(values, contains(
                BigDecimal.ONE,
                new BigDecimal("1.5"),
                new BigDecimal("2.0"),
                new BigDecimal("2.5"),
                new BigDecimal("3.0"),
                new BigDecimal("3.5"),
                new BigDecimal("4.0"),
                new BigDecimal("4.5"),
                new BigDecimal("5.0")));
    }

    @Test
    public void testGetValues_NoStep() {
        assertThrows(RuntimeException.class, () -> new TestRange(1, 5, null).getValues(true));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf() {
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new UnrestrictedValueSet<>(true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new UnrestrictedValueSet<>(true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new UnrestrictedValueSet<>(false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new UnrestrictedValueSet<>(false)))));
        assertThat(new TestRange(null, null, null, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, null, null, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, null, null, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(null, null, null, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(0, 10, null, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, 10, null, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, 10, null, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(0, 10, null, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(null, 10, null, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, 10, null, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, 10, null, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(null, 10, null, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(0, null, null, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, null, null, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, null, null, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(0, null, null, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(null, 10, 2, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, 10, 2, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(null, 10, 2, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(null, 10, 2, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(0, null, 2, true), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, null, 2, false), is(subsetOf(new TestRange(null, null, null, true))));
        assertThat(new TestRange(0, null, 2, false), is(subsetOf(new TestRange(null, null, null, false))));
        assertThat(new TestRange(0, null, 2, true), is(not(subsetOf(new TestRange(null, null, null, false)))));
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new TestRange(null, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, 100, null, false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new TestRange(null, 100, null, false)))));
        assertThat(new TestRange(null, 10, 2, true), is(not(subsetOf(new TestRange(0, 10, 2, true)))));
        assertThat(new TestRange(null, 10, 2, false), is(not(subsetOf(new TestRange(0, 10, 2, true)))));
        assertThat(new TestRange(null, 10, 2, false), is(not(subsetOf(new TestRange(0, 10, 2, false)))));
        assertThat(new TestRange(null, 10, 2, true), is(not(subsetOf(new TestRange(0, 10, 2, false)))));
        assertThat(new TestRange(0, null, 2, true), is(not(subsetOf(new TestRange(0, 10, 2, true)))));
        assertThat(new TestRange(0, null, 2, false), is(not(subsetOf(new TestRange(0, 10, 2, true)))));
        assertThat(new TestRange(0, null, 2, false), is(not(subsetOf(new TestRange(0, 10, 2, false)))));
        assertThat(new TestRange(0, null, 2, true), is(not(subsetOf(new TestRange(0, 10, 2, false)))));
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new TestRange(null, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(null, 100, null, false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new TestRange(null, 100, null, false)))));
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new TestRange(0, null, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(0, null, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(0, null, null, false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new TestRange(0, null, null, false)))));
        assertThat(new TestRange(0, 10, 2, true), is(subsetOf(new TestRange(0, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(0, 100, null, true))));
        assertThat(new TestRange(0, 10, 2, false), is(subsetOf(new TestRange(0, 100, null, false))));
        assertThat(new TestRange(0, 10, 2, true), is(not(subsetOf(new TestRange(0, 100, null, false)))));
        assertThat(new TestRange(2, 10, 4, true), is(subsetOf(new TestRange(0, 20, 2, true))));
        assertThat(new TestRange(2, 10, 4, false), is(subsetOf(new TestRange(0, 20, 2, true))));
        assertThat(new TestRange(2, 10, 4, false), is(subsetOf(new TestRange(0, 20, 2, false))));
        assertThat(new TestRange(2, 10, 4, true), is(not(subsetOf(new TestRange(0, 20, 2, false)))));
        assertThat(new TestRange(0, 10, 5, true), is(not(subsetOf(new TestRange(0, 20, 2, true)))));
        assertThat(new TestRange(0, 10, 5, false), is(not(subsetOf(new TestRange(0, 20, 2, true)))));
        assertThat(new TestRange(0, 10, 5, false), is(not(subsetOf(new TestRange(0, 20, 2, false)))));
        assertThat(new TestRange(2, 10, 4, true), is(not(subsetOf(new TestRange(0, 20, 2, false)))));
        assertThat(new TestRange(1, 11, 2, false), is(not(subsetOf(new TestRange(0, 20, 2, false)))));
        assertThat(IntegerRange.valueOf(0, 10, 2, false),
                is(not(subsetOf((Range)LongRange.valueOf(0L, 20L, 2L, false)))));
    }

    @Test
    public void testContains_LowerBoundOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, false);

        assertThat(range.contains(4), is(false));
        assertThat(range.contains(5), is(false));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(10), is(true));
        assertThat(range.contains(11), is(false));
    }

    @Test
    public void testContains_UpperBoundOpen() {
        TestRange range = new TestRange(5, 10, null, false, false, true);

        assertThat(range.contains(4), is(false));
        assertThat(range.contains(5), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
        assertThat(range.contains(11), is(false));
    }

    @Test
    public void testContains_BothBoundsOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, true);

        assertThat(range.contains(5), is(false));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
    }

    @Test
    public void testContains_BothBoundsClosed() {
        TestRange range = new TestRange(5, 10, null, false, false, false);

        assertThat(range.contains(5), is(true));
        assertThat(range.contains(10), is(true));
    }

    @Test
    public void testIsEmpty_EqualBoundsLowerOpen() {
        TestRange range = new TestRange(5, 5, null, false, true, false);

        assertThat(range.isEmpty(), is(true));
    }

    @Test
    public void testIsEmpty_EqualBoundsUpperOpen() {
        TestRange range = new TestRange(5, 5, null, false, false, true);

        assertThat(range.isEmpty(), is(true));
    }

    @Test
    public void testIsEmpty_EqualBoundsBothOpen() {
        TestRange range = new TestRange(5, 5, null, false, true, true);

        assertThat(range.isEmpty(), is(true));
    }

    @Test
    public void testIsEmpty_EqualBoundsBothClosed() {
        TestRange range = new TestRange(5, 5, null, false, false, false);

        assertThat(range.isEmpty(), is(false));
    }

    @Test
    public void testIsEmpty_DifferentBoundsWithOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, true);

        assertThat(range.isEmpty(), is(false));
    }

    @Test
    public void testEquals_DifferentOpenClosed() {
        TestRange closedRange = new TestRange(5, 10, null, false, false, false);
        TestRange openRange = new TestRange(5, 10, null, false, true, false);

        assertThat(closedRange, is(not(openRange)));
    }

    @Test
    public void testEquals_SameOpenClosed() {
        TestRange range1 = new TestRange(5, 10, null, false, true, true);
        TestRange range2 = new TestRange(5, 10, null, false, true, true);

        assertThat(range1, is(range2));
    }

    @Test
    public void testHashCode_DifferentOpenClosed() {
        TestRange closedRange = new TestRange(5, 10, null, false, false, false);
        TestRange openRange = new TestRange(5, 10, null, false, true, false);

        assertThat(closedRange.hashCode(), is(not(openRange.hashCode())));
    }

    @Test
    public void testToString_Closed() {
        TestRange range = new TestRange(5, 10, null, false, false, false);

        assertThat(range.toString(), is("[5-10]"));
    }

    @Test
    public void testToString_LowerOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, false);

        assertThat(range.toString(), is("(5-10]"));
    }

    @Test
    public void testToString_UpperOpen() {
        TestRange range = new TestRange(5, 10, null, false, false, true);

        assertThat(range.toString(), is("[5-10)"));
    }

    @Test
    public void testToString_BothOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, true);

        assertThat(range.toString(), is("(5-10)"));
    }

    @Test
    public void testToString_OpenWithStep() {
        TestRange range = new TestRange(5, 10, 2, false, true, false);

        assertThat(range.toString(), is("(5-10, 2]"));
    }

    @Test
    public void testContains_NullWithOpenBounds() {
        TestRange range = new TestRange(5, 10, null, true, true, true);

        assertThat(range.contains(null), is(true));
        assertThat(range.contains(5), is(false));
    }

    @Test
    public void testIsLowerBoundOpen() {
        TestRange range = new TestRange(5, 10, null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
    }

    @Test
    public void testIsUpperBoundOpen() {
        TestRange range = new TestRange(5, 10, null, false, false, true);

        assertThat(range.isLowerBoundOpen(), is(false));
        assertThat(range.isUpperBoundOpen(), is(true));
    }

    @Test
    public void testIsUnrestricted_WithLowerOpenBoundOnNullBound() {
        TestRange range = new TestRange(null, null, null, true, true, false);

        assertThat(range.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithLowerOpenBoundOnNullBound_ExcludeNull() {
        TestRange range = new TestRange(null, null, null, true, true, false);

        assertThat(range.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithLowerOpenBoundOnNonNullBound() {
        TestRange range = new TestRange(5, null, null, true, true, false);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithUpperOpenBoundOnNullBound() {
        TestRange range = new TestRange(null, null, null, true, false, true);

        assertThat(range.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithUpperOpenBoundOnNonNullBound() {
        TestRange range = new TestRange(null, 10, null, true, false, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithBothOpenBoundsOnNullBounds() {
        TestRange range = new TestRange(null, null, null, true, true, true);

        assertThat(range.isUnrestricted(false), is(true));
    }

    @Test
    public void testIsUnrestricted_WithBothOpenBoundsOnNullBounds_ExcludeNull() {
        TestRange range = new TestRange(null, null, null, true, true, true);

        assertThat(range.isUnrestricted(true), is(true));
    }

    @Test
    public void testIsUnrestricted_WithBothOpenBoundsOnNonNullBounds() {
        TestRange range = new TestRange(5, 10, null, true, true, true);

        assertThat(range.isUnrestricted(false), is(false));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_OpenWithinClosed() {
        TestRange openRange = new TestRange(5, 10, null, false, true, true);
        TestRange closedRange = new TestRange(5, 10, null, false, false, false);

        assertThat(openRange, is(subsetOf((Range)closedRange)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_ClosedNotWithinOpen() {
        TestRange closedRange = new TestRange(5, 10, null, false, false, false);
        TestRange openRange = new TestRange(5, 10, null, false, true, true);

        assertThat(closedRange, is(not(subsetOf((Range)openRange))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_HalfOpenWithinClosed() {
        TestRange halfOpen = new TestRange(5, 10, null, false, true, false);
        TestRange closed = new TestRange(5, 10, null, false, false, false);

        assertThat(halfOpen, is(subsetOf((Range)closed)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_ClosedNotWithinUpperOpen() {
        TestRange closed = new TestRange(5, 10, null, false, false, false);
        TestRange upperOpen = new TestRange(5, 10, null, false, false, true);

        assertThat(closed, is(not(subsetOf((Range)upperOpen))));
    }

    @Test
    public void testToString_EmptyOpenRange() {
        TestRange range = new TestRange(5, 5, null, false, true, false);

        assertThat(range.toString(), is("[]"));
    }

    @Test
    public void testGetValues_EqualBoundsWithOpen() {
        TestRange range = new TestRange(5, 5, 1, false, true, false);

        Set<Integer> values = range.getValues(false);

        assertThat(values, is(Set.of()));
    }

    @Test
    public void testGetValues_WithContainsNullAndOpenBounds() {
        TestRange range = new TestRange(5, 10, 1, true, true, true);

        Set<Integer> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(null), is(true));
        assertThat(values.contains(5), is(false));
        assertThat(values.contains(6), is(true));
        assertThat(values.contains(9), is(true));
        assertThat(values.contains(10), is(false));
    }

    @Test
    public void testSize_EqualBoundsWithOpen() {
        TestRange range = new TestRange(5, 5, null, false, true, false);

        assertThat(range.size(), is(0));
    }

    @Test
    public void testContains_WithStepAndLowerOpen() {
        TestRange range = new TestRange(0, 10, 2, false, true, false);

        assertThat(range.contains(0), is(false));
        assertThat(range.contains(2), is(true));
        assertThat(range.contains(4), is(true));
        assertThat(range.contains(10), is(true));
        assertThat(range.contains(3), is(false));
    }

    @Test
    public void testContains_WithStepAndUpperOpen() {
        TestRange range = new TestRange(0, 10, 2, false, false, true);

        assertThat(range.contains(0), is(true));
        assertThat(range.contains(8), is(true));
        assertThat(range.contains(10), is(false));
        assertThat(range.contains(3), is(false));
    }

    @Test
    public void testContains_WithStepAndBothOpen() {
        TestRange range = new TestRange(0, 10, 3, false, true, true);

        assertThat(range.contains(0), is(false));
        assertThat(range.contains(3), is(true));
        assertThat(range.contains(6), is(true));
        assertThat(range.contains(9), is(true));
        assertThat(range.contains(10), is(false));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_OpenWithStepWithinClosedWithStep() {
        TestRange openRange = new TestRange(0, 10, 2, false, true, false);
        TestRange closedRange = new TestRange(0, 10, 2, false, false, false);

        assertThat(openRange, is(subsetOf((Range)closedRange)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_ClosedWithStepNotWithinOpenWithStep() {
        TestRange closedRange = new TestRange(0, 10, 2, false, false, false);
        TestRange openRange = new TestRange(0, 10, 2, false, true, true);

        assertThat(closedRange, is(not(subsetOf((Range)openRange))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_OpenWithStepWithinWiderClosed() {
        TestRange openRange = new TestRange(0, 10, 2, false, true, true);
        TestRange closedRange = new TestRange(0, 20, 2, false, false, false);

        assertThat(openRange, is(subsetOf((Range)closedRange)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_UpperOpenStepAlignedWithinClosed() {
        TestRange upperOpenRange = new TestRange(0, 10, 2, false, false, true);
        TestRange closedRange = new TestRange(0, 10, 2, false, false, false);

        assertThat(upperOpenRange, is(subsetOf((Range)closedRange)));
        assertThat(closedRange, is(not(subsetOf((Range)upperOpenRange))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf_BothOpenStepAlignedWithinClosed() {
        TestRange openRange = new TestRange(0, 12, 3, false, true, true);
        TestRange closedRange = new TestRange(0, 12, 3, false, false, false);

        assertThat(openRange, is(subsetOf((Range)closedRange)));
        assertThat(closedRange, is(not(subsetOf((Range)openRange))));
    }

    @Test
    public void testContains_WithNullLowerBoundAndOpenFlag() {
        TestRange range = new TestRange(null, 10, null, false, true, false);

        assertThat(range.contains(5), is(true));
        assertThat(range.contains(10), is(true));
        assertThat(range.contains(11), is(false));
    }

    @Test
    public void testContains_WithNullUpperBoundAndOpenFlag() {
        TestRange range = new TestRange(5, null, null, false, false, true);

        assertThat(range.contains(4), is(false));
        assertThat(range.contains(5), is(true));
        assertThat(range.contains(100), is(true));
    }

    @Test
    public void testGetValues_WithOpenBoundsAndNullLowerBound() {
        TestRange range = new TestRange(null, 10, 1, false, true, false);

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues_WithOpenBoundsAndNullUpperBound() {
        TestRange range = new TestRange(5, null, 1, false, false, true);

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testSize_WithOpenLowerBound() {
        TestRange range = new TestRange(0, 10, 2, false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithOpenUpperBound() {
        TestRange range = new TestRange(0, 10, 2, false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithBothOpenBounds() {
        TestRange range = new TestRange(0, 10, 2, false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithOpenBoundsWhereUpperNotOnStep() {
        TestRange range = new TestRange(0, 10, 3, false, true, true);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testSize_WithBothOpenBoundsWhereUpperIsOnStep() {
        // (0,9) step 3 → values 3, 6 → size 2 (upper 9 is on the grid, so excluded)
        TestRange range = new TestRange(0, 9, 3, false, true, true);

        assertThat(range.size(), is(2));
        Set<Integer> values = range.getValues(false);
        assertThat(values, is(Set.of(3, 6)));
    }

    @Test
    public void testSize_WithNullLowerAndOpenFlag() {
        TestRange range = new TestRange(null, 10, 1, false, true, false);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_WithNullUpperAndOpenFlag() {
        TestRange range = new TestRange(5, null, 1, false, false, true);

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    private static class TestRangeForDiscreteValues extends DefaultRange<LocalDate> {
        private static final long serialVersionUID = 1L;

        public TestRangeForDiscreteValues(LocalDate lower, LocalDate upper, LocalDate step, boolean containsNull) {
            super(lower, upper, step, containsNull);
        }

        @Override
        public Optional<Class<LocalDate>> getDatatype() {
            return Optional.of(LocalDate.class);
        }

        @Override
        protected boolean divisibleWithoutRest(LocalDate dividend, LocalDate divisor) {
            return false;
        }
    }

    private static class TestRange extends DefaultRange<Integer> {

        private static final long serialVersionUID = 3385179851895588865L;

        public TestRange() {
            super();
        }

        public TestRange(Integer lower, Integer upper, Integer step) {
            super(lower, upper, step, false);
        }

        public TestRange(Integer lower, Integer upper, Integer step, boolean containsNull) {
            super(lower, upper, step, containsNull);
        }

        public TestRange(Integer lower, Integer upper, Integer step, boolean containsNull,
                boolean lowerBoundOpen, boolean upperBoundOpen) {
            super(lower, upper, step, containsNull, lowerBoundOpen, upperBoundOpen);
        }

        @Override
        protected int sizeForDiscreteValuesExcludingNull() {
            int diff = Math.abs(getUpperBound() - getLowerBound());
            return diff / getStep() + 1;
        }

        @Override
        protected int sizeForDiscreteValuesWithFloor() {
            return sizeForDiscreteValuesExcludingNull();
        }

        @Override
        protected Integer getNextValue(Integer currentValue) {
            return currentValue + getStep();
        }

        @Override
        protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {
            return Math.abs(bound - value) % getStep() == 0;
        }

        @Override
        protected Integer getNullValue() {
            return null;
        }

        @Override
        public Optional<Class<Integer>> getDatatype() {
            return Optional.of(Integer.class);
        }

        @Override
        protected boolean divisibleWithoutRest(Integer dividend, Integer divisor) {
            return dividend % divisor == 0;
        }

    }
}
