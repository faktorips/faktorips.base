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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.Test;

public class BigDecimalRangeTest {

    @Test
    public void testEmpty() {
        BigDecimalRange range = BigDecimalRange.empty();

        assertTrue(range.isEmpty());
        assertTrue(range.isDiscrete());
        assertFalse(range.containsNull());
        assertNull(range.getLowerBound());
        assertNull(range.getUpperBound());
        assertNull(range.getStep());
    }

    @Test
    public void testValueOf_BoundsEmpty() {
        BigDecimalRange range = BigDecimalRange.valueOf("", "", "", false);

        assertEquals(BigDecimalRange.empty(), range);
    }

    @Test
    public void testValueOf() {
        BigDecimalRange range = BigDecimalRange.valueOf("1.25", "5.67");
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    @Test
    public void testValueOf_UpperAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", null, null, false);

        assertEquals(BigDecimalRange.valueOf(BigDecimal.valueOf(0), null), range);
    }

    @Test
    public void testValueOf_LowerAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, "0", null, false);

        assertEquals(BigDecimalRange.valueOf(null, BigDecimal.valueOf(0)), range);
    }

    @Test
    public void testValueOf_LowerAndUpperBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, null, "0", true);

        assertEquals(BigDecimalRange.valueOf(null, null, BigDecimal.valueOf(0), true), range);
    }

    @Test
    public void testValueOf_WithStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(10, 0));

        assertEquals(BigDecimal.valueOf(10), range.getLowerBound());
        assertEquals(BigDecimal.valueOf(100), range.getUpperBound());
        assertEquals(BigDecimal.valueOf(10), range.getStep());
        assertFalse(range.containsNull());
    }

    @Test
    public void testValueOf_WithStep_Cents() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(135, 2), BigDecimal.valueOf(108, 1),
                BigDecimal.valueOf(135, 2));

        assertEquals(BigDecimal.valueOf(135, 2), range.getLowerBound());
        assertEquals(BigDecimal.valueOf(108, 1), range.getUpperBound());
        assertEquals(BigDecimal.valueOf(135, 2), range.getStep());
        assertFalse(range.containsNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepMismatch() {
        BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(12)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_StepZero() {
        BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(0)));
    }

    @Test
    public void testContains() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)));

        assertTrue(range.contains(BigDecimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(5))));
    }

    @Test
    public void testContains_StepNull() {
        BigDecimalRange range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)), null); // ?

        assertTrue(range.contains(BigDecimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(5))));
    }

    @Test
    public void testContains_Step() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(10)));

        assertTrue(range.contains(BigDecimal.valueOf(30, 0)));
        assertFalse(range.contains(BigDecimal.valueOf(35, 0)));
    }

    @Test
    public void testContainsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(0)),
                BigDecimal.valueOf(Integer.valueOf(100)), BigDecimal.valueOf(Integer.valueOf(10)), true);

        assertTrue(range.contains(null));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_NoStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)));

        range.getValues(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_NullStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)), null);

        range.getValues(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetValues_NoUpper() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), null,
                BigDecimal.valueOf(Integer.valueOf(10)));

        range.getValues(false);
    }

    @Test
    public void testGetValues() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(10)));

        Set<BigDecimal> values = range.getValues(false);

        assertEquals(10, values.size());
        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));
    }

    @Test
    public void testGetValues_WithNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(10)), true);

        Set<BigDecimal> values = range.getValues(false);

        assertEquals(11, values.size());
        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));
        assertTrue(values.contains(null));
    }

    @Test
    public void testSize() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(5), BigDecimal.valueOf(10),
                BigDecimal.valueOf(1));

        assertEquals(6, range.size());
    }

    @Test(expected = RuntimeException.class)
    public void testSize_TooLarge() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(0), BigDecimal.valueOf(Integer.MAX_VALUE),
                BigDecimal.valueOf(1, 2));

        range.size();
    }

    @Test
    public void testSize_NoLower() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, BigDecimal.valueOf(10),
                BigDecimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoUpper() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(5), null,
                BigDecimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_NoLimit() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, null,
                BigDecimal.valueOf(1));

        assertEquals(Integer.MAX_VALUE, range.size());
    }

    @Test
    public void testSize_Step() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(0), BigDecimal.valueOf(100),
                BigDecimal.valueOf(10));

        assertEquals(11, range.size());
    }

    @Test
    public void testSerializable() throws Exception {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)), BigDecimal.valueOf(Integer.valueOf(10)), true);
        TestUtil.testSerializable(range);
    }

    @Test
    public void testGetValuesOfOfBigDecimalRangeWithoutStep() {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        assertTrue(range.getValues(true).contains(BigDecimal.valueOf(10)));
    }

    @Test
    public void testSizeOfBigDecimalRangeWithEqualBoundariesHavingDifferentPrecision() {
        BigDecimalRange range = BigDecimalRange.valueOf(new BigDecimal("10"), new BigDecimal("10.00"));
        assertEquals(1, range.size());
    }

}
