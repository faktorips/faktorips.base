/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class MultiMapTest {

    private MultiMap<Integer, String> multiMap = new MultiMap<>();

    @Test
    public void testPut() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.put(10, "TEN");

        Collection<String> stringCollection = multiMap.get(10);

        assertEquals(3, stringCollection.size());
        assertTrue(stringCollection.contains("10"));
        assertTrue(stringCollection.contains("ten"));
        assertTrue(stringCollection.contains("TEN"));
    }

    @Test
    public void testPut_DifferntKeys() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.put(12, "12");

        Collection<String> stringCollection10 = multiMap.get(10);
        Collection<String> stringCollection12 = multiMap.get(12);

        assertEquals(2, stringCollection10.size());
        assertEquals(1, stringCollection12.size());
    }

    @Test
    public void testPut_defenceCopy() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        Collection<String> collection = multiMap.get(10);
        try {
            collection.add("abc123");
        } catch (UnsupportedOperationException u) {
            System.out.println("Not allowed to insert values directly in Collection");
        }

        Collection<String> expectedCollection = multiMap.get(10);

        assertEquals(2, expectedCollection.size());
        assertTrue(expectedCollection.contains("10"));
        assertTrue(expectedCollection.contains("ten"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut_defenceCopyEmptyList() {
        Collection<String> collection = multiMap.get(10);
        collection.add("abc123");
    }

    @Test
    public void testPut_multiValue() {
        multiMap.put(1, "a", "b");

        Collection<String> stringCollection = multiMap.get(1);

        assertEquals(2, stringCollection.size());
        assertThat(stringCollection, hasItems("a", "b"));
    }

    @Test
    public void testPut_multiValueList() {
        multiMap.put(1, Arrays.asList("a", "b"));

        Collection<String> stringCollection = multiMap.get(1);

        assertEquals(2, stringCollection.size());
        assertThat(stringCollection, hasItems("a", "b"));
    }

    @Test
    public void testRemove() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.put(10, "TEN");

        multiMap.remove(10, "ten");
        Collection<String> stringCollection = multiMap.get(10);

        assertEquals(2, stringCollection.size());
        assertTrue(stringCollection.contains("10"));
        assertTrue(stringCollection.contains("TEN"));
        assertFalse(stringCollection.contains("ten"));
    }

    @Test
    public void testRemove_DifferntKeys() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.remove(10, "ten");

        multiMap.put(12, "12");
        multiMap.remove(12, "12");

        Collection<String> stringCollection10 = multiMap.get(10);
        Collection<String> stringCollection12 = multiMap.get(12);

        assertEquals(1, stringCollection10.size());
        assertEquals(0, stringCollection12.size());
    }

    @Test
    public void testRemoveObject() throws Exception {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.put(10, "TEN");

        Collection<String> removedCollection = multiMap.remove(10);
        Collection<String> stringCollection = multiMap.get(10);

        assertTrue(stringCollection.isEmpty());
        assertEquals(3, removedCollection.size());
        assertTrue(removedCollection.contains("10"));
        assertTrue(removedCollection.contains("TEN"));
        assertTrue(removedCollection.contains("ten"));
    }

    @Test
    public void testRemoveObject_DifferntKeys() {
        multiMap.put(10, "10");
        multiMap.put(10, "ten");
        multiMap.put(12, "12");

        Collection<String> removedCollection = multiMap.remove(10);
        Collection<String> stringCollection10 = multiMap.get(10);
        Collection<String> stringCollection12 = multiMap.get(12);

        assertTrue(stringCollection10.isEmpty());
        assertEquals(1, stringCollection12.size());
        assertThat(stringCollection12, hasItem("12"));
        assertEquals(2, removedCollection.size());
        assertThat(removedCollection, hasItems("10", "ten"));
    }

    @Test
    public void testClear() {
        multiMap.put(10, "10");
        multiMap.put(12, "12");
        multiMap.clear();

        Collection<String> stringCollection10 = multiMap.get(10);
        Collection<String> stringCollection12 = multiMap.get(12);

        assertTrue(stringCollection10.isEmpty());
        assertTrue(stringCollection12.isEmpty());
    }

    @Test
    public void testPutReplace_noPreviousEntry() throws Exception {
        List<String> list = Arrays.asList("a", "b", "c");
        multiMap.putReplace(1, list);

        Collection<String> result = multiMap.get(1);

        assertEquals(list, new ArrayList<>(result));
    }

    @Test
    public void testPutReplace_withPreviousEntry() throws Exception {
        multiMap.put(1, "x", "y", "z");
        List<String> list = Arrays.asList("a", "b", "c");
        multiMap.putReplace(1, list);

        Collection<String> result = multiMap.get(1);

        assertEquals(list, new ArrayList<>(result));
    }

    @Test
    public void testPutReplace_wrongCollectionType() throws Exception {
        multiMap = MultiMap.createWithSetsAsValues();
        multiMap.putReplace(1, Arrays.asList("x", "y", "z"));
        List<String> list = Arrays.asList("a", "b", "c");
        multiMap.putReplace(1, list);

        Collection<String> result = multiMap.get(1);

        assertEquals(list.size(), result.size());
        for (String expectedValue : list) {
            assertThat(result, hasItem(expectedValue));
        }
    }

    @Test
    public void testMerge_differentKeys() throws Exception {
        multiMap.put(1, "a");
        MultiMap<Integer, String> otherMultiMap = new MultiMap<>();
        otherMultiMap.put(2, "b");

        multiMap.merge(otherMultiMap);

        assertEquals(2, multiMap.keySet().size());
        assertEquals(1, multiMap.get(1).size());
        assertEquals(1, multiMap.get(2).size());
        assertThat(multiMap.get(1), hasItem("a"));
        assertThat(multiMap.get(2), hasItem("b"));
        assertEquals(1, otherMultiMap.keySet().size());
        assertEquals(1, otherMultiMap.get(2).size());
        assertThat(otherMultiMap.get(2), hasItem("b"));
    }

    @Test
    public void testMerge_sameKeys() throws Exception {
        multiMap.put(1, "a");
        MultiMap<Integer, String> otherMultiMap = new MultiMap<>();
        otherMultiMap.put(1, "b");

        multiMap.merge(otherMultiMap);

        assertEquals(1, multiMap.keySet().size());
        assertEquals(2, multiMap.get(1).size());
        assertThat(multiMap.get(1), hasItem("a"));
        assertThat(multiMap.get(1), hasItem("b"));
        assertEquals(1, otherMultiMap.keySet().size());
        assertEquals(1, otherMultiMap.get(1).size());
        assertThat(otherMultiMap.get(1), hasItem("b"));
    }

    @Test
    public void testMerge_sameKeysSameValue_inList() throws Exception {
        multiMap.put(1, "a");
        MultiMap<Integer, String> otherMultiMap = new MultiMap<>();
        otherMultiMap.put(1, "a");

        multiMap.merge(otherMultiMap);

        assertEquals(1, multiMap.keySet().size());
        assertEquals(2, multiMap.get(1).size());
        assertThat(multiMap.get(1), hasItems("a", "a"));
        assertEquals(1, otherMultiMap.keySet().size());
        assertEquals(1, otherMultiMap.get(1).size());
        assertThat(otherMultiMap.get(1), hasItem("a"));
    }

    @Test
    public void testMerge_sameKeysSameValue_inSet() throws Exception {
        multiMap = MultiMap.createWithSetsAsValues();
        multiMap.put(1, "a");
        MultiMap<Integer, String> otherMultiMap = new MultiMap<>();
        otherMultiMap.put(1, "a");

        multiMap.merge(otherMultiMap);

        assertEquals(1, multiMap.keySet().size());
        assertEquals(1, multiMap.get(1).size());
        assertThat(multiMap.get(1), hasItem("a"));
        assertEquals(1, otherMultiMap.keySet().size());
        assertEquals(1, otherMultiMap.get(1).size());
        assertThat(otherMultiMap.get(1), hasItem("a"));
    }

    @Test
    public void testMerge_multipleEntries() throws Exception {
        multiMap = MultiMap.createWithSetsAsValues();
        multiMap.put(1, "a", "b");
        multiMap.put(2, "m", "n");
        multiMap.put(3, "x", "y");
        MultiMap<Integer, String> otherMultiMap = new MultiMap<>();
        otherMultiMap.put(2, "n", "o");
        otherMultiMap.put(3, "y", "z");
        otherMultiMap.put(4, "0", "1");

        multiMap.merge(otherMultiMap);

        assertEquals(4, multiMap.keySet().size());
        assertEquals(2, multiMap.get(1).size());
        assertEquals(3, multiMap.get(2).size());
        assertEquals(3, multiMap.get(3).size());
        assertEquals(2, multiMap.get(4).size());
        assertThat(multiMap.get(1), hasItems("a", "b"));
        assertThat(multiMap.get(2), hasItems("m", "n", "o"));
        assertThat(multiMap.get(3), hasItems("x", "y", "z"));
        assertThat(multiMap.get(4), hasItems("0", "1"));
        assertEquals(3, otherMultiMap.keySet().size());
        assertEquals(2, otherMultiMap.get(2).size());
        assertEquals(2, otherMultiMap.get(3).size());
        assertEquals(2, otherMultiMap.get(4).size());
        assertThat(otherMultiMap.get(2), hasItems("n", "o"));
        assertThat(otherMultiMap.get(3), hasItems("y", "z"));
        assertThat(otherMultiMap.get(4), hasItems("0", "1"));
    }

    @Test
    public void testCount() throws Exception {
        multiMap.put(1, "a", "b");
        multiMap.put(2, "a", "a");
        multiMap.put(3, "x", "a");

        int count = multiMap.count();

        assertEquals(6, count);
    }

    @Test
    public void testCount_withSet() throws Exception {
        multiMap = MultiMap.createWithSetsAsValues();
        multiMap.put(1, "a", "b");
        multiMap.put(2, "a", "a");
        multiMap.put(3, "x", "a");

        int count = multiMap.count();

        assertEquals(5, count);
    }

    @Test
    public void testCount_empty() throws Exception {

        int count = multiMap.count();

        assertEquals(0, count);
    }

    @Test
    public void testKeySet_backingMap() throws Exception {
        multiMap.put(1, "a", "b");
        multiMap.put(2, "a", "a");
        multiMap.put(3, "x", "a");

        multiMap.keySet().remove(2);

        assertEquals(2, multiMap.keySet().size());
        assertEquals(2, multiMap.get(1).size());
        assertEquals(2, multiMap.get(3).size());
        assertThat(multiMap.get(1), hasItems("a", "b"));
        assertThat(multiMap.get(3), hasItems("x", "a"));
    }

}
