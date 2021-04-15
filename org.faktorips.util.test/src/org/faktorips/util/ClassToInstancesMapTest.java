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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ClassToInstancesMapTest {

    @Test
    public void testGet() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        classToMultiInstanceMap.put(String.class, "123");
        List<String> result = classToMultiInstanceMap.get(String.class);
        assertArrayEquals(new String[] { "123" }, result.toArray());

        result.add("321");
        List<String> result2 = classToMultiInstanceMap.get(String.class);
        assertArrayEquals(new String[] { "123", "321" }, result2.toArray());
        assertEquals(result, result2);

        classToMultiInstanceMap.put(Integer.class, 123);
        assertArrayEquals(new Integer[] { 123 }, classToMultiInstanceMap.get(Integer.class).toArray());
        assertArrayEquals(new String[] { "123", "321" }, classToMultiInstanceMap.get(String.class).toArray());
    }

    @Test
    public void testPut() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        List<String> result = classToMultiInstanceMap.put(String.class, "123");
        assertArrayEquals(new String[] { "123" }, result.toArray());

        List<String> result2 = classToMultiInstanceMap.put(String.class, "321");
        assertArrayEquals(new String[] { "123", "321" }, result.toArray());
        assertArrayEquals(new String[] { "123", "321" }, result2.toArray());
        assertEquals(result, result2);

        List<Integer> result3 = classToMultiInstanceMap.put(Integer.class, 123);
        assertArrayEquals(new Integer[] { 123 }, result3.toArray());
    }

    @Test
    public void testSize() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        assertEquals(0, classToMultiInstanceMap.size());
        classToMultiInstanceMap.put(String.class, "123");
        assertEquals(1, classToMultiInstanceMap.size());
        classToMultiInstanceMap.put(String.class, "321");
        assertEquals(2, classToMultiInstanceMap.size());
        classToMultiInstanceMap.put(Integer.class, 123);
        assertEquals(3, classToMultiInstanceMap.size());
        classToMultiInstanceMap.removeAll(String.class);
        assertEquals(1, classToMultiInstanceMap.size());
        classToMultiInstanceMap.remove(Integer.class, 123);
        assertEquals(0, classToMultiInstanceMap.size());
    }

    @Test
    public void testSizeClassOfK() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        assertEquals(0, classToMultiInstanceMap.size(String.class));
        assertEquals(0, classToMultiInstanceMap.size(Integer.class));
        classToMultiInstanceMap.put(String.class, "123");
        assertEquals(1, classToMultiInstanceMap.size(String.class));
        assertEquals(0, classToMultiInstanceMap.size(Integer.class));
        classToMultiInstanceMap.put(String.class, "321");
        assertEquals(2, classToMultiInstanceMap.size(String.class));
        assertEquals(0, classToMultiInstanceMap.size(Integer.class));
        classToMultiInstanceMap.put(Integer.class, 123);
        assertEquals(2, classToMultiInstanceMap.size(String.class));
        assertEquals(1, classToMultiInstanceMap.size(Integer.class));
        classToMultiInstanceMap.removeAll(String.class);
        assertEquals(0, classToMultiInstanceMap.size(String.class));
        assertEquals(1, classToMultiInstanceMap.size(Integer.class));
        classToMultiInstanceMap.remove(Integer.class, 123);
        assertEquals(0, classToMultiInstanceMap.size(String.class));
        assertEquals(0, classToMultiInstanceMap.size(Integer.class));
    }

    @Test
    public void testIsEmpty() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        assertTrue(classToMultiInstanceMap.isEmpty());
        classToMultiInstanceMap.put(String.class, "123");
        assertFalse(classToMultiInstanceMap.isEmpty());
        classToMultiInstanceMap.put(String.class, "321");
        assertFalse(classToMultiInstanceMap.isEmpty());
        classToMultiInstanceMap.put(Integer.class, 123);
        assertFalse(classToMultiInstanceMap.isEmpty());
        classToMultiInstanceMap.removeAll(String.class);
        assertFalse(classToMultiInstanceMap.isEmpty());
        classToMultiInstanceMap.remove(Integer.class, 123);
        assertTrue(classToMultiInstanceMap.isEmpty());
    }

    @Test
    public void testRemoveAndContainsValue() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        assertFalse(classToMultiInstanceMap.containsValue("123"));
        assertFalse(classToMultiInstanceMap.containsValue("321"));
        assertFalse(classToMultiInstanceMap.containsValue(123));

        classToMultiInstanceMap.put(String.class, "123");
        assertTrue(classToMultiInstanceMap.containsValue("123"));
        assertFalse(classToMultiInstanceMap.containsValue("321"));
        assertFalse(classToMultiInstanceMap.containsValue(123));

        classToMultiInstanceMap.put(String.class, "321");
        assertTrue(classToMultiInstanceMap.containsValue("123"));
        assertTrue(classToMultiInstanceMap.containsValue("321"));
        assertFalse(classToMultiInstanceMap.containsValue(123));

        classToMultiInstanceMap.put(Integer.class, 123);
        assertTrue(classToMultiInstanceMap.containsValue("123"));
        assertTrue(classToMultiInstanceMap.containsValue("321"));
        assertTrue(classToMultiInstanceMap.containsValue(123));

        classToMultiInstanceMap.removeAll(String.class);
        assertFalse(classToMultiInstanceMap.containsValue("123"));
        assertFalse(classToMultiInstanceMap.containsValue("321"));
        assertTrue(classToMultiInstanceMap.containsValue(123));

        classToMultiInstanceMap.remove(Integer.class, 123);
        assertFalse(classToMultiInstanceMap.containsValue("123"));
        assertFalse(classToMultiInstanceMap.containsValue("321"));
        assertFalse(classToMultiInstanceMap.containsValue(123));
    }

    @Test
    public void testClear() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        classToMultiInstanceMap.put(String.class, "123");
        classToMultiInstanceMap.put(String.class, "321");
        classToMultiInstanceMap.put(Integer.class, 123);
        assertEquals(3, classToMultiInstanceMap.size());
        classToMultiInstanceMap.clear();
        assertEquals(0, classToMultiInstanceMap.size());
    }

    @Test
    public void testValues() {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();

        List<Object> values = classToMultiInstanceMap.values();
        assertEquals(0, values.size());

        classToMultiInstanceMap.put(String.class, "a321");
        classToMultiInstanceMap.put(String.class, "a123");
        classToMultiInstanceMap.put(Integer.class, 123);

        // assert that values is a passive copy
        assertArrayEquals(new Object[] {}, values.toArray());

        values = classToMultiInstanceMap.values();
        assertEquals(3, values.size());

        assertTrue(values.contains("a123"));
        assertTrue(values.contains("a321"));
        assertTrue(values.contains(123));

        // values are ordered by class and by natural order
        assertEquals(123, values.get(0));
        assertEquals("a321", values.get(1));
        assertEquals("a123", values.get(2));
    }

    @Test
    public void testContainsValuesOf() throws Exception {
        ClassToInstancesMap<Object> classToMultiInstanceMap = new ClassToInstancesMap<>();
        assertFalse(classToMultiInstanceMap.containsValuesOf(String.class));

        classToMultiInstanceMap.put("asd");
        assertTrue(classToMultiInstanceMap.containsValuesOf(String.class));
        assertFalse(classToMultiInstanceMap.containsValuesOf(Integer.class));
    }
}
