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
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link Message} if it's {@link Message#getSeverity() severity} is the given
 * {@link Severity}.
 */
public class MessageSeverityMatcher extends TypeSafeMatcher<Message> {

    private final Severity severity;

    public MessageSeverityMatcher(Severity severity) {
        this.severity = severity;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message with severity " + severity);
    }

    @Override
    protected boolean matchesSafely(Message m) {
        return severity == m.getSeverity();
    }

}
