/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.faktorips.values.Money;
import org.junit.Test;

/**
 * Test class for <code>com.fja.pm.domain.MoneyRange</code>
 * 
 * @author Peter Kuntz
 */
public class MoneyRangeTest {
    @Test
    public void testValueOf() {
        MoneyRange range = MoneyRange.valueOf("1.25 EUR", "5.67 EUR");
        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertEquals(Money.euro(1, 25), lower);
        assertEquals(Money.euro(5, 67), upper);

        try {
            MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(10, 0), false);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(0, 0), false);
            fail("Expected to fail since zero step size is not allowed.");
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(Money.euro(10, 0), range.getLowerBound());
        assertEquals(Money.euro(100, 0), range.getUpperBound());
        assertEquals(Money.euro(10, 0), range.getStep());
        assertTrue(range.containsNull());
    }

    @Test
    public void testConstructor() {
        MoneyRange range = new MoneyRange(Money.euro(1, 25), Money.euro(5, 67));
        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertEquals(Money.euro(1, 25), lower);
        assertEquals(Money.euro(5, 67), upper);
    }

    @Test
    public void testContains() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(11, range.size());
        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertTrue(range.contains(Money.NULL));
        assertFalse(range.contains(Money.euro(110, 0)));

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        assertEquals(10, range.size());
        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertFalse(range.contains(Money.euro(110, 0)));
    }

    @Test
    public void testSize() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(12, range.size());

        range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        assertEquals(11, range.size());
    }

    @Test
    public void testGetValues() {
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        Set<Money> values = range.getValues(false);
        assertEquals(11, range.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertTrue(values.contains(Money.NULL));
        assertFalse(values.contains(Money.euro(110, 0)));

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        values = range.getValues(false);
        assertEquals(10, range.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertFalse(values.contains(Money.euro(110, 0)));
    }

    @Test
    public void testSerializable() throws Exception {
        TestUtil.testSerializable(MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true));
    }

}
