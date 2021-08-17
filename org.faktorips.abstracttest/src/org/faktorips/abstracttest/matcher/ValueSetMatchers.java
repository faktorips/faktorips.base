/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.matcher;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ValueSetMatchers {

    private ValueSetMatchers() {
        // util class
    }

    public static Matcher<IValueSet> empty() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("an empty value set");
            }

            @Override
            protected boolean matchesSafely(IValueSet valueSet) {
                return valueSet.isEmpty();
            }
        };
    }

    public static Matcher<IValueSet> containsNull() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a value set containing null");
            }

            @Override
            protected boolean matchesSafely(IValueSet valueSet) {
                return valueSet.isContainsNull();
            }
        };
    }

    public static Matcher<IValueSet> isRange(String lower, String upper) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a range value set from " + lower + " to " + upper);
            }

            @Override
            protected boolean matchesSafely(IValueSet valueSet) {
                return valueSet.isRange()
                        && Objects.equals(((IRangeValueSet)valueSet).getLowerBound(), lower)
                        && Objects.equals(((IRangeValueSet)valueSet).getUpperBound(), upper);
            }
        };
    }

    public static Matcher<IValueSet> contains(String... values) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("a value set containing " + String.join(", ", values));
            }

            @Override
            protected boolean matchesSafely(IValueSet valueSet) {
                return Arrays.stream(values).allMatch(v -> {
                    try {
                        return valueSet.containsValue(v, valueSet.getIpsProject());
                    } catch (CoreException e) {
                        fail(e.getMessage());
                        return false;
                    }
                });
            }
        };
    }
}
