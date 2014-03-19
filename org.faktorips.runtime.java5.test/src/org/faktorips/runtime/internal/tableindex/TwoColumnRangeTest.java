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
            public void testIsBelowUpperBoundOf() {
                TwoColumnRange<Integer> originalKey = new TwoColumnRange<Integer>(0, 10);
                TwoColumnRange<Integer> keyEqualRange = new TwoColumnRange<Integer>(0, 10);
                TwoColumnRange<Integer> keySubRangeEqualUpperBound = new TwoColumnRange<Integer>(1, 10);
                TwoColumnRange<Integer> keySubRangeEqualLowerBound = new TwoColumnRange<Integer>(0, 9);
                TwoColumnRange<Integer> keySubRange = new TwoColumnRange<Integer>(5, 5);
                TwoColumnRange<Integer> keyPartiallyOverlappingRange = new TwoColumnRange<Integer>(5, 15);
                TwoColumnRange<Integer> keyDisjunctRange = new TwoColumnRange<Integer>(11, 15);
        
                assertTrue(keyEqualRange.isBelowUpperBoundOf(originalKey));
                assertTrue(keySubRangeEqualUpperBound.isBelowUpperBoundOf(originalKey));
                assertTrue(keySubRangeEqualLowerBound.isBelowUpperBoundOf(originalKey));
                assertTrue(keySubRange.isBelowUpperBoundOf(originalKey));
                assertFalse(keyPartiallyOverlappingRange.isBelowUpperBoundOf(originalKey));
                assertFalse(keyDisjunctRange.isBelowUpperBoundOf(originalKey));
            }

    @Test
            public void testIsBelowUpperBoundOf_exclusive() {
                TwoColumnRange<Integer> originalKeyExcl = new TwoColumnRange<Integer>(0, 10, false, false);
                TwoColumnRange<Integer> originalKeyIncl = new TwoColumnRange<Integer>(0, 10, true, true);
                TwoColumnRange<Integer> keyEqualUpperIncl = new TwoColumnRange<Integer>(0, 10, false, true);
                TwoColumnRange<Integer> keyEqualUpperExcl = new TwoColumnRange<Integer>(0, 10, true, false);
        
                assertFalse(keyEqualUpperIncl.isBelowUpperBoundOf(originalKeyExcl));
                assertTrue(keyEqualUpperIncl.isBelowUpperBoundOf(originalKeyIncl));
                assertTrue(keyEqualUpperExcl.isBelowUpperBoundOf(originalKeyExcl));
                assertTrue(keyEqualUpperExcl.isBelowUpperBoundOf(originalKeyIncl));
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

}
