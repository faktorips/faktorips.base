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

import org.faktorips.values.Money;
import org.junit.Test;

/**
 * Test class for {@link MoneyRange}
 * 
 * @author Peter Kuntz
 */
public class MoneyRangeTest {

    @Test
    public void testEmpty() {
        MoneyRange range = MoneyRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf() {
        MoneyRange range = MoneyRange.valueOf("1.25 EUR", "5.67 EUR");

        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertThat(lower, is(Money.euro(1, 25)));
        assertThat(upper, is(Money.euro(5, 67)));
        assertThat(range.containsNull(), is(false));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(10, 0), false));
    }

    @Test
    public void testValueOf_StepZero() {
        assertThrows(IllegalArgumentException.class,
                () -> MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(0, 0), false));
    }

    @Test
    public void testValueOf_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertThat(range.getLowerBound(), is(Money.euro(10, 0)));
        assertThat(range.getUpperBound(), is(Money.euro(100, 0)));
        assertThat(range.getStep(), is(Money.euro(10, 0)));
        assertThat(range.containsNull(), is(true));
    }

    @Test
    public void testContains() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        assertThat(range.contains(Money.euro(10, 0)), is(true));
        assertThat(range.contains(Money.euro(20, 0)), is(true));
        assertThat(range.contains(Money.euro(30, 0)), is(true));
        assertThat(range.contains(Money.euro(100, 0)), is(true));
        assertThat(range.contains(Money.euro(110, 0)), is(false));
    }

    @Test
    public void testContains_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertThat(range.contains(Money.euro(10, 0)), is(true));
        assertThat(range.contains(Money.euro(20, 0)), is(true));
        assertThat(range.contains(Money.euro(30, 0)), is(true));
        assertThat(range.contains(Money.euro(100, 0)), is(true));
        assertThat(range.contains(Money.NULL), is(true));
        assertThat(range.contains(Money.euro(110, 0)), is(false));
    }

    @Test
    public void testSize() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        assertThat(range.size(), is(11));
    }

    @Test
    public void testSize_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertThat(range.size(), is(12));
    }

    @Test
    public void testSize_TooLarge() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(Integer.MAX_VALUE, 0), Money.euro(0, 01),
                true);

        assertThrows(RuntimeException.class, () -> range.size());
    }

    @Test
    public void testGetValues() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        Set<Money> values = range.getValues(false);

        assertThat(values.size(), is(10));
        assertThat(values.contains(Money.euro(10, 0)), is(true));
        assertThat(values.contains(Money.euro(20, 0)), is(true));
        assertThat(values.contains(Money.euro(30, 0)), is(true));
        assertThat(values.contains(Money.euro(100, 0)), is(true));
        assertThat(values.contains(Money.euro(110, 0)), is(false));
    }

    @Test
    public void testGetValues_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        Set<Money> values = range.getValues(false);

        assertThat(values.size(), is(11));
        assertThat(values.contains(Money.euro(10, 0)), is(true));
        assertThat(values.contains(Money.euro(20, 0)), is(true));
        assertThat(values.contains(Money.euro(30, 0)), is(true));
        assertThat(values.contains(Money.euro(100, 0)), is(true));
        assertThat(values.contains(Money.NULL), is(true));
        assertThat(values.contains(Money.euro(110, 0)), is(false));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true));
    }

    @Test
    public void testContains_DifferentCurrency() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(0, 1), false);
        assertThat(range.contains(Money.usd(1, 0)), is(false));
    }

    @Test
    public void testContains_DifferentCurrencyOpenRange() {
        MoneyRange range = MoneyRange.valueOf(null, Money.euro(100), Money.euro(1));
        assertThat(range.contains(Money.usd(1, 0)), is(false));
    }

    @Test
    public void testContains_Empty() {
        MoneyRange range = MoneyRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(Money.usd(1, 0)), is(false));
    }

    @Test
    public void testContains_WithUpperOpenBound() {
        MoneyRange range = MoneyRange.valueOf("5.00 EUR", "10.00 EUR", null, false, false, true);

        assertThat(range.contains(Money.euro(4, 99)), is(false));
        assertThat(range.contains(Money.euro(5, 0)), is(true));
        assertThat(range.contains(Money.euro(9, 99)), is(true));
        assertThat(range.contains(Money.euro(10, 0)), is(false));
    }

    @Test
    public void testContains_WithLowerOpenBound() {
        MoneyRange range = MoneyRange.valueOf("5.00 EUR", "10.00 EUR", null, false, true, false);

        assertThat(range.contains(Money.euro(4, 99)), is(false));
        assertThat(range.contains(Money.euro(5, 0)), is(false));
        assertThat(range.contains(Money.euro(5, 1)), is(true));
        assertThat(range.contains(Money.euro(10, 0)), is(true));
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        MoneyRange range = MoneyRange.valueOf("5.00 EUR", "10.00 EUR", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(Money.euro(5, 0)), is(false));
        assertThat(range.contains(Money.euro(5, 1)), is(true));
        assertThat(range.contains(Money.euro(10, 0)), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        MoneyRange range = MoneyRange.valueOf("5.00 EUR", "10.00 EUR", null, false, true, true);

        assertThat(range.contains(Money.euro(5, 0)), is(false));
        assertThat(range.contains(Money.euro(5, 1)), is(true));
        assertThat(range.contains(Money.euro(9, 99)), is(true));
        assertThat(range.contains(Money.euro(10, 0)), is(false));
    }

    @Test
    public void testGetValues_WithLowerOpenBoundAndStep() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.20 EUR", false, true, false);

        Set<Money> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(Money.euro(0, 0)), is(false));
        assertThat(values.contains(Money.euro(0, 20)), is(true));
        assertThat(values.contains(Money.euro(1, 0)), is(true));
    }

    @Test
    public void testGetValues_WithBothOpenBoundsAndStep() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.20 EUR", false, true, true);

        Set<Money> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(Money.euro(0, 0)), is(false));
        assertThat(values.contains(Money.euro(0, 20)), is(true));
        assertThat(values.contains(Money.euro(0, 80)), is(true));
        assertThat(values.contains(Money.euro(1, 0)), is(false));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.30 EUR", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndStep() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.20 EUR", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.20 EUR", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        MoneyRange range = MoneyRange.valueOf("0.00 EUR", "1.00 EUR", "0.20 EUR", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(MoneyRange.valueOf("5.00 EUR", "10.00 EUR", null, false, true, true));
    }
}
