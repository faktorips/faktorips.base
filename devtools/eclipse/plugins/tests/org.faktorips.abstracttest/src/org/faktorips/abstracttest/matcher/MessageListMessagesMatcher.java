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

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.testsupport.matchers.MessageListMessageMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

/**
 * A {@link Matcher} that matches if, for every given {@link Matcher}, the checked
 * {@link MessageList} contains a {@link Message} that is matched by that {@link Matcher}. This must
 * be a different {@link Message} for every {@link Matcher Matchers}. The {@link MessageList} may
 * contain additional {@link Message Messages} not matched by any {@link Matcher}. The order of the
 * {@link Message Messages} and {@link Matcher Matchers} is irrelevant.
 * 
 * @deprecated since 21.12. Use {@link MessageListMessageMatcher} instead.
 */
@Deprecated
final class MessageListMessagesMatcher extends TypeSafeMatcher<MessageList> {
    private final Matcher<Message>[] messageMatchers;
    private Description mismatchDescription;

    @SafeVarargs
    MessageListMessagesMatcher(Matcher<Message>... messageMatchers) {
        this.messageMatchers = messageMatchers;
    }

    @Override
    public void describeTo(Description description) {
        boolean first = true;
        description.appendText("a " + MessageList.class.getSimpleName() + " containing ");
        for (Matcher<Message> matcher : messageMatchers) {
            if (!first) {
                description.appendText("\n AND ");
            } else {
                first = false;
            }
            description.appendDescriptionOf(matcher);
        }
    }

    @Override
    protected boolean matchesSafely(MessageList messageList) {
        boolean allMatch = true;
        Set<Message> messages = new LinkedHashSet<>(messageList.getMessages());
        mismatchDescription = new StringDescription();
        matchers: for (Matcher<Message> messageMatcher : messageMatchers) {
            for (Message message : messages) {
                if (messageMatcher.matches(message)) {
                    messages.remove(message);
                    continue matchers;
                }
            }
            if (!mismatchDescription.toString().isEmpty()) {
                mismatchDescription.appendText("\n AND ");
            }
            messageMatcher.describeTo(mismatchDescription);
            allMatch = false;
        }
        return allMatch;
    }

    @Override
    protected void describeMismatchSafely(MessageList item, Description mismatchDescription) {
        mismatchDescription.appendText(this.mismatchDescription.toString());
    }
}
