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

import org.faktorips.runtime.MessageList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link MessageList}'s {@link MessageList#size() size}.
 */
public class MessageListSizeMatcher extends TypeSafeMatcher<MessageList> {

    private Matcher<Integer> intMatcher;

    public MessageListSizeMatcher(Matcher<Integer> intMatcher) {
        this.intMatcher = intMatcher;
    }

    @Override
    public void describeTo(Description description) {
        intMatcher.describeTo(description);
        description.appendText(" messages");
    }

    @Override
    protected boolean matchesSafely(MessageList ml) {
        return intMatcher.matches(ml.size());
    }

}
