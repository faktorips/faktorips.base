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

import java.util.stream.Collectors;

import org.faktorips.runtime.IMarker;
import org.faktorips.runtime.Message;
import org.hamcrest.Description;

/**
 * Matches a {@link Message} if it's {@link Message#getMarkers() markers} contain the given
 * {@link IMarker marker}.
 */
public class MessageMarkerMatcher extends MessageMatcher {

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

    @Override
    protected void describeMessageProperty(Description description) {
        description.appendText("has the marker " + marker);
    }

    @Override
    protected void describeMismatchedProperty(Message message, Description mismatchDescription) {
        mismatchDescription.appendText("had ");
        switch (message.getMarkers().size()) {
            case 0:
                mismatchDescription.appendText("no markers");
                break;
            case 1:
                mismatchDescription.appendText("only the marker ");
                mismatchDescription.appendValue(message.getMarkers().iterator().next());
                break;
            default:
                mismatchDescription.appendText("the markers ");
                mismatchDescription.appendValue(message.getMarkers().stream()
                        .map(IMarker::toString)
                        .collect(Collectors.joining(", ")));
        }
    }

}
