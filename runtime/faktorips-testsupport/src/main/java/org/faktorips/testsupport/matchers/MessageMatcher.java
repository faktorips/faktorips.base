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

import org.faktorips.runtime.Message;
import org.faktorips.testsupport.IpsMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A typesafe {@link Matcher} for a {@link Message}.
 *
 * @implSpec the methods {@link #describeMessageProperty(Description)} and
 *               {@link #describeMismatchedProperty(Message, Description)} should only describe the
 *               matched property; they will be combined with a leading {@value #A_MESSAGE_THAT}
 *               when this matcher is used as is and embedded in more descriptive messages when used
 *               with {@link #and(MessageMatcher)} or a {@link MessageListMessageMatcher} as created
 *               by {@link IpsMatchers#hasMessages(Matcher...)}.
 * @since 22.6
 */
public abstract class MessageMatcher extends TypeSafeMatcher<Message> {

    private static final String A_MESSAGE_THAT = "a message that ";

    /**
     * {@inheritDoc}
     * 
     * @implNote uses {@link #describeMessageProperty(Description)} to describe the property the
     *               message should have
     */
    @Override
    public void describeTo(Description description) {
        description.appendText(A_MESSAGE_THAT);
        describeMessageProperty(description);
    }

    /**
     * Adds the description of the property checked by this matcher to the given description.
     */
    protected abstract void describeMessageProperty(Description description);

    /**
     * {@inheritDoc}
     * 
     * @implNote uses {@link #describeMismatchedProperty(Message, Description)} to describe the
     *               property the actually has
     */
    @Override
    protected void describeMismatchSafely(Message message, Description mismatchDescription) {
        mismatchDescription.appendText("the message ");
        describeMismatchedProperty(message, mismatchDescription);
    }

    /**
     * Adds the description of the given message's actual value of the property checked by this
     * matcher to the given description.
     */
    protected abstract void describeMismatchedProperty(Message message, Description mismatchDescription);

    /**
     * Combines this {@link MessageMatcher} with another {@link MessageMatcher} creating a
     * {@link MessageMatcher} that only matches if both original matchers match.
     */
    public MessageMatcher and(MessageMatcher other) {
        return new CombinedMessageMatcher(this, other);
    }

    private static final class CombinedMessageMatcher extends MessageMatcher {
        private final MessageMatcher first;
        private final MessageMatcher second;

        CombinedMessageMatcher(MessageMatcher first, MessageMatcher second) {
            this.first = first;
            this.second = second;
        }

        @Override
        protected boolean matchesSafely(Message item) {
            return first.matchesSafely(item) && second.matchesSafely(item);
        }

        @Override
        protected void describeMismatchedProperty(Message message, Description mismatchDescription) {
            first.describeMismatchedProperty(message, mismatchDescription);
            mismatchDescription.appendText(" AND ");
            second.describeMismatchedProperty(message, mismatchDescription);
        }

        @Override
        protected void describeMessageProperty(Description description) {
            first.describeMessageProperty(description);
            description.appendText(" AND ");
            second.describeMessageProperty(description);
        }
    }

}
