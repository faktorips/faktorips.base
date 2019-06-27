/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.abstracttest.matcher;

import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Checks whether a {@link MessageList} contains a message with a certain message code.
 */
public class MessageSevertiyMatcher extends BaseMatcher<Message> {

    private final int severity;

    /** @param severity the expected severity. */
    MessageSevertiyMatcher(int severity) {
        super();
        this.severity = severity;
    }

    @Override
    public void describeTo(Description description) {
        switch (severity) {
            case Message.ERROR:
                description.appendText("a message with severity ERROR");
                break;
            case Message.INFO:
                description.appendText("a message with severity INFO");
                break;
            case Message.NONE:
                description.appendText("a message with severity NONE");
                break;
            case Message.WARNING:
                description.appendText("a message with severity WARNING");
                break;
            default:
                description.appendText("a message with severity " + severity);
                break;
        }
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Message)) {
            return false;
        }
        Message message = (Message)item;
        return message.getSeverity() == severity;
    }
}