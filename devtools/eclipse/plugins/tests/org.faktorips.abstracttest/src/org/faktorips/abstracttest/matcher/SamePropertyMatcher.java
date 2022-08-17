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
import java.util.function.Function;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
 * property of type &lt;P&gt; of the matched object of type &lt;T&gt; and referenced object of that
 * same type.
 *
 * @param <T> the type of the compared objects
 * @param <P> the type of the property
 */
public class SamePropertyMatcher<T, P> extends BaseMatcher<T> {

    private final Function<T, P> propertyGetter;
    private final String propertyDescription;
    private final T objectToMatch;

    public SamePropertyMatcher(Function<T, P> propertyGetter,
            String propertyDescription,
            T objectToMatch) {
        this.propertyGetter = propertyGetter;
        this.propertyDescription = propertyDescription;
        this.objectToMatch = objectToMatch;
    }

    @Override
    public boolean matches(Object item) {
        return Objects.equals(getNullSafe(item), getNullSafe(getObjectToMatch()));
    }

    @SuppressWarnings("unchecked")
    protected P getNullSafe(Object item) {
        return item == null ? null : propertyGetter.apply((T)item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("has same " + propertyDescription + "(");
        description.appendValue(getNullSafe(getObjectToMatch()));
        description.appendText(")");
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        super.describeMismatch(getNullSafe(item), description);
    }

    protected T getObjectToMatch() {
        return objectToMatch;
    }

    public static <T, P> Matcher<T> same(Function<T, P> propertyGetter,
            String propertyDescription,
            T objectToMatch) {
        return new SamePropertyMatcher<>(propertyGetter, propertyDescription, objectToMatch);
    }
}
