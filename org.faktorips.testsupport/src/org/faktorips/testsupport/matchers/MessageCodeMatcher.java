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

    public MessageCodeMatcher(@CheckForNull String messageCode) {
        this.messageCode = messageCode;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message with message code " + messageCode);
    }

    @Override
    protected boolean matchesSafely(Message m) {
        return Objects.equals(messageCode, m.getCode());
    }

}
