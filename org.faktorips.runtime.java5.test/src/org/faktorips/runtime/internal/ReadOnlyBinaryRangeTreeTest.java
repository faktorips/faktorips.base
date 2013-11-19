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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.KeyType;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.TwoColumnKey;
import org.junit.Test;

/**
 * 
 * @author Peter Erzberger
 */
public class ReadOnlyBinaryRangeTreeTest {

    @Test
    public void testTwoCulmnTreeBasedOnStrings() {
        Map<TwoColumnKey<String>, Integer> map = new HashMap<TwoColumnKey<String>, Integer>();
        map.put(new TwoColumnKey<String>("a", "c"), new Integer(42));
        map.put(new TwoColumnKey<String>("d", "z"), new Integer(43));

        ReadOnlyBinaryRangeTree<String, Integer> tree = new ReadOnlyBinaryRangeTree<String, Integer>(map,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_TWO_COLUMN_KEY);
        assertEquals(new Integer(43), tree.getValue("m"));
    }

    @Test
    public void testLowerRangeTreeBasedOnStrings() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", new Integer(42));
        map.put("m", new Integer(43));
        map.put("x", new Integer(44));

        ReadOnlyBinaryRangeTree<String, Integer> tree = new ReadOnlyBinaryRangeTree<String, Integer>(map,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_LOWER_BOUND_EQUAL);
        assertEquals(new Integer(42), tree.getValue("l"));
        assertEquals(new Integer(43), tree.getValue("o"));
    }

    @Test
    public void testLowerBoundEqualGetValue() {
        ReadOnlyBinaryRangeTree<Integer, Integer> tree = createTreeWidthIntegerValues(10, 10, 10,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_LOWER_BOUND_EQUAL);

        Integer lowerBound = tree.getValue(new Integer(5));
        assertNull(lowerBound);

        lowerBound = tree.getValue(new Integer(110));
        assertEquals(new Integer(100), lowerBound);

        lowerBound = tree.getValue(new Integer(10));
        assertEquals(new Integer(10), lowerBound);

        lowerBound = tree.getValue(new Integer(19));
        assertEquals(new Integer(10), lowerBound);

        lowerBound = tree.getValue(new Integer(100));
        assertEquals(new Integer(100), lowerBound);

        lowerBound = tree.getValue(new Integer(35));
        assertEquals(new Integer(30), lowerBound);

        lowerBound = tree.getValue(new Integer(69));
        assertEquals(new Integer(60), lowerBound);
    }

    @Test
    public void testLowerBoundGetValue() {
        ReadOnlyBinaryRangeTree<Integer, Integer> tree = createTreeWidthIntegerValues(10, 10, 10,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_LOWER_BOUND);

        Integer lowerBound = tree.getValue(new Integer(10));
        assertNull(lowerBound);

        lowerBound = tree.getValue(new Integer(11));
        assertEquals(new Integer(10), lowerBound);

    }

    @Test
    public void testUpperBoundEqualGetValue() {
        ReadOnlyBinaryRangeTree<Integer, Integer> tree = createTreeWidthIntegerValues(10, 10, 10,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_UPPER_BOUND_EQUAL);

        Integer upperBound = tree.getValue(new Integer(5));
        assertEquals(new Integer(10), upperBound);

        upperBound = tree.getValue(new Integer(110));
        assertNull(upperBound);

        upperBound = tree.getValue(new Integer(100));
        assertEquals(new Integer(100), upperBound);

        upperBound = tree.getValue(new Integer(10));
        assertEquals(new Integer(10), upperBound);

        upperBound = tree.getValue(new Integer(26));
        assertEquals(new Integer(30), upperBound);

        upperBound = tree.getValue(new Integer(76));
        assertEquals(new Integer(80), upperBound);

        upperBound = tree.getValue(new Integer(30));
        assertEquals(new Integer(30), upperBound);

        upperBound = tree.getValue(new Integer(80));
        assertEquals(new Integer(80), upperBound);
    }

    @Test
    public void testUpperBoundGetValue() {
        ReadOnlyBinaryRangeTree<Integer, Integer> tree = createTreeWidthIntegerValues(10, 10, 10,
                ReadOnlyBinaryRangeTree.KeyType.KEY_IS_UPPER_BOUND);

        Integer upperBound = tree.getValue(new Integer(10));
        assertEquals(new Integer(20), upperBound);

        upperBound = tree.getValue(new Integer(9));
        assertEquals(new Integer(10), upperBound);
    }

    @Test
    public void testTwoColumnRangeGetValue() {
        ReadOnlyBinaryRangeTree<Integer, Integer> tree = createTwoColumnTreeWithIntegerValues(10, 2, 1);

        Integer value = tree.getValue(new Integer(-2));
        assertNull(value);

        value = tree.getValue(new Integer(-1));
        assertNull(value);

        value = tree.getValue(new Integer(0));
        assertEquals(new Integer(1), value);

        value = tree.getValue(new Integer(1));
        assertEquals(new Integer(1), value);

        value = tree.getValue(new Integer(2));
        assertEquals(new Integer(1), value);

        value = tree.getValue(new Integer(3));
        assertNull(value);

        value = tree.getValue(new Integer(4));
        assertEquals(new Integer(5), value);

        value = tree.getValue(new Integer(5));
        assertEquals(new Integer(5), value);

        value = tree.getValue(new Integer(6));
        assertEquals(new Integer(5), value);

        value = tree.getValue(new Integer(7));
        assertNull(value);
    }

    @Test
    public void testTwoColumnRangeWithZipCodes() {
        // There was a test case with the number of 35 nodes that caused the tree to break during
        // initialization
        createTwoColumnTreeWithIntegerValues(35, 0, 0);
    }

    public static ReadOnlyBinaryRangeTree<Integer, Integer> createTreeWidthIntegerValues(int nodeCount,
            int startPos,
            int rangeWidth,
            KeyType keyType) {

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            Integer value = new Integer(startPos + rangeWidth * i);
            map.put(value, value);
        }
        return new ReadOnlyBinaryRangeTree<Integer, Integer>(map, keyType);
    }

    public static ReadOnlyBinaryRangeTree<Integer, Integer> createTwoColumnTreeWithIntegerValues(int nodeCount,
            int nodeWidth,
            int gap) {

        final Map<TwoColumnKey<Integer>, Integer> map = new HashMap<TwoColumnKey<Integer>, Integer>(nodeCount);

        for (int i = 0; i < nodeCount; i++) {
            final Integer lowerBound = new Integer((nodeWidth + gap + 1) * (i));
            final Integer upperBound = new Integer(lowerBound.intValue() + nodeWidth);
            final Integer value = new Integer(lowerBound.intValue() + nodeWidth / 2);
            final TwoColumnKey<Integer> key = new TwoColumnKey<Integer>(lowerBound, upperBound);
            map.put(key, value);
        }

        return new ReadOnlyBinaryRangeTree<Integer, Integer>(map, KeyType.KEY_IS_TWO_COLUMN_KEY);
    }

}
