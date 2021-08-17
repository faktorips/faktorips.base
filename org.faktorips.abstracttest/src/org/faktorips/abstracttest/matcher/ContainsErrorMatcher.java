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
 * Checks whether a {@link MessageList} contains an error message.
 */
public class ContainsErrorMatcher extends TypeSafeMatcher<MessageList> {
    @Override
    public void describeTo(Description description) {
        description.appendText("contain an error message");
    }

    @Override
    protected boolean matchesSafely(MessageList messageList) {
        return messageList.containsErrorMsg();
    }
}