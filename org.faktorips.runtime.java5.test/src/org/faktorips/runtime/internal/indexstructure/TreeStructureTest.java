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

package org.faktorips.runtime.internal.indexstructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

public class TreeStructureTest {
    private TreeStructure<Integer, ResultStructure<String>, String> structure;

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
        createStructure(KeyType.KEY_IS_LOWER_BOUND_EQUAL);
        setForKeyWayOutOfLowerBound(isEmpty());
        setForKeyLessThanSmallestLowerBound(isEmpty());
        setForKeyExactSmallestLowerBound(hasItem("A"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        setForKeyInRange1(hasItem("A"));
        setForKeyInRange2(hasItem("B"));
        setForKeyLessThanGreatestUpperBound(hasItem("B"));
        setForKeyExactGreatestUpperBound(hasItem("C"));
        setForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        setForKeyWayOutOfUpperBound(hasItem("C"));
    }

    @Test
    public void testGet_KeyTypeLowerBound() {
        createStructure(KeyType.KEY_IS_LOWER_BOUND);
        setForKeyWayOutOfLowerBound(isEmpty());
        setForKeyLessThanSmallestLowerBound(isEmpty());
        setForKeyExactSmallestLowerBound(isEmpty());
        setForKeyGreaterThanSmallestLowerBound(hasItem("A"));
        setForKeyInRange1(hasItem("A"));
        setForKeyInRange2(hasItem("B"));
        setForKeyLessThanGreatestUpperBound(hasItem("B"));
        setForKeyExactGreatestUpperBound(hasItem("B"));
        setForKeyGreaterThanGreatestUpperBound(hasItem("C"));
        setForKeyWayOutOfUpperBound(hasItem("C"));
    }

    @Test
    public void testGet_KeyTypeUpperBoundEqual() {
        createStructure(KeyType.KEY_IS_UPPER_BOUND_EQUAL);
        setForKeyWayOutOfLowerBound(hasItem("A"));
        setForKeyLessThanSmallestLowerBound(hasItem("A"));
        setForKeyExactSmallestLowerBound(hasItem("A"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        setForKeyInRange1(hasItem("B"));
        setForKeyInRange2(hasItem("C"));
        setForKeyLessThanGreatestUpperBound(hasItem("C"));
        setForKeyExactGreatestUpperBound(hasItem("C"));
        setForKeyGreaterThanGreatestUpperBound(isEmpty());
        setForKeyWayOutOfUpperBound(isEmpty());
    }

    @Test
    public void testGet_KeyTypeUpperBound() {
        createStructure(KeyType.KEY_IS_UPPER_BOUND);
        setForKeyWayOutOfLowerBound(hasItem("A"));
        setForKeyLessThanSmallestLowerBound(hasItem("A"));
        setForKeyExactSmallestLowerBound(hasItem("B"));
        setForKeyGreaterThanSmallestLowerBound(hasItem("B"));
        setForKeyInRange1(hasItem("B"));
        setForKeyInRange2(hasItem("C"));
        setForKeyLessThanGreatestUpperBound(hasItem("C"));
        setForKeyExactGreatestUpperBound(isEmpty());
        setForKeyGreaterThanGreatestUpperBound(isEmpty());
        setForKeyWayOutOfUpperBound(isEmpty());
    }

    private void createStructure(KeyType keyType) {
        structure = TreeStructure.create(keyType);
        structure.put(-5, new ResultStructure<String>("A"));
        structure.put(2, new ResultStructure<String>("B"));
        structure.put(10, new ResultStructure<String>("C"));
    }

    public void setForKeyWayOutOfLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-100).get(), matcher);
    }

    public void setForKeyLessThanSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-6).get(), matcher);
    }

    public void setForKeyExactSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-5).get(), matcher);
    }

    public void setForKeyGreaterThanSmallestLowerBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(-4).get(), matcher);
    }

    public void setForKeyInRange1(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(0).get(), matcher);
    }

    public void setForKeyInRange2(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(5).get(), matcher);
    }

    public void setForKeyLessThanGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(9).get(), matcher);
    }

    public void setForKeyExactGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(10).get(), matcher);
    }

    public void setForKeyGreaterThanGreatestUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(11).get(), matcher);
    }

    public void setForKeyWayOutOfUpperBound(Matcher<Iterable<String>> matcher) {
        assertThat(structure.get(100).get(), matcher);
    }

    private static Matcher<Iterable<String>> isEmpty() {
        return new IsEmpty<String>();
    }

    private static class IsEmpty<T> extends BaseMatcher<Iterable<T>> {

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
        Set<String> set = new HashSet<String>();
        IsEmpty<String> matcher = new IsEmpty<String>();
        assertTrue(matcher.matches(set));

        set.add("String");
        assertFalse(matcher.matches(set));
    }

    @Test
    public void testDescribeEmptyMatcher() {
        IsEmpty<String> matcher = new IsEmpty<String>();
        Description description = mock(Description.class);
        matcher.describeTo(description);
        verify(description).appendText("an empty Iterable");
    }
}
