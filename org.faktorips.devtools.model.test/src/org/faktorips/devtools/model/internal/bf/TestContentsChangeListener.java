/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.bf;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

class TestContentsChangeListener implements ContentsChangeListener {

    private List<IIpsObjectPartContainer> ipsParts = new ArrayList<IIpsObjectPartContainer>();
    private List<Integer> eventTypes = new ArrayList<Integer>();

    public List<IIpsObjectPartContainer> getIpsObjectParts() {
        return ipsParts;
    }

    public List<Integer> getEventTypes() {
        return eventTypes;
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        ipsParts.add(event.getPart());
        eventTypes.add(event.getEventType());
    }

    public void clear() {
        ipsParts.clear();
        eventTypes.clear();
    }
}
