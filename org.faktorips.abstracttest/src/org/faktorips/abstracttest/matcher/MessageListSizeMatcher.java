/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.abstracttest.matcher;

import org.faktorips.util.message.MessageList;
import org.hamcrest.Description;

/**
 * Checks whether a {@link MessageList} is empty.
 */
public class MessageListSizeMatcher extends AbstractMessageListMatcher {

    private final int size;

    public MessageListSizeMatcher(int size) {
        super();
        this.size = size;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message list with size " + size);
    }

    @Override
    boolean matchesSafely(MessageList list) {
        return list.size() == size;
    }

}
