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
import org.faktorips.runtime.Severity;
import org.hamcrest.Description;

/**
 * Matches a {@link Message} if it's {@link Message#getSeverity() severity} is the given
 * {@link Severity}.
 */
public class MessageSeverityMatcher extends MessageMatcher {

    private final Severity severity;

    public MessageSeverityMatcher(Severity severity) {
        this.severity = severity;
    }

    @Override
    protected void describeMessageProperty(Description description) {
        description.appendText("has severity " + severity);
    }

    @Override
    protected void describeMismatchedProperty(Message message, Description mismatchDescription) {
        mismatchDescription.appendText(" has severity ");
        mismatchDescription.appendValue(message.getSeverity());
    }

    @Override
    protected boolean matchesSafely(Message m) {
        return severity == m.getSeverity();
    }

}
