package org.faktorips.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
    public void testClear() {
        multiMapDifferentStringRepresentations.put(10, "10");
        multiMapDifferentStringRepresentations.put(12, "12");
        multiMapDifferentStringRepresentations.clear();

        Collection<String> stringCollection10 = multiMapDifferentStringRepresentations.get(10);
        Collection<String> stringCollection12 = multiMapDifferentStringRepresentations.get(12);

        assertNull(stringCollection10);
        assertNull(stringCollection12);
    }
}
