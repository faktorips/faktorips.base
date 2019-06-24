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

import java.util.Set;

import org.faktorips.values.Decimal;
import org.junit.Test;

public class DecimalRangeTest {
    @Test
    public void testValueOf() {
        DecimalRange range = DecimalRange.valueOf("1.25", "5.67");
        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertEquals(Decimal.valueOf(125, 2), lower);
        assertEquals(Decimal.valueOf(567, 2), upper);
    }

    @Test
    public void testConstructor() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(125, 2), Decimal.valueOf(567, 2));
        Decimal lower = range.getLowerBound();
        Decimal upper = range.getUpperBound();
        assertEquals(Decimal.valueOf(125, 2), lower);
        assertEquals(Decimal.valueOf(567, 2), upper);
    }

    @Test
    public void testConstructorWithStep() {
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

    @Test
    public void testContains() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)));
        assertTrue(range.contains(Decimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(5))));

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)), Decimal.NULL);
        assertTrue(range.contains(Decimal.valueOf(Integer.valueOf(30))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(120))));
        assertFalse(range.contains(Decimal.valueOf(Integer.valueOf(5))));

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        assertTrue(range.contains(Decimal.valueOf(30, 0)));
        assertFalse(range.contains(Decimal.valueOf(35, 0)));
    }

    @Test
    public void testGetValues() {
        DecimalRange range = new DecimalRange(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)));
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)), Decimal.NULL);
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), null, Decimal.valueOf(Integer.valueOf(10)));

        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // Expected exception.
        }

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)));

        Set<Decimal> values = range.getValues(false);
        assertEquals(10, values.size());

        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));

        range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)), true);
        values = range.getValues(false);
        assertEquals(11, values.size());

        assertTrue(values.contains(Decimal.valueOf(100, 0)));
        assertTrue(values.contains(Decimal.valueOf(70, 0)));
        assertTrue(values.contains(Decimal.valueOf(10, 0)));
        assertTrue(values.contains(Decimal.NULL));
    }

    @Test
    public void testSerializable() throws Exception {
        DecimalRange range = DecimalRange.valueOf(Decimal.valueOf(Integer.valueOf(10)), Decimal.valueOf(Integer.valueOf(100)),
                Decimal.valueOf(Integer.valueOf(10)), true);
        TestUtil.testSerializable(range);
    }

    @Test
    public void testContainsNull() {
        DecimalRange allowedValues = DecimalRange.valueOf("0", "5", "1", true);

        assertTrue(allowedValues.contains(Decimal.NULL));
    }
}
