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

import java.util.Optional;
import java.util.stream.Collectors;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.ObjectProperty;
import org.hamcrest.Description;

/**
 * Checks whether a {@link Message} contains the given invalid object.
 */
public class MessageInvalidObjectMatcher extends MessageMatcher {

    private Object invalidObject;
    private Optional<String> propertyName;

    public MessageInvalidObjectMatcher(Object invalidObject) {
        this.invalidObject = invalidObject;
        propertyName = Optional.empty();
    }

    public MessageInvalidObjectMatcher(Object invalidObject, String propertyName) {
        this.invalidObject = invalidObject;
        this.propertyName = Optional.ofNullable(propertyName);
    }

    @Override
    protected void describeMessageProperty(Description description) {
        description.appendText("contains the invalid object: " + invalidObject);

        if (propertyName.isPresent()) {
            description.appendText(" for the property: " + propertyName.get());
        }
    }

    @Override
    protected void describeMismatchedProperty(Message message, Description mismatchDescription) {
        mismatchDescription.appendText("had ");
        switch (message.getNumOfInvalidObjectProperties()) {
            case 0:
                mismatchDescription.appendText("no invalid object properties");
                break;
            case 1:
                mismatchDescription.appendText("only the invalid object property ");
                mismatchDescription.appendValue(message.getInvalidObjectProperties().get(0));
                break;
            default:
                mismatchDescription.appendText("the invalid object properties ");
                mismatchDescription.appendValue(message.getInvalidObjectProperties().stream()
                        .map(ObjectProperty::toString)
                        .collect(Collectors.joining(", ")));
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
