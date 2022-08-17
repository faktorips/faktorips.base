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

import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Checks whether a {@link MessageList} contains a message with a certain message code.
 * 
 * @deprecated since 21.12. Use {@link org.faktorips.testsupport.matchers.MessageListCodeMatcher}
 *                 instead.
 */
@Deprecated
public class MessageCodeMatcher extends TypeSafeMatcher<MessageList> {
    private final String msgCode;
    private boolean expectMessage;

    /**
     * @param msgCode the expected message code
     * @param expectMessage whether a message is expected. <code>true</code> if a message is
     *            expected, <code>false</code> if no message with the given message code is expected
     *            (negates result).
     */
    MessageCodeMatcher(String msgCode, boolean expectMessage) {
        this.msgCode = msgCode;
        this.expectMessage = expectMessage;
    }

    @Override
    public void describeTo(Description description) {
        if (expectMessage) {
            description.appendText("a messageList containing messages with code: " + msgCode);
        } else {
            description.appendText("a messageList without message code: " + msgCode);
        }
    }

    @Override
    public boolean matchesSafely(MessageList list) {
        return list.getMessageByCode(msgCode) != null == expectMessage;
    }
}
