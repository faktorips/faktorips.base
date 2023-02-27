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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

    @Test
    public void testValueOf() {
        MoneyRange range = MoneyRange.valueOf("1.25 EUR", "5.67 EUR");

        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertEquals(Money.euro(1, 25), lower);
        assertEquals(Money.euro(5, 67), upper);
        assertFalse(range.containsNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepMismatch() {
        MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(10, 0), false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepZero() {
        MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(0, 0), false);
    }

    @Test
    public void testValueOf_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertEquals(Money.euro(10, 0), range.getLowerBound());
        assertEquals(Money.euro(100, 0), range.getUpperBound());
        assertEquals(Money.euro(10, 0), range.getStep());
        assertTrue(range.containsNull());
    }

    @Test
    public void testContains() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertFalse(range.contains(Money.euro(110, 0)));
    }

    @Test
    public void testContains_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertTrue(range.contains(Money.NULL));
        assertFalse(range.contains(Money.euro(110, 0)));
    }

    @Test
    public void testSize() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        assertEquals(11, range.size());
    }

    @Test
    public void testSize_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        assertEquals(12, range.size());
    }

    @Test(expected = RuntimeException.class)
    public void testSize_TooLarge() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(Integer.MAX_VALUE, 0), Money.euro(0, 01),
                true);

        range.size();
    }

    @Test
    public void testGetValues() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);

        Set<Money> values = range.getValues(false);

        assertEquals(10, values.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertFalse(values.contains(Money.euro(110, 0)));
    }

    @Test
    public void testGetValues_WithNull() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);

        Set<Money> values = range.getValues(false);

        assertEquals(11, values.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertTrue(values.contains(Money.NULL));
        assertFalse(values.contains(Money.euro(110, 0)));
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
}
