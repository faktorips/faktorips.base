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

import java.util.Optional;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.ObjectProperty;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Checks whether a {@link Message} contains the given invalid object.
 */
public class MessageInvalidObjectMatcher extends TypeSafeMatcher<Message> {

    private Object invalidObject;
    private Optional<String> propertyName;

    MessageInvalidObjectMatcher(Object invalidObject) {
        this(invalidObject, null);
    }

    MessageInvalidObjectMatcher(Object invalidObject, String propertyName) {
        this.invalidObject = invalidObject;
        this.propertyName = Optional.ofNullable(propertyName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message containing the invalid object: " + invalidObject);

        if (propertyName.isPresent()) {
            description.appendText(" for the property: " + propertyName.get());
        }
    }

    @Override
    public boolean matchesSafely(Message message) {
        for (ObjectProperty objectProperty : message.getInvalidObjectProperties()) {
            if (propertyName.isPresent()) {
                if (!propertyName.get().equals(objectProperty.getProperty())) {
                    continue;
                }
            }
            if (objectProperty.getObject().equals(invalidObject)) {
                return true;
            }
        }

        return false;
    }
}