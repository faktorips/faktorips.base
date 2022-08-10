/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.testsupport.matchers;

import java.util.function.Function;

import org.faktorips.runtime.Message;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Creates a {@link MessageMatcher} that extracts a property of type &lt;P&gt; from a
 * {@link Message} and matches it with a given matcher.
 *
 * @param <P> the property type to match
 * @since 22.6
 */
public class MessagePropertyMatcher<P> extends MessageMatcher {
    private final Function<Message, P> propertyExtractor;
    private final Matcher<P> propertyMatcher;
    private final String propertyDescription;

    @CheckForNull
    private StringDescription lastMismatchDescription = null;

    /**
     * Creates a new {@link MessageMatcher} that extracts a property of type &lt;P&gt; from a
     * {@link Message} and matches it with the given matcher.
     * 
     * @param propertyExtractor the function to get the property from the object
     * @param propertyMatcher the matcher for the property
     * @param propertyDescription the description of the object and property (e.g. "a car where the
     *            color is") that will be combined with the description of the given matcher
     */
    public MessagePropertyMatcher(Function<Message, P> propertyExtractor, Matcher<P> propertyMatcher,
            String propertyDescription) {
        this.propertyExtractor = propertyExtractor;
        this.propertyMatcher = propertyMatcher;
        this.propertyDescription = propertyDescription;
    }

    @Override
    protected boolean matchesSafely(Message item) {
        P propertyValue = propertyExtractor.apply(item);
        boolean matches = propertyMatcher.matches(propertyValue);
        if (!matches) {
            lastMismatchDescription = new StringDescription();
            lastMismatchDescription.appendText(propertyDescription);
            lastMismatchDescription.appendText(" that ");
            propertyMatcher.describeMismatch(propertyValue, lastMismatchDescription);
        } else {
            lastMismatchDescription = null;
        }
        return matches;
    }

    @Override
    protected void describeMismatchedProperty(Message message, Description mismatchDescription) {
        if (this.lastMismatchDescription != null) {
            mismatchDescription.appendText(this.lastMismatchDescription.toString());
        }
    }

    @Override
    protected void describeMessageProperty(Description description) {
        description.appendText(propertyDescription);
        description.appendText(" that is ");
        propertyMatcher.describeTo(description);
    }
}