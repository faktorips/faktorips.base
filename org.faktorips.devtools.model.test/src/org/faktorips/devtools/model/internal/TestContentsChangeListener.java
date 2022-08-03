/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;

/**
 * 
 * @author Jan Ortmann
 */
public class TestContentsChangeListener implements ContentsChangeListener {

    private int count;

    private ContentChangeEvent lastEvent;

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
        count++;

    }

    public int getNumOfEventsReceived() {
        return count;
    }

    public ContentChangeEvent getLastEvent() {
        return lastEvent;
    }

}
