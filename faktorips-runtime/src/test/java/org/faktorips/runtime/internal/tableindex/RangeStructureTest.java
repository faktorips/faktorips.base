/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.values.Decimal;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

public class RangeStructureTest {
    private RangeStructure<Integer, ResultStructure<String>, String> structure;

    @Test(expected = NullPointerException.class)
    public void testGet_null() {
        structure.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor() {
        createStructure(null);
    }

    @Test
    public void testGet_KeyTypeLowerBoundEqual() {
        createStructure(RangeType.LOWER_BOUND_EQUAL);
        resultSetForKeyWayOutOfLowerBound(isEmpty());
        resultSetForKeyLessThanSmallestLowerBound(isEmpty());
        resultSetForKeyExactSmallestLowerBound(hasItem("A"));
        resultSetForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        resultSetForKeyInRange1(hasItem("A"));
        resultSetForKeyInRange2(hasItem("B"));
        resultSetForKeyLessThanGreatestUpperBound(hasItem("B"));
        resultSetForKeyExactGreatestUpperBound(hasItem("C"));
        resultSetForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        resultSetForKeyWayOutOfUpperBound(hasItem("C"));
        resultSetForKeyNull(isEmpty());
        resultSetForKeyNullObject(isEmpty(), RangeType.LOWER_BOUND_EQUAL);
    }

    @SuppressWarnings({ "deprecation", "javadoc" })
    /**
     * Tests the {@link RangeStructure} with {@link RangeType#LOWER_BOUND}. May be deleted when
     * {@link RangeType#LOWER_BOUND} is removed.
     */
    @Test
    public void testGet_KeyTypeLowerBound() {
        createStructure(RangeType.LOWER_BOUND);
        resultSetForKeyWayOutOfLowerBound(isEmpty());
        resultSetForKeyLessThanSmallestLowerBound(isEmpty());
        resultSetForKeyExactSmallestLowerBound(isEmpty());
        resultSetForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        resultSetForKeyInRange1(hasItem("A"));
        resultSetForKeyInRange2(hasItem("B"));
        resultSetForKeyLessThanGreatestUpperBound(hasItem("B"));
        resultSetForKeyExactGreatestUpperBound(hasItem("B"));
        resultSetForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        resultSetForKeyWayOutOfUpperBound(hasItem("C"));
        resultSetForKeyNull(isEmpty());
        resultSetForKeyNullObject(isEmpty(), RangeType.LOWER_BOUND);
    }

    @Test
    public void testGet_KeyTypeUpperBoundEqual() {
        createStructure(RangeType.UPPER_BOUND_EQUAL);
        resultSetForKeyWayOutOfLowerBound(hasItem("A"));
        resultSetForKeyLessThanSmallestLowerBound(hasItem("A"));
        resultSetForKeyExactSmallestLowerBound(hasItem("A"));
        resultSetForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        resultSetForKeyInRange1(hasItem("B"));
        resultSetForKeyInRange2(hasItem("C"));
        resultSetForKeyLessThanGreatestUpperBound(hasItem("C"));
        resultSetForKeyExactGreatestUpperBound(hasItem("C"));
        resultSetForKeyGreaterThanGreatestUpperBound(isEmpty());
        resultSetForKeyWayOutOfUpperBound(isEmpty());
        resultSetForKeyNull(isEmpty());
        resultSetForKeyNullObject(isEmpty(), RangeType.UPPER_BOUND_EQUAL);
    }

    @SuppressWarnings({ "deprecation", "javadoc" })
    /**
     * Tests the {@link RangeStructure} with {@link RangeType#UPPER_BOUND}. May be deleted when
     * {@link RangeType#UPPER_BOUND} is removed.
     */
    @Test
    public void testGet_KeyTypeUpperBound() {
        createStructure(RangeType.UPPER_BOUND);
        resultSetForKeyWayOutOfLowerBound(hasItem("A"));
        resultSetForKeyLessThanSmallestLowerBound(hasItem("A"));
        resultSetForKeyExactSmallestLowerBound(hasItem("B"));
        resultSetForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        resultSetForKeyInRange1(hasItem("B"));
        resultSetForKeyInRange2(hasItem("C"));
        resultSetForKeyLessThanGreatestUpperBound(hasItem("C"));
        resultSetForKeyExactGreatestUpperBound(isEmpty());
        resultSetForKeyGreaterThanGreatestUpperBound(isEmpty());
        resultSetForKeyWayOutOfUpperBound(isEmpty());
        resultSetForKeyNull(isEmpty());
        resultSetForKeyNullObject(isEmpty(), RangeType.UPPER_BOUND);
    }

    private void createStructure(RangeType keyType) {
        structure = RangeStructure.create(keyType);
        structure.put(-5, new ResultStructure<>("A"));
        structure.put(2, new ResultStructure<>("B"));
        structure.put(10, new ResultStructure<>("C"));
    }

    public void resultSetForKeyWayOutOfLowerBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(-100).get(), matcher);
    }

    public void resultSetForKeyLessThanSmallestLowerBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(-6).get(), matcher);
    }

    public void resultSetForKeyExactSmallestLowerBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(-5).get(), matcher);
    }

    public void resultSetForKeyGreaterThanSmallestLowerBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(-4).get(), matcher);
    }

    public void resultSetForKeyInRange1(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(0).get(), matcher);
    }

    public void resultSetForKeyInRange2(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(5).get(), matcher);
    }

    public void resultSetForKeyLessThanGreatestUpperBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(9).get(), matcher);
    }

    public void resultSetForKeyExactGreatestUpperBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(10).get(), matcher);
    }

    public void resultSetForKeyGreaterThanGreatestUpperBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(11).get(), matcher);
    }

    public void resultSetForKeyWayOutOfUpperBound(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(100).get(), matcher);
    }

    public void resultSetForKeyNull(Matcher<Iterable<? super String>> matcher) {
        assertThat(structure.get(null).get(), matcher);
    }

    public void resultSetForKeyNullObject(Matcher<Iterable<? super String>> matcher, RangeType keyType) {
        RangeStructure<Decimal, ResultStructure<String>, String> decimalStructure = RangeStructure.createWith(keyType,
                Decimal.ZERO, new ResultStructure<>("Foo"));
        assertThat(decimalStructure.get(Decimal.NULL).get(), matcher);
    }

    private static Matcher<Iterable<? super String>> isEmpty() {
        return new IsEmpty<>();
    }

    private static class IsEmpty<T> extends BaseMatcher<Iterable<? super T>> {

        @Override
        public boolean matches(Object item) {
            if (item instanceof Set) {
                Iterable<?> iterable = (Iterable<?>)item;
                return !iterable.iterator().hasNext();
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("an empty Iterable");
        }
    }

    @Test
    public void testIsEmptyMatcher() {
        Set<String> set = new HashSet<>();
        IsEmpty<String> matcher = new IsEmpty<>();
        assertTrue(matcher.matches(set));

        set.add("String");
        assertFalse(matcher.matches(set));
    }

    @Test
    public void testDescribeEmptyMatcher() {
        IsEmpty<String> matcher = new IsEmpty<>();
        Description description = mock(Description.class);
        matcher.describeTo(description);
        verify(description).appendText("an empty Iterable");
    }

    /**
     * FIPS-2595. This tests checks the correctness of the comparable generics. GregorianCalendar
     * extends {@code Comparable<Calendar>} hence we need {@code K extends Comparable<? super
     * K>} instead of simply {@code K extends Comparable<K>}.
     */
    public void testInstantiateSuperComparable() {
        RangeStructure<GregorianCalendar, ResultStructure<Object>, Object> structure = RangeStructure
                .create(RangeType.LOWER_BOUND_EQUAL);

        // The assert is not really necessary, the real test is that the statement above compiles.
        assertTrue(structure.get().isEmpty());
    }

    @Test
    public void testCopy_DeepCopyOfMapContent() {
        RangeStructure<Integer, ResultStructure<String>, String> structure = RangeStructure
                .create(RangeType.LOWER_BOUND_EQUAL);
        structure.put(1, new ResultStructure<>("ONE"));
        structure.put(2, new ResultStructure<>("TWO"));

        RangeStructure<Integer, ResultStructure<String>, String> copiedStructure = structure.copy();

        assertEquals(copiedStructure.getMap().get(1), structure.getMap().get(1));
        assertNotSame(copiedStructure.getMap().get(1), structure.getMap().get(1));
        assertEquals(copiedStructure.getMap().get(2), structure.getMap().get(2));
        assertNotSame(copiedStructure.getMap().get(2), structure.getMap().get(2));
    }

    @Test
    public void testCopy_CopyOfObject() {
        RangeStructure<Integer, ResultStructure<String>, String> structure = RangeStructure
                .create(RangeType.LOWER_BOUND_EQUAL);
        structure.put(1, new ResultStructure<>("ONE"));

        RangeStructure<Integer, ResultStructure<String>, String> copiedStructure = structure.copy();

        assertEquals(structure.getMap(), copiedStructure.getMap());
        assertNotSame(copiedStructure, structure);
    }
}
