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

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

/**
 * Similar to {@link CoreMatchers#allOf(Matcher...)}, but with better mismatch description.
 */
public class AllMatcher<T> extends BaseMatcher<T> {

    private final List<Matcher<T>> matchers;
    private Description mismatchDescription;

    public AllMatcher(List<Matcher<T>> matchers) {
        super();
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Object item) {
        mismatchDescription = new StringDescription();
        boolean allMatch = true;
        for (Matcher<T> matcher : matchers) {
            if (!matcher.matches(item)) {
                if (!mismatchDescription.toString().isEmpty()) {
                    mismatchDescription.appendText("\n AND ");
                }
                matcher.describeTo(mismatchDescription);
                mismatchDescription.appendText(" ");
                matcher.describeMismatch(item, mismatchDescription);
                if (matcher instanceof AllMatcher) {
                    mismatchDescription.appendText("\n because ");
                    mismatchDescription.appendText(((AllMatcher<?>)matcher).mismatchDescription.toString());
                }
                allMatch = false;
            }
        }
        return allMatch;
    }

    @Override
    public void describeTo(Description description) {
        boolean first = true;
        description.appendText("{");
        for (Matcher<T> matcher : matchers) {
            if (!first) {
                description.appendText("\n AND ");
            } else {
                first = false;
            }
            description.appendDescriptionOf(matcher);
        }
        description.appendText("}");
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText(mismatchDescription.toString());
    }

    public Description getMismatchDescription() {
        return mismatchDescription;
    }

    @SafeVarargs
    public static <T> AllMatcher<T> allOf(Matcher<T>... matchers) {
        return new AllMatcher<>(Arrays.asList(matchers));
    }

}
