/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.faktorips.valueset.TestUtil.subsetOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.values.Money;
import org.junit.Test;

public class OrderedValueSetTest {

    // Warning doesn't matter as only constructor is tested
    @SuppressWarnings("unused")
    @Test
    public void testConstructor() {
        try {
            new OrderedValueSet<>(false, null, Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3),
                    Integer.valueOf(1));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            new OrderedValueSet<>(false, null, Integer.valueOf(1), null, Integer.valueOf(2), Integer.valueOf(3),
                    null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }

        try {
            new OrderedValueSet<>(true, null, Money.valueOf("1"), Money.NULL, null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void testEmpty() {
        OrderedValueSet<Integer> emptyValueSet = OrderedValueSet.empty();
        List<Integer> expectedValues = new ArrayList<>();
        assertEquals(expectedValues, Arrays.asList(emptyValueSet.getValues().toArray()));
    }

    @Test
    public void testOf_Array() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), null,
                Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = OrderedValueSet.of(values);
        assertArrayEquals(values, valueSet.getValues().toArray());
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testOf_Collection() {
        List<Integer> values = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), null,
                Integer.valueOf(3));
        OrderedValueSet<Integer> valueSet = OrderedValueSet.of(values);
        assertThat(valueSet.getValues(), hasItems(values.toArray(new Integer[0])));
        assertThat(valueSet.containsNull(), is(true));
    }

    @Test
    public void testOf_Collection_WithNullObject() {
        List<Money> values = List.of(Money.euro(1), Money.euro(2), Money.NULL, Money.euro(3));
        OrderedValueSet<Money> valueSet = OrderedValueSet.of(values);
        assertThat(valueSet.getValues(), hasItems(values.toArray(new Money[0])));
        assertThat(valueSet.containsNull(), is(true));
    }

