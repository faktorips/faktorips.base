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
 * Checks whether a {@link MessageList} is empty.
 * 
 * @deprecated since 21.12. Use {@link org.faktorips.testsupport.matchers.MessageListSizeMatcher}
 *                 instead.
 */
@Deprecated
public class MessageListSizeMatcher extends TypeSafeMatcher<MessageList> {

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
    protected boolean matchesSafely(MessageList list) {
        return list.size() == size;
    }

}
