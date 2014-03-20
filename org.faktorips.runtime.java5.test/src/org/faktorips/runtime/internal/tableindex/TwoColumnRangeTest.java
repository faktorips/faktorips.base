/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TwoColumnRangeTest {

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testConstructor_lowerBound() {
        new TwoColumnRange<String>(null, "B");
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testConstructor_upperBound() {
        new TwoColumnRange<String>("A", null);
    }

    @Test
    public void testHashCode() {
        TwoColumnRange<Integer> key1 = new TwoColumnRange<Integer>(0, 10);
        TwoColumnRange<Integer> key2 = new TwoColumnRange<Integer>(0, 25);
        TwoColumnRange<Integer> key3 = new TwoColumnRange<Integer>(1, 10);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotSame(key1.hashCode(), key3.hashCode());
        assertNotSame(key2.hashCode(), key3.hashCode());
    }

    @Test
    public void testTwoColumnRangeKKBooleanBoolean() throws Exception {
        TwoColumnRange<String> twoColumnRange = new TwoColumnRange<String>("a", "b", false, false);

        assertFalse(twoColumnRange.isLowerInclusive());
        assertFalse(twoColumnRange.isUpperInclusive());
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new TwoColumnRange<Integer>(1, 2), new TwoColumnRange<Integer>(1, 2));
        assertEquals(new TwoColumnRange<Integer>(1, 2), new TwoColumnRange<Integer>(1, 3));
        assertFalse(new TwoColumnRange<Integer>(2, 5).equals(new TwoColumnRange<Integer>(3, 5)));
        assertFalse(new TwoColumnRange<Integer>(5, 5).equals(new TwoColumnRange<Integer>(3, 5)));
    }

    @Test
    public void testEquals_lowerBoundExclusive() throws Exception {
        assertEquals(new TwoColumnRange<Integer>(1, 2, false, true), new TwoColumnRange<Integer>(1, 2, false, true));
        assertFalse(new TwoColumnRange<Integer>(1, 5, false, true)
                .equals(new TwoColumnRange<Integer>(1, 5, true, false)));
        assertEquals(new TwoColumnRange<Integer>(1, 2), new TwoColumnRange<Integer>(1, 3));
        assertFalse(new TwoColumnRange<Integer>(2, 5).equals(new TwoColumnRange<Integer>(3, 5)));
        assertFalse(new TwoColumnRange<Integer>(5, 5).equals(new TwoColumnRange<Integer>(3, 5)));
    }

    @Test
    public void testCompareTo() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(2, 5).compareTo(new TwoColumnRange<Integer>(3, 5)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 5).compareTo(new TwoColumnRange<Integer>(3, 5)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12).compareTo(new TwoColumnRange<Integer>(3, 5)) == 0);
        assertTrue(new TwoColumnRange<Integer>(3, 5).compareTo(new TwoColumnRange<Integer>(3, 5)) == 0);
    }

    @Test
    public void testCompareTo_lowerExclusive() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(2, 5, false, true).compareTo(new TwoColumnRange<Integer>(3, 5, false,
                false)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 5, false, false).compareTo(new TwoColumnRange<Integer>(3, 5, false,
                false)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12, false, false).compareTo(new TwoColumnRange<Integer>(3, 5, true,
                false)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12, true, false).compareTo(new TwoColumnRange<Integer>(3, 5, false,
                false)) < 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12, false, false).compareTo(new TwoColumnRange<Integer>(3, 5, false,
                false)) == 0);
    }

    @Test
    public void testCompareToUpperBound() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(2, 5).compareToUpperBound(new TwoColumnRange<Integer>(2, 6)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 6).compareToUpperBound(new TwoColumnRange<Integer>(5, 5)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12).compareToUpperBound(new TwoColumnRange<Integer>(5, 12)) == 0);
        assertTrue(new TwoColumnRange<Integer>(3, 5).compareToUpperBound(new TwoColumnRange<Integer>(3, 5)) == 0);
    }

    @Test
    public void testcompareToUpperBoundUpperBound_lowerExclusive() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(2, 5, true, false).compareToUpperBound(new TwoColumnRange<Integer>(3, 6,
                false, false)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 6, false, false).compareToUpperBound(new TwoColumnRange<Integer>(3,
                5, false, false)) > 0);
        assertTrue(new TwoColumnRange<Integer>(1, 5, false, false).compareToUpperBound(new TwoColumnRange<Integer>(3,
                5, false, true)) < 0);
        assertTrue(new TwoColumnRange<Integer>(1, 5, false, true).compareToUpperBound(new TwoColumnRange<Integer>(3, 5,
                false, false)) > 0);
        assertTrue(new TwoColumnRange<Integer>(1, 5, false, false).compareToUpperBound(new TwoColumnRange<Integer>(3,
                5, false, false)) == 0);
    }

    @Test
    public void testIsContained_excl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, false, false);

        assertFalse(range.isContained(4));
        assertFalse(range.isContained(5));
        assertTrue(range.isContained(6));
        assertTrue(range.isContained(9));
        assertFalse(range.isContained(10));
        assertFalse(range.isContained(11));
    }

    @Test
    public void testIsContained_inkl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, true, true);

        assertFalse(range.isContained(4));
        assertTrue(range.isContained(5));
        assertTrue(range.isContained(6));
        assertTrue(range.isContained(9));
        assertTrue(range.isContained(10));
        assertFalse(range.isContained(11));
    }

    @Test
    public void testIsContained_inklexcl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, true, false);

        assertFalse(range.isContained(4));
        assertTrue(range.isContained(5));
        assertTrue(range.isContained(6));
        assertTrue(range.isContained(9));
        assertFalse(range.isContained(10));
        assertFalse(range.isContained(11));
    }

    @Test
    public void testIsContained_exklinkl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, false, true);

        assertFalse(range.isContained(4));
        assertFalse(range.isContained(5));
        assertTrue(range.isContained(6));
        assertTrue(range.isContained(9));
        assertTrue(range.isContained(10));
        assertFalse(range.isContained(11));
    }

}