    @Test
    public void testGetValues() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));

        Set<Integer> valuesAsSet = new HashSet<>();
        valuesAsSet.add(Integer.valueOf(1));
        valuesAsSet.add(Integer.valueOf(2));
        valuesAsSet.add(Integer.valueOf(3));

        valueSet = new OrderedValueSet<>(valuesAsSet, false, null);
        assertEquals(Arrays.asList(valuesAsSet.toArray()), Arrays.asList(valueSet.getValues(false).toArray()));

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null };
        valueSet = new OrderedValueSet<>(true, null, values);
        assertEquals(Arrays.asList(values), Arrays.asList(valueSet.getValues(false).toArray()));
        List<Integer> expectedValues = new ArrayList<>();
        expectedValues.add(values[0]);
        expectedValues.add(values[1]);
        expectedValues.add(values[2]);
        assertEquals(expectedValues, Arrays.asList(valueSet.getValues(true).toArray()));
    }

    @Test
    public void testStream() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), null, Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = OrderedValueSet.<Integer> of(values);
        Stream<Integer> valueStream = valueSet.stream();
        assertThat(valueStream.collect(Collectors.toList()), hasItems(values));
    }

    @Test
    public void testIsDiscrete() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<>(false, null);
        assertTrue(valueSet.isDiscrete());
    }

    @Test
    public void testContains() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), null };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(true, null, values);

        assertTrue(valueSet.contains(Integer.valueOf(2)));
        assertTrue(valueSet.contains(null));
        assertFalse(valueSet.contains(Integer.valueOf(5)));
    }

    @Test
    public void testContainsNull() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<>(false, null);
        assertFalse(valueSet.containsNull());

        valueSet = new OrderedValueSet<>(true, null);
        assertTrue(valueSet.containsNull());
    }

    @Test
    public void testIsEmpty() {
        OrderedValueSet<Object> valueSet = new OrderedValueSet<>(false, null);
        assertTrue(valueSet.isEmpty());

        valueSet = new OrderedValueSet<>(true, null, new Object[] { null });
        assertTrue(valueSet.isEmpty());

        Object[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        valueSet = new OrderedValueSet<>(false, null, values);
        assertFalse(valueSet.isEmpty());
    }

    @Test
    public void testSize() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);
        assertEquals(3, valueSet.size());
    }

    @Test
    public void testSerializable() throws Exception {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);
        TestUtil.testSerializable(valueSet);
    }

    @Test
    public void testEquals() {

        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<>(false, null, values);

        assertEquals(valueSet, valueSet2);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<>(false, null, values);

        assertFalse(valueSet.equals(valueSet3));

        NaturalOrderedValueSet<Integer> naturalOrderedValueSet = new NaturalOrderedValueSet<>(values);

        assertFalse(valueSet3.equals(naturalOrderedValueSet));
    }

    @Test
    public void testHashCode() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet2 = new OrderedValueSet<>(false, null, values);

        assertEquals(valueSet.hashCode(), valueSet2.hashCode());

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4) };
        OrderedValueSet<Integer> valueSet3 = new OrderedValueSet<>(false, null, values);

        assertFalse(valueSet.hashCode() == valueSet3.hashCode());

        values = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), null };
        OrderedValueSet<Integer> valueSet4 = new OrderedValueSet<>(true, null, values);

        assertFalse(valueSet.hashCode() == valueSet4.hashCode());

        NaturalOrderedValueSet<Integer> naturalOrderedValueSet = new NaturalOrderedValueSet<>(values);

        assertEquals(valueSet4.hashCode(), naturalOrderedValueSet.hashCode());
    }

    @Test
    public void testToString() {
        Integer[] values = { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        OrderedValueSet<Integer> valueSet = new OrderedValueSet<>(false, null, values);
        assertEquals("[1, 2, 3]", valueSet.toString());
    }

    @Test
    public void testIsUnrestricted_WithoutNull_includesNull() {
        OrderedValueSet<String> set = new OrderedValueSet<>(false, null, "1", "2", "3", "4");

        // OrderedValueSet is never unrestricted
        assertThat(set.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithNull_includesNull() {
        OrderedValueSet<String> set = new OrderedValueSet<>(true, null);

        // OrderedValueSet is never unrestricted
        assertThat(set.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_WithoutNull_excludesNull() {
        OrderedValueSet<String> set = new OrderedValueSet<>(false, null, "1", "2", "3", "4");

        // OrderedValueSet is never unrestricted
        assertThat(set.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_WithNull_excludesNull() {
        OrderedValueSet<String> set = new OrderedValueSet<>(true, null);

        // OrderedValueSet is never unrestricted
        assertThat(set.isUnrestricted(true), is(false));
    }

    @Test
    public void testIsUnrestricted_Empty_includesNull() {
        OrderedValueSet<String> empty = OrderedValueSet.empty();

        // OrderedValueSet is never unrestricted
        assertThat(empty.isUnrestricted(false), is(false));
    }

    @Test
    public void testIsUnrestricted_Empty_excludesNull() {
        OrderedValueSet<String> empty = OrderedValueSet.empty();

        // OrderedValueSet is never unrestricted
        assertThat(empty.isUnrestricted(true), is(false));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testIsSubsetOf() {
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3), is(subsetOf(new UnrestrictedValueSet<>(true))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3), is(subsetOf(new UnrestrictedValueSet<>(true))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3), is(subsetOf(new UnrestrictedValueSet<>(false))));
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3),
                is(not(subsetOf(new UnrestrictedValueSet<>(false)))));
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3),
                is(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3),
                is(not(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3)))));
        assertThat(new OrderedValueSet<>(true, null, 1, 3),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3, 4))));
        assertThat(new OrderedValueSet<>(false, null, 1, 3),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3, 4))));
        assertThat(new OrderedValueSet<>(false, null, 1, 3),
                is(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3, 4))));
        assertThat(new OrderedValueSet<>(true, null, 1, 3),
                is(not(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3, 4)))));
        assertThat(new OrderedValueSet<>(true, null),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(false, null),
                is(subsetOf(new OrderedValueSet<>(true, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(false, null),
                is(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3))));
        assertThat(new OrderedValueSet<>(true, null),
                is(not(subsetOf(new OrderedValueSet<>(false, null, 1, 2, 3)))));
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3),
                is(not(subsetOf(new OrderedValueSet<>(true, null, "1", "2", "3")))));
        assertThat(new OrderedValueSet<>(true, null), is(subsetOf(new OrderedValueSet<>(true, null))));
        assertThat(new OrderedValueSet<>(false, null), is(subsetOf(new OrderedValueSet<>(true, null))));
        assertThat(new OrderedValueSet<>(false, null), is(subsetOf(new OrderedValueSet<>(false, null))));
        assertThat(new OrderedValueSet<>(true, null),
                is(not(subsetOf(new OrderedValueSet<>(false, null)))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3),
                is(subsetOf(IntegerRange.valueOf((Integer)null, null))));
        assertThat(new OrderedValueSet<>(false, null), is(subsetOf(IntegerRange.empty())));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3), is(subsetOf(IntegerRange.valueOf(0, 10))));
        assertThat(new OrderedValueSet<>(true, null, 1, 2, 3), is(not(subsetOf(IntegerRange.valueOf(0, 10)))));
        assertThat(new OrderedValueSet<>(false, null, 1, 2, 3),
                is(not(subsetOf(IntegerRange.valueOf(0, 10, 2)))));
        assertThat(new OrderedValueSet<>(false, null, "1", "2"),
                is(not(subsetOf((ValueSet)IntegerRange.valueOf(0, 10)))));
        assertThat(new OrderedValueSet<>(false, null, "A", "B", "C"), is(subsetOf(new StringLengthValueSet(1, false))));
        assertThat(new OrderedValueSet<>(true, null, "A", "B", "C"),
                is(not(subsetOf(new StringLengthValueSet(1, false)))));
        assertThat(new OrderedValueSet<>(true, null, "A", "B", "C"), is(subsetOf(new StringLengthValueSet(1, true))));
        assertThat(new OrderedValueSet<>(false, null, "A", "B", "C"), is(subsetOf(new StringLengthValueSet(1, true))));
        assertThat(new OrderedValueSet<>(true, null, "Aaaaa", "B", "C"),
                is(not(subsetOf(new StringLengthValueSet(1)))));
        assertThat(new OrderedValueSet<>(false, null, "A", "B", "C"),
                is(not(subsetOf(new ValueSet<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean contains(String value) {
                        return false;
                    }

                    @Override
                    public boolean isDiscrete() {
                        return false;
                    }

                    @Override
                    public boolean containsNull() {
                        return false;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean isRange() {
                        return false;
                    }

                    @Override
                    public int size() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Set<String> getValues(boolean excludeNull) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean isUnrestricted(boolean excludeNull) {
                        return false;
                    }

                    @Override
                    public boolean isSubsetOf(ValueSet<String> otherValueSet) {
                        return false;
                    }

                    @Override
                    public Optional<Class<String>> getDatatype() {
                        return Optional.of(String.class);
                    }
                }))));
    }
}
