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

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.testsupport.matchers.MessageSeverityMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Checks whether a {@link MessageList} contains a message with a certain message code.
 * 
 * @deprecated since 21.12. Use {@link MessageSeverityMatcher} instead.
 */
@Deprecated
public class MessageSevertiyMatcher extends BaseMatcher<Message> {

    private final Severity severity;

    /** @param severity the expected severity. */
    MessageSevertiyMatcher(Severity severity) {
        super();
        this.severity = severity;
    }

    @Override
    public void describeTo(Description description) {
        switch (severity) {
            case ERROR:
                description.appendText("a message with severity ERROR");
                break;
            case INFO:
                description.appendText("a message with severity INFO");
                break;
            case NONE:
                description.appendText("a message with severity NONE");
                break;
            case WARNING:
                description.appendText("a message with severity WARNING");
                break;
            default:
                description.appendText("a message with severity " + severity);
                break;
        }
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Message message)) {
            return false;
        }
        return message.getSeverity() == severity;
    }
}
