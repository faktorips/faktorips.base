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
import org.hamcrest.TypeSafeMatcher;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Checks whether a {@link MessageList} contains a {@link Message} with a certain message code.
 */
public class MessageListCodeMatcher extends TypeSafeMatcher<MessageList> {

    @CheckForNull
    private final String messageCode;
    private boolean expectMessage;

    /**
     * @param messageCode the expected message code
     */
    public MessageListCodeMatcher(@CheckForNull String messageCode) {
        this.messageCode = messageCode;
        this.expectMessage = true;
    }

    /**
     * @param messageCode the expected message code
     * @param expectMessage whether a message is expected. <code>true</code> if a message is
     *            expected, <code>false</code> if no message with the given message code is expected
     *            (negates result).
     */
    public MessageListCodeMatcher(@CheckForNull String messageCode, boolean expectMessage) {
        this.messageCode = messageCode;
        this.expectMessage = expectMessage;
    }

    @Override
    public void describeTo(Description description) {
        if (expectMessage) {
            description.appendText("a messageList containing messages with code: " + messageCode);
        } else {
            description.appendText("a messageList without message code: " + messageCode);
        }
    }

    @Override
    public boolean matchesSafely(MessageList list) {
        return list.getMessageByCode(messageCode) != null == expectMessage;
    }
}
