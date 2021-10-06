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
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link MessageList} if it contains a given number of {@link Message Messages} with a
 * given {@link ObjectProperty}.
 */
public class MessageListObjectPropertyMatcher extends TypeSafeMatcher<MessageList> {

    private final ObjectProperty objectProperty;

    private final int count;

    public MessageListObjectPropertyMatcher(ObjectProperty objectProperty) {
        this(objectProperty, 1);
    }

    public MessageListObjectPropertyMatcher(ObjectProperty objectProperty, int count) {
        this.objectProperty = objectProperty;
        this.count = count;
    }

    @Override
    public void describeTo(Description description) {
        if (count == 1) {
            description.appendText("a message for " + objectProperty);
        } else {
            description.appendText(count + " messages for " + objectProperty);
        }
    }

    @Override
    protected boolean matchesSafely(MessageList ml) {
        return ml.getMessagesFor(objectProperty.getObject(), objectProperty.getProperty()).size() == count;
    }

}
