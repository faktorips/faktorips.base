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

    @Test(expected = RuntimeException.class)
    public void testGetValues_NoStep() {
        new TestRange(1, 5, null).getValues(true);
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
