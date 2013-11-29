/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.faktorips.runtime.internal.tableindex.TwoColumnRange;
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
            public void testIsLowerOrEqualUpperBound() {
                TwoColumnRange<Integer> originalKey = new TwoColumnRange<Integer>(0, 10);
                TwoColumnRange<Integer> keyEqualRange = new TwoColumnRange<Integer>(0, 10);
                TwoColumnRange<Integer> keySubRangeEqualUpperBound = new TwoColumnRange<Integer>(1, 10);
                TwoColumnRange<Integer> keySubRangeEqualLowerBound = new TwoColumnRange<Integer>(0, 9);
                TwoColumnRange<Integer> keySubRange = new TwoColumnRange<Integer>(5, 5);
                TwoColumnRange<Integer> keyPartiallyOverlappingRange = new TwoColumnRange<Integer>(5, 15);
                TwoColumnRange<Integer> keyDisjunctRange = new TwoColumnRange<Integer>(11, 15);
        
                assertTrue(keyEqualRange.isLowerOrEqualUpperBound(originalKey));
                assertTrue(keySubRangeEqualUpperBound.isLowerOrEqualUpperBound(originalKey));
                assertTrue(keySubRangeEqualLowerBound.isLowerOrEqualUpperBound(originalKey));
                assertTrue(keySubRange.isLowerOrEqualUpperBound(originalKey));
                assertFalse(keyPartiallyOverlappingRange.isLowerOrEqualUpperBound(originalKey));
                assertFalse(keyDisjunctRange.isLowerOrEqualUpperBound(originalKey));
            }
}
