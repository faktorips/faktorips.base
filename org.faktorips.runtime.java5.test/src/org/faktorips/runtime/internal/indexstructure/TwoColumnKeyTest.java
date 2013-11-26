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

package org.faktorips.runtime.internal.indexstructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TwoColumnKeyTest {
    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testConstructor_lowerBound() {
        new TwoColumnKey<String>(null, "B");
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void testConstructor_upperBound() {
        new TwoColumnKey<String>("A", null);
    }

    @Test
    public void testHashCode() {
        TwoColumnKey<Integer> key1 = new TwoColumnKey<Integer>(0, 10);
        TwoColumnKey<Integer> key2 = new TwoColumnKey<Integer>(0, 25);
        TwoColumnKey<Integer> key3 = new TwoColumnKey<Integer>(1, 10);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotSame(key1.hashCode(), key3.hashCode());
        assertNotSame(key2.hashCode(), key3.hashCode());
    }

    @Test
    public void testIsSubRangeOf() {
        TwoColumnKey<Integer> originalKey = new TwoColumnKey<Integer>(0, 10);
        TwoColumnKey<Integer> keyEqualRange = new TwoColumnKey<Integer>(0, 10);
        TwoColumnKey<Integer> keySubRangeEqualUpperBound = new TwoColumnKey<Integer>(1, 10);
        TwoColumnKey<Integer> keySubRangeEqualLowerBound = new TwoColumnKey<Integer>(0, 9);
        TwoColumnKey<Integer> keySubRange = new TwoColumnKey<Integer>(5, 5);
        TwoColumnKey<Integer> keyPartiallyOverlappingRange = new TwoColumnKey<Integer>(5, 15);
        TwoColumnKey<Integer> keyDisjunctRange = new TwoColumnKey<Integer>(11, 15);

        assertTrue(keyEqualRange.isSubRangeOf(originalKey));
        assertTrue(keySubRangeEqualUpperBound.isSubRangeOf(originalKey));
        assertTrue(keySubRangeEqualLowerBound.isSubRangeOf(originalKey));
        assertTrue(keySubRange.isSubRangeOf(originalKey));
        assertFalse(keyPartiallyOverlappingRange.isSubRangeOf(originalKey));
        assertFalse(keyDisjunctRange.isSubRangeOf(originalKey));
    }
}
