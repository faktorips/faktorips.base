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

import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Checks whether a {@link MessageList} contains a message with a certain message code.
 */
public class MessageInvalidObjectMatcher extends BaseMatcher<Message> {

    private Object invalidObject;

    MessageInvalidObjectMatcher(Object invalidObject) {
        this.invalidObject = invalidObject;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message containing the invalid object: " + invalidObject);
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Message)) {
            return false;
        }
        Message message = (Message)item;
        ObjectProperty[] invalidObjectProperties = message.getInvalidObjectProperties();
        for (ObjectProperty objectProperty : invalidObjectProperties) {
            if (objectProperty.getObject().equals(objectProperty.getObject())) {
                return true;
            }
        }
        return false;
    }
}