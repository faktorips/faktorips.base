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

import org.faktorips.runtime.Message;
import org.faktorips.testsupport.IpsMatchers;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Checks whether a {@link Message} contains the given text.
 * 
 * @deprecated since 21.12. Use {@link IpsMatchers#containsText(String)} instead.
 */
@Deprecated
public class MessageTextMatcher extends TypeSafeMatcher<Message> {

    private String text;

    MessageTextMatcher(String text) {
        this.text = text;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message containing the text: " + text);
    }

    @Override
    public boolean matchesSafely(Message message) {
        return message.getText().contains(text);
    }
}