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
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Matches a {@link Message} if it's {@link Message#getCode() message code} is the given
 * {@code messageCode}.
 */
public class MessageCodeMatcher extends TypeSafeMatcher<Message> {

    private final Matcher<String> messageCodeMatcher;
    private boolean expectMessage;

    /**
     * @param messageCode the expected message code
     */
    public MessageCodeMatcher(@CheckForNull String messageCode) {
        this(messageCode, true);
    }

    /**
     * @param messageCode the expected message code
     * @param expectMessage whether a message is expected. <code>true</code> if a message is
     *            expected, <code>false</code> if no message with the given message code is expected
     *            (negates result).
     */
    public MessageCodeMatcher(@CheckForNull String messageCode, boolean expectMessage) {
        this(CoreMatchers.equalTo(messageCode), expectMessage);
    }

    /**
     * @param messageCodeMatcher a {@link Matcher} for the message code
     */
    public MessageCodeMatcher(Matcher<String> messageCodeMatcher) {
        this(messageCodeMatcher, true);
    }

    /**
     * @param messageCodeMatcher a {@link Matcher} for the message code
     * @param expectMessage whether a message is expected. <code>true</code> if a message is
     *            expected, <code>false</code> if no message matched by the given message is
     *            expected (negates result).
     */
    public MessageCodeMatcher(Matcher<String> messageCodeMatcher, boolean expectMessage) {
        this.messageCodeMatcher = messageCodeMatcher;
        this.expectMessage = expectMessage;
    }

    @Override
    public void describeTo(Description description) {
        if (expectMessage) {
            description.appendText("a message with message code ");
        } else {
            description.appendText("a message without message code ");
        }
        messageCodeMatcher.describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(Message message, Description mismatchDescription) {
        mismatchDescription.appendText("message has code ");
        mismatchDescription.appendText(message.getCode());
    }

    @Override
    protected boolean matchesSafely(Message message) {
        return messageCodeMatcher.matches(message.getCode()) == expectMessage;
    }

}
