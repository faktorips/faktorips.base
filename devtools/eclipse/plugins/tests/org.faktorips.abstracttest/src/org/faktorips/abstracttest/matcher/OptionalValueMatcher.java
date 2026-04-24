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

import java.util.Objects;
import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/** Matches if an {@link Optional Optional's} value is equal to a given value. */
public class OptionalValueMatcher<T> extends TypeSafeMatcher<Optional<? extends T>> {

    private T expectedValue;

    public OptionalValueMatcher(T expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedValue);
    }

    @Override
    protected boolean matchesSafely(Optional<? extends T> item) {
        return Optional.ofNullable(item)
                .stream()
                .flatMap(Optional::stream)
                .anyMatch(t -> Objects.equals(t, expectedValue));
    }

}