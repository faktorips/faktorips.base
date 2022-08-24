/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.w3c.dom.Element;

/**
 * A memento for a source file that stores an old object state (as XML element) and the dirty state.
 * <p>
 * This is an application of the memento pattern.
 */
public class IpsSrcFileMemento implements IIpsSrcFileMemento {

    private IIpsSrcFile file;
    private Element state;
    private boolean dirty;

    public IpsSrcFileMemento(IIpsSrcFile file, Element state, boolean dirty) {
        this.file = file;
        this.state = state;
        this.dirty = dirty;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

    @Override
    public Element getState() {
        return state;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

}
