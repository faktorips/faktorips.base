/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

class TestContentsChangeListener implements ContentsChangeListener {

    private List<IIpsObjectPart> ipsParts = new ArrayList<IIpsObjectPart>();
    private List<Integer> eventTypes = new ArrayList<Integer>();

    public List<IIpsObjectPart> getIpsObjectParts() {
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
