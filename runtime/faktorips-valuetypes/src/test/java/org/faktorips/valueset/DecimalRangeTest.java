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
import static org.junit.Assert.fail;

import java.util.Set;

import org.faktorips.values.Decimal;
import org.junit.Test;

public class DecimalRangeTest {

    @Test
    public void testEmpty() {
        DecimalRange range = DecimalRange.empty();

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

    @Test
    public void testValueOf() {
        DecimalRange range = DecimalRange.valueOf("1.25", "5.67");

        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertEquals(Decimal.valueOf(125, 2), lower);
        assertEquals(Decimal.valueOf(567, 2), upper);
    }

    @Test
    public void testValueOf_WithStep() {
        DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(10, 0));
        DecimalRange.valueOf(Decimal.valueOf(135, 2), Decimal.valueOf(108, 1), Decimal.valueOf(135, 2));

        try {
            // step doesn't fit to range
            DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                    Decimal.valueOf(Integer.valueOf(12)));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                    Decimal.valueOf(Integer.valueOf(0)));
            fail("Expect to fail since a step size of zero is not allowed.");
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepMismatch() {
        DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(12)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepZero() {
        DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(0)));
    }

    @Test
    public void testContains_Step() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        assertTrue(range.contains(Decimal.valueOf(30, 0)));
        assertFalse(range.contains(Decimal.valueOf(35, 0)));
    }

    @Test
    public void testContains() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)));

        assertTrue(range.contains(Decimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(5))));
    }

    @Test
    public void testContains_StepNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.NULL);

        assertTrue(range.contains(Decimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(5))));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_NoStep() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)));

        range.getValues(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_StepNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.NULL);

        range.getValues(false);
    }

    @Test
    public void testGetValues_StepNull_UpperEqualsLower() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(100), Decimal.valueOf(100), Decimal.NULL);

        Set<Decimal> values = range.getValues(false);

        assertEquals(1, values.size());
        assertEquals(Decimal.valueOf(100), values.iterator().next());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_NoUpper() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), null,
                Decimal.valueOf(Integer.valueOf(10)));

        range.getValues(false);
    }

    @Test
    public void testGetValues() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        Set<Decimal> values = range.getValues(false);

        assertEquals(10, values.size());
        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));
    }

    @Test
    public void testGetValues_WithNull() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)),
                Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)), true);

        Set<Decimal> values = range.getValues(false);

        assertEquals(11, values.size());
        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));
        assertTrue(values.contains(Decimal.NULL));
    }

    @Test
    public void testSize() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(5), Decimal.valueOf(10),
                Decimal.valueOf(1));

        assertEquals(6, range.size());
    }

    @Test(expected = RuntimeException.class)
    public void testSize_TooLarge() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(0), Decimal.valueOf(Integer.MAX_VALUE),
                Decimal.valueOf(1, 2));

        range.size();
    }

    @Test
    public void testSize_NoLower() {
        DecimalRange range = DecimalRange.valueOf(null, Decimal.valueOf(10),
                Decimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoUpper() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(5), null,
                Decimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoLimit() {
        DecimalRange range = DecimalRange.valueOf(null, null,
                Decimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_Step() {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(0), Decimal.valueOf(100),
                Decimal.valueOf(10));

        assertEquals(11, range.size());
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

        assertTrue(allowedValues.contains(Decimal.NULL));
    }

    @Test
    public void testContains_Empty() {
        DecimalRange range = DecimalRange.empty();
        assertThat(range.contains(null), is(false));
        assertThat(range.contains(Decimal.valueOf(1)), is(false));
    }
}
