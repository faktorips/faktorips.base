/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
