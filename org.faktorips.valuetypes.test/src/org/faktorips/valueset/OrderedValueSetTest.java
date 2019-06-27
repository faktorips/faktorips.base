/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class OrderedValueSetTest {

    // Warning doesn't matter as only constructor is tested
    @SuppressWarnings("unused")
    @Test
    public void testConstructor() {
        try {
            new OrderedValueSet<Integer>(false, null, Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(1));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            new OrderedValueSet<Integer>(false, null, Integer.valueOf(1), null, Integer.valueOf(2), Integer.valueOf(3), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void testGetValues() {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));

        Set<Integer> valuesAsSet = new HashSet<Integer>();
        valuesAsSet.add(Integer.valueOf(1));
        valuesAsSet.add(Integer.valueOf(2));
        valuesAsSet.add(Integer.valueOf(3));

        valueSet = new OrderedValueSet<Integer>(valuesAsSet, false, null);
        assertEquals(Arrays.asList(valuesAsSet.toArray()), Arrays.asList(valueSet.getValues(false).toArray()));

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null };
        valueSet = new OrderedValueSet<Integer>(true, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));
        List<Integer> expectedValues = new ArrayList<Integer>();
        expectedValues.add(values[0]);
        expectedValues.add(values[1]);
        expectedValues.add(values[2]);
        assertEquals(expectedValues, Arrays.asList(valueSet.getValues(true).toArray()));
    }

    @Test
    public void testIsDiscrete() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertTrue(valueSet.isDiscrete());
    }

    @Test
    public void testContains() {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(true, null, values);

        assertTrue(valueSet.contains(Integer.valueOf(2)));
        assertTrue(valueSet.contains(null));
        assertFalse(valueSet.contains(Integer.valueOf(5)));
    }

    @Test
    public void testContainsNull() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertFalse(valueSet.containsNull());

        valueSet = new OrderedValueSet<Object>(true, null, new Object[0]);
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testIsEmpty() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertTrue(valueSet.isEmpty());

        valueSet = new OrderedValueSet<Object>(true, null, new Object[] { null });
        assertFalse(valueSet.isEmpty());

        Object[] values = new Object[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        valueSet = new OrderedValueSet<Object>(false, null, values);
        assertFalse(valueSet.isEmpty());
    }

    @Test
    public void testSize() {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals(3, valueSet.size());
    }

    @Test
    public void testSerializable() throws Exception {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        TestUtil.testSerializable(valueSet);
    }

    @Test
    public void testEquals() {

        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<Integer>(false, null, values);

        assertEquals(valueSet, valueSet2);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.equals(valueSet3));

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), null };
        OrderedValueSet<Integer> valueSet4 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.equals(valueSet4));
    }

    @Test
    public void testHashCode() {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<Integer>(false, null, values);

        assertEquals(valueSet.hashCode(), valueSet2.hashCode());

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.hashCode() == valueSet3.hashCode());

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), null };
        OrderedValueSet<Integer> valueSet4 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.hashCode() == valueSet4.hashCode());
    }

    @Test
    public void testToString() {
        Integer[] values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals("[1, 2, 3]", valueSet.toString());
    }

}
