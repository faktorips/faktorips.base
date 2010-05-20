/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import org.w3c.dom.Element;

/**
 * A memento for a source file that stores an old object state (as XML element) and the dirty state.
 * <p>
 * This is an application of the memento pattern.
 */
public class IIpsSrcFileMemento {

    private IIpsSrcFile file;
    private Element state;
    private boolean dirty;

    public IIpsSrcFileMemento(IIpsSrcFile file, Element state, boolean dirty) {
        this.file = file;
        this.state = state;
        this.dirty = dirty;
    }

    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

    public Element getState() {
        return state;
    }

    public boolean isDirty() {
        return dirty;
    }

}
