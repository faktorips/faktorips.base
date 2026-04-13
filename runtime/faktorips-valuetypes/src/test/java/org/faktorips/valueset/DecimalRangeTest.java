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

import org.faktorips.values.Decimal;
import org.junit.Test;

public class DecimalRangeTest {

    @Test
    public void testEmpty() {
        DecimalRange range = DecimalRange.empty();

        assertThat(range.isEmpty(), is(true));
        assertThat(range.isDiscrete(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLowerBound(), is(nullValue()));
        assertThat(range.getUpperBound(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testValueOf() {
        DecimalRange range = DecimalRange.valueOf("1.25", "5.67");

        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertThat(lower, is(Decimal.valueOf(125, 2)));
        assertThat(upper, is(Decimal.valueOf(567, 2)));
    }

    @Test
    public void testValueOf_WithStep() {
        DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(10, 0));
        DecimalRange.valueOf(Decimal.valueOf(135, 2), Decimal.valueOf(108, 1), Decimal.valueOf(135, 2));

        assertThrows(IllegalArgumentException.class,
                () -> DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                        Decimal.valueOf(Integer.valueOf(100)),
                        Decimal.valueOf(Integer.valueOf(12))));

        assertThrows(IllegalArgumentException.class,
                () -> DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                        Decimal.valueOf(Integer.valueOf(100)),
                        Decimal.valueOf(Integer.valueOf(0))));
    }

    @Test
    public void testValueOf_StepMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                        Decimal.valueOf(Integer.valueOf(100)),
                        Decimal.valueOf(Integer.valueOf(12))));
    }

    @Test
    public void testValueOf_StepZero() {
        assertThrows(IllegalArgumentException.class,
                () -> DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                        Decimal.valueOf(Integer.valueOf(100)),
                        Decimal.valueOf(Integer.valueOf(0))));
    }

    @Test
    public void testContains_Step() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        assertThat(range.contains(Decimal.valueOf(30, 0)), is(true));
        assertThat(range.contains(Decimal.valueOf(35, 0)), is(false));
    }

    @Test
    public void testContains() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)));

        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(30))), is(true));
        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(120))), is(false));
        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(5))), is(false));
    }

    @Test
    public void testContains_StepNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.NULL);

        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(30))), is(true));
        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(120))), is(false));
        assertThat(range.contains(Decimal.valueOf(Integer.valueOf(5))), is(false));
    }

    @Test
    public void testGetValues_NoStep() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)));

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues_StepNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.NULL);

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues_StepNull_UpperEqualsLower() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(100), Decimal.valueOf(100), Decimal.NULL);

        Set<Decimal> values = range.getValues(false);

        assertThat(values.size(), is(1));
        assertThat(values.iterator().next(), is(Decimal.valueOf(100)));
    }

    @Test
    public void testGetValues_NoUpper() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), null,
                Decimal.valueOf(Integer.valueOf(10)));

        assertThrows(IllegalStateException.class, () -> range.getValues(false));
    }

    @Test
    public void testGetValues() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        Set<Decimal> values = range.getValues(false);

        assertThat(values.size(), is(10));
        assertThat(values.contains(Decimal.valueOf(100, 0)), is(true));
        assertThat(values.contains(Decimal.valueOf(70, 0)), is(true));
        assertThat(values.contains(Decimal.valueOf(10, 0)), is(true));
    }

    @Test
    public void testGetValues_WithNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)), true);

        Set<Decimal> values = range.getValues(false);

        assertThat(values.size(), is(11));
        assertThat(values.contains(Decimal.valueOf(100, 0)), is(true));
        assertThat(values.contains(Decimal.valueOf(70, 0)), is(true));
        assertThat(values.contains(Decimal.valueOf(10, 0)), is(true));
        assertThat(values.contains(Decimal.NULL), is(true));
    }

    @Test
    public void testSize() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(5), Decimal.valueOf(10),
                Decimal.valueOf(1));

        assertThat(range.size(), is(6));
    }

    @Test
    public void testSize_TooLarge() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(0), Decimal.valueOf(Integer.MAX_VALUE),
                Decimal.valueOf(1, 2));

        assertThrows(RuntimeException.class, () -> range.size());
    }

    @Test
    public void testSize_NoLower() {
        DecimalRange range = DecimalRange.valueOf(null, Decimal.valueOf(10),
                Decimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoUpper() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(5), null,
                Decimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_NoLimit() {
        DecimalRange range = DecimalRange.valueOf(null, null,
                Decimal.valueOf(1));

        assertThat(range.size(), is(Integer.MAX_VALUE));
    }

    @Test
    public void testSize_Step() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(0), Decimal.valueOf(100),
                Decimal.valueOf(10));

        assertThat(range.size(), is(11));
    }

    @Test
    public void testSerializable() throws Exception {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)), true);
        TestUtil.testSerializable(range);
    }

    @Test
    public void testContainsNull() {
        DecimalRange allowedValues = DecimalRange.valueOf("0", "5", "1", true);

        assertThat(allowedValues.contains(Decimal.NULL), is(true));
    }

    @Test
    public void testContains_Empty() {
        DecimalRange range = DecimalRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(Decimal.valueOf(1)), is(false));
    }

    @Test
    public void testValueOf_WithOpenBounds() {
        DecimalRange range = DecimalRange.valueOf("5", "10", null, false, true, false);

        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
        assertThat(range.contains(Decimal.valueOf(5)), is(false));
        assertThat(range.contains(Decimal.valueOf("5.01")), is(true));
        assertThat(range.contains(Decimal.valueOf(10)), is(true));
    }

    @Test
    public void testValueOf_WithBothOpenBounds() {
        DecimalRange range = DecimalRange.valueOf("5", "10", null, false, true, true);

        assertThat(range.contains(Decimal.valueOf(5)), is(false));
        assertThat(range.contains(Decimal.valueOf("5.01")), is(true));
        assertThat(range.contains(Decimal.valueOf("9.99")), is(true));
        assertThat(range.contains(Decimal.valueOf(10)), is(false));
    }

    @Test
    public void testGetValues_WithOpenBoundsAndStep() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "2", false, true, false);

        Set<Decimal> values = range.getValues(false);

        assertThat(values.size(), is(5));
        assertThat(values.contains(Decimal.valueOf(0)), is(false));
        assertThat(values.contains(Decimal.valueOf(2)), is(true));
        assertThat(values.contains(Decimal.valueOf(10)), is(true));
    }

    @Test
    public void testGetValues_WithBothOpenBoundsAndStep() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "2", false, true, true);

        Set<Decimal> values = range.getValues(false);

        assertThat(values.size(), is(4));
        assertThat(values.contains(Decimal.valueOf(0)), is(false));
        assertThat(values.contains(Decimal.valueOf(2)), is(true));
        assertThat(values.contains(Decimal.valueOf(8)), is(true));
        assertThat(values.contains(Decimal.valueOf(10)), is(false));
    }

    @Test
    public void testSize_WithOpenBoundsAndStepNotFittingClosedBounds() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "3", false, true, false);

        assertThat(range.size(), is(3));
    }

    @Test
    public void testContains_WithUpperOpenBound() {
        DecimalRange range = DecimalRange.valueOf("5", "10", null, false, false, true);

        assertThat(range.contains(Decimal.valueOf("4.99")), is(false));
        assertThat(range.contains(Decimal.valueOf(5)), is(true));
        assertThat(range.contains(Decimal.valueOf("9.99")), is(true));
        assertThat(range.contains(Decimal.valueOf(10)), is(false));
    }

    @Test
    public void testContains_WithLowerOpenBound() {
        DecimalRange range = DecimalRange.valueOf("5", "10", null, false, true, false);

        assertThat(range.contains(Decimal.valueOf("4.99")), is(false));
        assertThat(range.contains(Decimal.valueOf(5)), is(false));
        assertThat(range.contains(Decimal.valueOf("5.01")), is(true));
        assertThat(range.contains(Decimal.valueOf(10)), is(true));
    }

    @Test
    public void testSize_WithBothOpenBoundsAndStep() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "2", false, true, true);

        assertThat(range.size(), is(4));
    }

    @Test
    public void testSize_WithLowerOpenBound() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "2", false, true, false);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSize_WithUpperOpenBound() {
        DecimalRange range = DecimalRange.valueOf("0", "10", "2", false, false, true);

        assertThat(range.size(), is(5));
    }

    @Test
    public void testSerializable_WithOpenBounds() throws Exception {
        TestUtil.testSerializable(DecimalRange.valueOf("5", "10", null, false, true, true));
    }

}
