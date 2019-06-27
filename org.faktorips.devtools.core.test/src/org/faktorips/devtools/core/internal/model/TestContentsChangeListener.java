/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;

/**
 * 
 * @author Jan Ortmann
 */
public class TestContentsChangeListener implements ContentsChangeListener {

    private int count;

    private ContentChangeEvent lastEvent;

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        this.lastEvent = event;
        count++;

    }

    public int getNumOfEventsReceived() {
        return count;
    }

    public ContentChangeEvent getLastEvent() {
        return lastEvent;
    }

}
