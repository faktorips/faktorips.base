/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testsupport;

import java.util.Arrays;
import java.util.Objects;

import org.faktorips.valueset.Range;
import org.faktorips.valueset.ValueSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest {@link Matcher Matchers} for use in JUnit tests of Faktor-IPS {@link ValueSet value
 * sets}.
 */
public class ValueSetMatchers {

    private ValueSetMatchers() {
        // util class
    }

    /**
     * Creates a {@link Matcher} that matches a {@link ValueSet} if it {@link ValueSet#isEmpty() is
     * empty}.
     */
    public static Matcher<ValueSet<?>> empty() {
        return new TypeSafeMatcher<ValueSet<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty value set");
            }

            @Override
            protected boolean matchesSafely(ValueSet<?> valueSet) {
                return valueSet.isEmpty();
            }
        };
    }

    /**
     * Creates a {@link Matcher} that matches a {@link ValueSet} if it
     * {@link ValueSet#containsNull() contains null}.
     */
    public static Matcher<ValueSet<?>> containsNull() {
        return new TypeSafeMatcher<ValueSet<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a value set containing null");
            }

            @Override
            protected boolean matchesSafely(ValueSet<?> valueSet) {
                return valueSet.containsNull();
            }
        };
    }

    /**
     * Creates a {@link Matcher} that matches a {@link ValueSet} if it {@link ValueSet#isRange() is
     * a range} with the given lower and upper bound.
     *
     * @param <T> the type of the range's bounds
     * @param lower the expected lower bound
     * @param upper the expected upper bound
     */
    public static <T extends Comparable<T>> Matcher<ValueSet<T>> isRange(T lower, T upper) {
        return new TypeSafeMatcher<ValueSet<T>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a range value set from " + lower + " to " + upper);
            }

            @Override
            protected boolean matchesSafely(ValueSet<T> valueSet) {
                return valueSet.isRange()
                        && Objects.equals(((Range<T>)valueSet).getLowerBound(), lower)
                        && Objects.equals(((Range<T>)valueSet).getUpperBound(), upper);
            }
        };
    }

    /**
     * Creates a {@link Matcher} that matches a {@link ValueSet} if it {@link ValueSet#contains
     * contains} the given values.
     * 
     * @param <T> the object type to match
     * @param values the expected values
     */
    @SuppressWarnings("unchecked")
    public static <T> Matcher<ValueSet<T>> contains(T... values) {
        return new TypeSafeMatcher<ValueSet<T>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a value set containing " + Arrays.toString(values));
            }

            @Override
            protected boolean matchesSafely(ValueSet<T> valueSet) {
                return Arrays.stream(values).allMatch(v -> {
                    return valueSet.contains(v);
                });
            }
        };
    }
}
