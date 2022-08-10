package org.faktorips.valueset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class NaturalOrderedValueSetTest {

    // Warning doesn't matter as only constructor is tested
    @SuppressWarnings("unused")
    @Test
    public void testConstructor_Exception() {
        try {
            new NaturalOrderedValueSet<>(false, null, Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3),
                    Integer.valueOf(1));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void testConstructor_Array() {
        Integer[] values = { Integer.valueOf(3), Integer.valueOf(1), null,
                Integer.valueOf(2) };
        Integer[] valuesSorted = { Integer.valueOf(1), Integer.valueOf(2),
                Integer.valueOf(3), null };
        NaturalOrderedValueSet<Integer> valueSet = new NaturalOrderedValueSet<>(values);
        assertArrayEquals(valuesSorted, valueSet.getValues().toArray());
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testConstructor_Collection() {
        List<Integer> values = Arrays.asList(Integer.valueOf(3), Integer.valueOf(1), null,
                Integer.valueOf(2));
        Integer[] valuesSorted = { Integer.valueOf(1), Integer.valueOf(2),
                Integer.valueOf(3), null };
        NaturalOrderedValueSet<Integer> valueSet = new NaturalOrderedValueSet<>(values);
        assertArrayEquals(valuesSorted, valueSet.getValues().toArray());
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testEmpty() {
        NaturalOrderedValueSet<Integer> emptyValueSet = new NaturalOrderedValueSet<>();
        List<Integer> expectedValues = new ArrayList<>();
        assertEquals(expectedValues, new ArrayList<>(emptyValueSet.getValues()));
    }

    @Test
    public void testGetLowerBound() {
        Integer[] values = { Integer.valueOf(3), Integer.valueOf(1), null,
                Integer.valueOf(2) };
        NaturalOrderedValueSet<Integer> valueSet = new NaturalOrderedValueSet<>(values);
        assertEquals(Integer.valueOf(1), valueSet.getLowerBound());
    }

    @Test
    public void testGetUpperBound() {
        Integer[] values = { Integer.valueOf(3), Integer.valueOf(1), null,
                Integer.valueOf(2) };
        NaturalOrderedValueSet<Integer> valueSet = new NaturalOrderedValueSet<>(values);
        assertEquals(Integer.valueOf(3), valueSet.getUpperBound());
    }

    @Test
    public void testEquals_IndependentOfOriginalOrder() {
        NaturalOrderedValueSet<Integer> naturalOrderedValueSet1 = new NaturalOrderedValueSet<>(
                Integer.valueOf(3), Integer.valueOf(1), null, Integer.valueOf(2));
        NaturalOrderedValueSet<Integer> naturalOrderedValueSet2 = new NaturalOrderedValueSet<>(
                Integer.valueOf(2), Integer.valueOf(1), null, Integer.valueOf(3));

        assertEquals(naturalOrderedValueSet1, naturalOrderedValueSet2);
        assertEquals(naturalOrderedValueSet2, naturalOrderedValueSet1);
    }

    @Test
    public void testEquals_NotEqualToOrderedValueSet() {
        Integer[] values = { Integer.valueOf(3), Integer.valueOf(1), null,
                Integer.valueOf(2) };
        NaturalOrderedValueSet<Integer> naturalOrderedValueSet = new NaturalOrderedValueSet<>(values);
        OrderedValueSet<Integer> orderedValueSet = new OrderedValueSet<>(Arrays.asList(values));

        assertNotEquals(naturalOrderedValueSet, orderedValueSet);
        assertNotEquals(orderedValueSet, naturalOrderedValueSet);
    }
}
