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

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.Message;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matches a {@link Message} if it's {@link Message#getMarkers() markers} contain the given
 * {@link IMarker marker}.
 */
public class MessageMarkerMatcher extends TypeSafeMatcher<Message> {

    private final IMarker marker;

    public MessageMarkerMatcher(IMarker marker) {
        this.marker = marker;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a message with marker " + marker);
    }

    @Override
    protected boolean matchesSafely(Message m) {
        return m.hasMarker(marker);
    }

}
