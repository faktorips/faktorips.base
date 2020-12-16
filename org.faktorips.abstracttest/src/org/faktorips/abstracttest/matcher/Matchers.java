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

import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;

public class Matchers {

    private Matchers() {
        // avoid default constructor for utility class
    }

    public static Matcher<MessageList> hasMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, true);
    }

    public static Matcher<MessageList> lacksMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, false);
    }

    public static Matcher<MessageList> hasSize(int size) {
        return new MessageListSizeMatcher(size);
    }

    public static Matcher<MessageList> isEmpty() {
        return new EmptyMessageListMatcher();
    }

    public static Matcher<MessageList> containsMessages() {
        return new IsNot<MessageList>(new EmptyMessageListMatcher());
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject) {
        return new MessageInvalidObjectMatcher(invalidObject);
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject, String propertyName) {
        return new MessageInvalidObjectMatcher(invalidObject, propertyName);
    }

    public static Matcher<Message> hasSeverity(int severity) {
        return new MessageSevertiyMatcher(severity);
    }

    public static Matcher<MessageList> containsErrorMsg() {
        return new ContainsErrorMatcher();
    }

}
