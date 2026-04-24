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

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/** Matches if an {@link Optional} is present/absent. */
public class OptionalPresentMatcher<T> extends TypeSafeMatcher<Optional<? extends T>> {

    private final boolean expectedPresent;

    OptionalPresentMatcher(boolean expected) {
        expectedPresent = expected;
    }

    @Override
    public void describeTo(Description description) {
        if (expectedPresent) {
            description.appendText("<Present>");
        } else {
            description.appendText("<Absent>");
        }
    }

    @Override
    protected boolean matchesSafely(Optional<? extends T> item) {
        return item.isPresent() == expectedPresent;
    }

}
