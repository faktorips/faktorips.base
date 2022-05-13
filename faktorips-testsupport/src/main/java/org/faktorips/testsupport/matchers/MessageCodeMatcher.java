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

import java.util.Objects;

import org.faktorips.runtime.Message;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Matches a {@link Message} if it's {@link Message#getCode() message code} is the given
 * {@code messageCode}.
 */
public class MessageCodeMatcher extends TypeSafeMatcher<Message> {

    @CheckForNull
    private final String messageCode;
    private boolean expectMessage;

    /**
     * @param messageCode the expected message code
     */
    public MessageCodeMatcher(@CheckForNull String messageCode) {
        this.messageCode = messageCode;
        this.expectMessage = true;
    }

    /**
     * @param messageCode the expected message code
     * @param expectMessage whether a message is expected. <code>true</code> if a message is
     *            expected, <code>false</code> if no message with the given message code is expected
     *            (negates result).
     */
    public MessageCodeMatcher(@CheckForNull String messageCode, boolean expectMessage) {
        this.messageCode = messageCode;
        this.expectMessage = expectMessage;
    }

    @Override
    public void describeTo(Description description) {
        if (expectMessage) {
            description.appendText("a message with message code " + messageCode);
        } else {
            description.appendText("a message without message code: " + messageCode);
        }
    }

    @Override
    protected boolean matchesSafely(Message m) {
        return Objects.equals(messageCode, m.getCode()) == expectMessage;
    }

}
