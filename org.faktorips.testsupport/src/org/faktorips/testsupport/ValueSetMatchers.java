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

public class ValueSetMatchers {

    private ValueSetMatchers() {
        // util class
    }

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

    public static Matcher<ValueSet<?>> isRange(String lower, String upper) {
        return new TypeSafeMatcher<ValueSet<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a range value set from " + lower + " to " + upper);
            }

            @Override
            protected boolean matchesSafely(ValueSet<?> valueSet) {
                return valueSet.isRange()
                        && Objects.equals(((Range<?>)valueSet).getLowerBound(), lower)
                        && Objects.equals(((Range<?>)valueSet).getUpperBound(), upper);
            }
        };
    }

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
