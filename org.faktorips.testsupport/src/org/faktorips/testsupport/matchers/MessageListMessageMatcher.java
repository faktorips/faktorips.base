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
import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link MessageList} if one of it's {@link Message Messages} is matched by the given
 * {@link Matcher Matcher&lt;Message&gt;}.
 */
public class MessageListMessageMatcher extends TypeSafeMatcher<MessageList> {

    private final Matcher<Message> messageMatcher;

    public MessageListMessageMatcher(Matcher<Message> messageMatcher) {
        this.messageMatcher = messageMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message list containing ");
        messageMatcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(MessageList ml) {
        for (Message m : ml) {
            if (messageMatcher.matches(m)) {
                return true;
            }
        }
        return false;
    }

}
