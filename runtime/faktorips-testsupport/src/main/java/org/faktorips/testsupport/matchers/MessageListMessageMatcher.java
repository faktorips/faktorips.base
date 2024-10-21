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

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link MessageList} if one of it's {@link Message Messages} is matched by the given
 * {@link Matcher Matcher&lt;Message&gt;}.
 */
public class MessageListMessageMatcher extends TypeSafeMatcher<MessageList> {

    private final Matcher<Message>[] messageMatchers;
    private Description mismatchDescription;

    @SafeVarargs
    public MessageListMessageMatcher(Matcher<Message>... messageMatchers) {
        this.messageMatchers = messageMatchers;
        mismatchDescription = new StringDescription();
    }

    @Override
    public void describeTo(Description description) {
        boolean first = true;
        description.appendText("a " + MessageList.class.getSimpleName() + " containing a message that ");
        for (Matcher<Message> matcher : messageMatchers) {
            if (!first) {
                description.appendText("\n AND ");
            } else {
                first = false;
            }
            if (matcher instanceof MessageMatcher messageMatcher) {
                messageMatcher.describeMessageProperty(description);
            } else {
                matcher.describeTo(description);
            }
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
            allMatch = false;
            break;
        }
        if (!allMatch) {
            mismatchDescription.appendText("the " + MessageList.class.getSimpleName());
            switch (messageList.size()) {
                case 0:
                    mismatchDescription.appendText(" contained no message");
                    break;
                case 1:
                    mismatchDescription.appendText(" only contained the message ");
                    mismatchDescription.appendValue(messageList);
                    break;
                default:
                    mismatchDescription.appendText(" only contained the messages ");
                    mismatchDescription.appendValue(messageList);
            }
        }
        return allMatch;
    }

    @Override
    protected void describeMismatchSafely(MessageList item, Description mismatchDescription) {
        mismatchDescription.appendText(this.mismatchDescription.toString());
    }

}
