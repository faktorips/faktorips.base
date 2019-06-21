/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.Test;

public class BigDecimalRangeTest {

    @Test
    public void testBigDecimalOf() {
        BigDecimalRange range = BigDecimalRange.valueOf("1.25", "5.67");
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    @Test
    public void testBigDecimalOf_upperAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf("0", null, null, false);

        assertEquals(new BigDecimalRange(BigDecimal.valueOf(0), null), range);
    }

    @Test
    public void testBigDecimalOf_lowerAndStepBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, "0", null, false);

        assertEquals(new BigDecimalRange(null, BigDecimal.valueOf(0)), range);
    }

    @Test
    public void testBigDecimalOf_lowerAndUpperBoundsNull() {
        BigDecimalRange range = BigDecimalRange.valueOf(null, null, "0", true);

        assertEquals(BigDecimalRange.valueOf(null, null, BigDecimal.valueOf(0), true), range);
    }

    @Test
    public void testBigDecimalOf_boundsEmpty() {
        BigDecimalRange range = BigDecimalRange.valueOf("", "", "", false);

        assertEquals(new BigDecimalRange(null, null), range);
    }

    @Test
    public void testConstructor() {
        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(125, 2), BigDecimal.valueOf(567, 2));
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    @Test
    public void testConstructorWithStep() {
        BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(10, 0));
        BigDecimalRange.valueOf(BigDecimal.valueOf(135, 2), BigDecimal.valueOf(108, 1), BigDecimal.valueOf(135, 2));

        try {
            // step doesn't fit to range
            BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                    BigDecimal.valueOf(Integer.valueOf(12)));
            fail();
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }

        try {
            BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                    BigDecimal.valueOf(Integer.valueOf(0)));
            fail("Expect to fail since a step size of zero is not allowed.");
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }
    }

    @Test
    public void testContains() {
        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)));
        assertTrue(range.contains(BigDecimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(5))));

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)), null); // ?
        assertTrue(range.contains(BigDecimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(BigDecimal.valueOf(Integer.valueOf(5))));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
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

    @Test
    public void testGetValues() {

        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)));
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)), null);
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), null, BigDecimal.valueOf(Integer.valueOf(10)));

        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(10)));

        Set<BigDecimal> values = range.getValues(false);
        assertEquals(10, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)), BigDecimal.valueOf(Integer.valueOf(100)),
                BigDecimal.valueOf(Integer.valueOf(10)), true);
        values = range.getValues(false);
        assertEquals(11, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));
        assertTrue(values.contains(null));

    }

    @Test
    public void testSerializable() throws Exception {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(Integer.valueOf(10)),
                BigDecimal.valueOf(Integer.valueOf(100)), BigDecimal.valueOf(Integer.valueOf(10)), true);
        TestUtil.testSerializable(range);
    }

}
