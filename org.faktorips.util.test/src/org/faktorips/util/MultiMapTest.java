package org.faktorips.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class MultiMapTest {

    private final MultiMap<Integer, String> multiMapDifferentStringRepresentations = new MultiMap<Integer, String>();

    @Test
    public void testPut() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(10, "ten");
        multiMapDifferentStringRepresentations.put(10, "TEN");

        Collection<String> stringCollection = multiMapDifferentStringRepresentations.get(10);

        assertEquals(3, stringCollection.size());
        assertTrue(stringCollection.contains("10"));
        assertTrue(stringCollection.contains("ten"));
        assertTrue(stringCollection.contains("TEN"));
    }

    @Test
    public void testPut_DifferntKeys() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(10, "ten");
        multiMapDifferentStringRepresentations.put(12, "12");

        Collection<String> stringCollection10 = multiMapDifferentStringRepresentations.get(10);
        Collection<String> stringCollection12 = multiMapDifferentStringRepresentations.get(12);

        assertEquals(2, stringCollection10.size());
        assertEquals(1, stringCollection12.size());
    }

    @Test
    public void testPut_defenceCopy() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(10, "ten");
        Collection<String> collection = multiMapDifferentStringRepresentations.get(10);
        try {
            collection.add("abc123");
        } catch (UnsupportedOperationException u) {
            System.out.println("Not allowed to insert values directly in Collection");
        }

        Collection<String> expectedCollection = multiMapDifferentStringRepresentations.get(10);

        assertEquals(2, expectedCollection.size());
        assertTrue(expectedCollection.contains("10"));
        assertTrue(expectedCollection.contains("ten"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut_defenceCopyEmptyList() {
        Collection<String> collection = multiMapDifferentStringRepresentations.get(10);
        collection.add("abc123");
    }

    @Test
    public void testRemove() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(10, "ten");
        multiMapDifferentStringRepresentations.put(10, "TEN");
        multiMapDifferentStringRepresentations.remove(10, "ten");

        Collection<String> stringCollection = multiMapDifferentStringRepresentations.get(10);

        assertEquals(2, stringCollection.size());
        assertTrue(stringCollection.contains("10"));
        assertTrue(stringCollection.contains("TEN"));
        assertFalse(stringCollection.contains("ten"));
    }

    @Test
    public void testRemove_DifferntKeys() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(10, "ten");
        multiMapDifferentStringRepresentations.remove(10, "ten");

        multiMapDifferentStringRepresentations.put(12, "12");
        multiMapDifferentStringRepresentations.remove(12, "12");

        Collection<String> stringCollection10 = multiMapDifferentStringRepresentations.get(10);
        Collection<String> stringCollection12 = multiMapDifferentStringRepresentations.get(12);

        assertEquals(1, stringCollection10.size());
        assertEquals(0, stringCollection12.size());
    }

    @Test
    public void testClear() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(12, "12");
        multiMapDifferentStringRepresentations.clear();

        Collection<String> stringCollection10 = multiMapDifferentStringRepresentations.get(10);
        Collection<String> stringCollection12 = multiMapDifferentStringRepresentations.get(12);

        assertTrue(stringCollection10.isEmpty());
        assertTrue(stringCollection12.isEmpty());
    }
}
