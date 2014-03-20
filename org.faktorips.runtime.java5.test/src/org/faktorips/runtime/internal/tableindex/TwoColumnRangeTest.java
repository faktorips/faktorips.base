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
    public void testEquals_infinite() throws Exception {
        assertEquals(new TwoColumnRange<Integer>(null, 2), new TwoColumnRange<Integer>(null, 2));
        assertEquals(new TwoColumnRange<Integer>(1, null), new TwoColumnRange<Integer>(1, null));
        assertEquals(new TwoColumnRange<Integer>(null, null), new TwoColumnRange<Integer>(null, null));
        assertEquals(new TwoColumnRange<Integer>(3, 5), new TwoColumnRange<Integer>(3, null));
        assertFalse(new TwoColumnRange<Integer>(1, null).equals(new TwoColumnRange<Integer>(3, null)));
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
    public void testCompareTo_infinity() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(null, 5).compareTo(new TwoColumnRange<Integer>(3, 5)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 5).compareTo(new TwoColumnRange<Integer>(null, 5)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, null).compareTo(new TwoColumnRange<Integer>(3, 5)) == 0);
        assertTrue(new TwoColumnRange<Integer>(3, 5).compareTo(new TwoColumnRange<Integer>(3, null)) == 0);
    }

    @Test
    public void testCompareToUpperBound_infinity() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(4, 5).compareToUpperBound(new TwoColumnRange<Integer>(3, null)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, null).compareToUpperBound(new TwoColumnRange<Integer>(2, null)) > 0);
        assertTrue(new TwoColumnRange<Integer>(null, 5).compareToUpperBound(new TwoColumnRange<Integer>(3, 5)) == 0);
        assertTrue(new TwoColumnRange<Integer>(3, 5).compareToUpperBound(new TwoColumnRange<Integer>(null, 5)) == 0);
    }

    @Test
    public void testCompareToUpperBound() throws Exception {
        assertTrue(new TwoColumnRange<Integer>(2, 5).compareToUpperBound(new TwoColumnRange<Integer>(2, 6)) < 0);
        assertTrue(new TwoColumnRange<Integer>(4, 6).compareToUpperBound(new TwoColumnRange<Integer>(5, 5)) > 0);
        assertTrue(new TwoColumnRange<Integer>(3, 12).compareToUpperBound(new TwoColumnRange<Integer>(5, 12)) == 0);
        assertTrue(new TwoColumnRange<Integer>(3, 5).compareToUpperBound(new TwoColumnRange<Integer>(3, 5)) == 0);
    }

    @Test
    public void testcompareToUpperBoundUpperBound_upperExclusive() throws Exception {
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
    public void testIsOverlapping_excl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, false, false);
        TwoColumnRange<Integer> outOfLowerRange = new TwoColumnRange<Integer>(3, 4, true, true);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(3, 5, false, false);
        TwoColumnRange<Integer> overlapLowerRange = new TwoColumnRange<Integer>(3, 6, false, false);
        TwoColumnRange<Integer> overlapInnerRange = new TwoColumnRange<Integer>(6, 8, false, false);
        TwoColumnRange<Integer> overlapSameRange = new TwoColumnRange<Integer>(5, 10, false, false);
        TwoColumnRange<Integer> overlapOuterRange = new TwoColumnRange<Integer>(3, 11, false, false);
        TwoColumnRange<Integer> overlapUpperRange = new TwoColumnRange<Integer>(8, 12, false, false);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, 12, false, false);
        TwoColumnRange<Integer> outOfUpperRange = new TwoColumnRange<Integer>(11, 15, true, true);

        assertFalse(range.isOverlapping(outOfLowerRange));
        assertFalse(range.isOverlapping(lowerRange));
        assertTrue(range.isOverlapping(overlapLowerRange));
        assertTrue(range.isOverlapping(overlapInnerRange));
        assertTrue(range.isOverlapping(overlapSameRange));
        assertTrue(range.isOverlapping(overlapOuterRange));
        assertTrue(range.isOverlapping(overlapUpperRange));
        assertFalse(range.isOverlapping(upperRange));
        assertFalse(range.isOverlapping(outOfUpperRange));
    }

    @Test
    public void testIsOverlapping_inkl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, true, true);
        TwoColumnRange<Integer> outOfLowerRange = new TwoColumnRange<Integer>(3, 4, true, true);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(3, 5, true, true);
        TwoColumnRange<Integer> overlapLowerRange = new TwoColumnRange<Integer>(3, 6, true, true);
        TwoColumnRange<Integer> overlapInnerRange = new TwoColumnRange<Integer>(6, 8, true, true);
        TwoColumnRange<Integer> overlapSameRange = new TwoColumnRange<Integer>(5, 10, true, true);
        TwoColumnRange<Integer> overlapOuterRange = new TwoColumnRange<Integer>(3, 11, true, true);
        TwoColumnRange<Integer> overlapUpperRange = new TwoColumnRange<Integer>(8, 12, true, true);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, 12, true, true);
        TwoColumnRange<Integer> outOfUpperRange = new TwoColumnRange<Integer>(11, 15, true, true);

        assertFalse(range.isOverlapping(outOfLowerRange));
        assertTrue(range.isOverlapping(lowerRange));
        assertTrue(range.isOverlapping(overlapLowerRange));
        assertTrue(range.isOverlapping(overlapInnerRange));
        assertTrue(range.isOverlapping(overlapSameRange));
        assertTrue(range.isOverlapping(overlapOuterRange));
        assertTrue(range.isOverlapping(overlapUpperRange));
        assertTrue(range.isOverlapping(upperRange));
        assertFalse(range.isOverlapping(outOfUpperRange));
    }

    @Test
    public void testIsOverlapping_inklexcl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, true, true);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(3, 5, true, false);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, 12, false, true);

        assertFalse(range.isOverlapping(lowerRange));
        assertFalse(range.isOverlapping(upperRange));
    }

    @Test
    public void testIsOverlapping_exclIncl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, false, false);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(3, 5, true, true);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, 12, true, true);

        assertFalse(range.isOverlapping(lowerRange));
        assertFalse(range.isOverlapping(upperRange));
    }

    @Test
    public void testIsOverlapping_exclExcl() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10, false, false);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(3, 5, true, false);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, 12, false, true);

        assertFalse(range.isOverlapping(lowerRange));
        assertFalse(range.isOverlapping(upperRange));
    }

    @Test
    public void testIsOverlapping_infinite() throws Exception {
        TwoColumnRange<Integer> range = new TwoColumnRange<Integer>(5, 10);
        TwoColumnRange<Integer> outOfLowerRange = new TwoColumnRange<Integer>(null, 4);
        TwoColumnRange<Integer> lowerRange = new TwoColumnRange<Integer>(null, 5);
        TwoColumnRange<Integer> overlapLowerRange = new TwoColumnRange<Integer>(null, 6);
        TwoColumnRange<Integer> overlapOuterRange = new TwoColumnRange<Integer>(null, null);
        TwoColumnRange<Integer> overlapUpperRange = new TwoColumnRange<Integer>(8, null);
        TwoColumnRange<Integer> upperRange = new TwoColumnRange<Integer>(10, null);
        TwoColumnRange<Integer> outOfUpperRange = new TwoColumnRange<Integer>(11, null);

        assertFalse(range.isOverlapping(outOfLowerRange));
        assertTrue(range.isOverlapping(lowerRange));
        assertTrue(range.isOverlapping(overlapLowerRange));
        assertTrue(range.isOverlapping(overlapOuterRange));
        assertTrue(range.isOverlapping(overlapUpperRange));
        assertTrue(range.isOverlapping(upperRange));
        assertFalse(range.isOverlapping(outOfUpperRange));
    }

}
