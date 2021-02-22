/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - www.faktorzehn.de
 * 
 * All Rights Reserved - Alle Rechte vorbehalten.
 *******************************************************************************/
package org.faktorips.abstracttest.matcher;

import java.util.function.Function;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
 * property of type &lt;P&gt; of the matched object of type &lt;T&gt;.
 * 
 * @param <T> the type of the matched object
 * @param <P> the type of the object's property matched by the wrapped matcher
 */
public class PropertyMatcher<T, P> extends BaseMatcher<T> {

    private Function<T, P> propertyGetter;
    private Matcher<P> propertyMatcher;
    private String propertyDescription;

    public PropertyMatcher(Function<T, P> propertyGetter, String propertyDescription,
            Matcher<P> propertyMatcher) {
        this.propertyGetter = propertyGetter;
        this.propertyDescription = propertyDescription;
        this.propertyMatcher = propertyMatcher;
    }

    @Override
    public boolean matches(Object item) {
        P property = getNullSafe(item);
        return propertyMatcher.matches(property);
    }

    @SuppressWarnings("unchecked")
    private P getNullSafe(Object item) {
        return item == null ? null : propertyGetter.apply((T)item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(propertyDescription + ' ');
        propertyMatcher.describeTo(description);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        P property = getNullSafe(item);
        super.describeMismatch(property, description);
        if (propertyMatcher instanceof AllMatcher) {
            description.appendText("\n because ");
            description.appendText(((AllMatcher<?>)propertyMatcher).getMismatchDescription().toString());
        }
    }

}