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
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link MessageList} if it {@link MessageList#isEmpty() is empty}.
 */
public class EmptyMessageListMatcher extends TypeSafeMatcher<MessageList> {

    @Override
    public void describeTo(Description description) {
        description.appendText("an empty message list");

    }

    @Override
    protected boolean matchesSafely(MessageList ml) {
        return ml.isEmpty();
    }

}
