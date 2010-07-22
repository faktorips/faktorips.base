/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.valueset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class OrderedValueSetTest extends TestCase {

    public void testConstructor() {

        try {
            new OrderedValueSet<Integer>(false, null, new Integer(1), new Integer(2), new Integer(3), new Integer(1));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            new OrderedValueSet<Integer>(false, null, new Integer(1), null, new Integer(2), new Integer(3), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testGetValues() {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));

        Set<Integer> valuesAsSet = new HashSet<Integer>();
        valuesAsSet.add(new Integer(1));
        valuesAsSet.add(new Integer(2));
        valuesAsSet.add(new Integer(3));

        valueSet = new OrderedValueSet<Integer>(valuesAsSet, false, null);
        assertEquals(Arrays.asList(valuesAsSet.toArray()), Arrays.asList(valueSet.getValues(false).toArray()));

        values = new Integer[] { new Integer(1), new Integer(2), new Integer(3), null };
        valueSet = new OrderedValueSet<Integer>(true, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));
        List<Integer> expectedValues = new ArrayList<Integer>();
        expectedValues.add(values[0]);
        expectedValues.add(values[1]);
        expectedValues.add(values[2]);
        assertEquals(expectedValues, Arrays.asList(valueSet.getValues(true).toArray()));
    }

    public void testIsDiscrete() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertTrue(valueSet.isDiscrete());
    }

    public void testContains() {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3), null };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(true, null, values);

        assertTrue(valueSet.contains(new Integer(2)));
        assertTrue(valueSet.contains(null));
        assertFalse(valueSet.contains(new Integer(5)));
    }

    public void testContainsNull() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertFalse(valueSet.containsNull());

        valueSet = new OrderedValueSet<Object>(true, null, new Object[0]);
        assertTrue(valueSet.containsNull());
    }

    public void testIsEmpty() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<Object>(false, null, new Object[0]);
        assertTrue(valueSet.isEmpty());

        valueSet = new OrderedValueSet<Object>(true, null, new Object[] { null });
        assertFalse(valueSet.isEmpty());

        Object[] values = new Object[] { new Integer(1), new Integer(2), new Integer(3) };
        valueSet = new OrderedValueSet<Object>(false, null, values);
        assertFalse(valueSet.isEmpty());
    }

    public void testSize() {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals(3, valueSet.size());
    }

    public void testSerializable() throws Exception {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        TestUtil.testSerializable(valueSet);
    }

    public void testEquals() {

        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);

        values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<Integer>(false, null, values);

        assertEquals(valueSet, valueSet2);

        values = new Integer[] { new Integer(1), new Integer(2), new Integer(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.equals(valueSet3));

        values = new Integer[] { new Integer(1), new Integer(2), null };
        OrderedValueSet<Integer> valueSet4 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.equals(valueSet4));
    }

    public void testHashCode() {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);

        values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<Integer>(false, null, values);

        assertEquals(valueSet.hashCode(), valueSet2.hashCode());

        values = new Integer[] { new Integer(1), new Integer(2), new Integer(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.hashCode() == valueSet3.hashCode());

        values = new Integer[] { new Integer(1), new Integer(2), null };
        OrderedValueSet<Integer> valueSet4 = new OrderedValueSet<Integer>(false, null, values);

        assertFalse(valueSet.hashCode() == valueSet4.hashCode());
    }

    public void testToString() {
        Integer[] values = new Integer[] { new Integer(1), new Integer(2), new Integer(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<Integer>(false, null, values);
        assertEquals("[1, 2, 3]", valueSet.toString());
    }

}
