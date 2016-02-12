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
import org.hamcrest.BaseMatcher;

public abstract class AbstractMessageListMatcher extends BaseMatcher<MessageList> {

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof MessageList)) {
            return false;
        }
        return matchesSafely((MessageList)item);
    }

    abstract boolean matchesSafely(MessageList item);

}
