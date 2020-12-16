/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.memento;

import org.w3c.dom.Element;

/**
 * A memento that stores the originator's state as XML element.
 */
public class XmlMemento implements Memento {

    private Object originator;
    private Element state;

    /**
     * Creates a new memento.
     * 
     * @param originator the object this is a memento for.
     * @param state the originator's state as XML element.
     */
    public XmlMemento(Object originator, Element state) {
        this.originator = originator;
        this.state = state;
    }

    @Override
    public Object getOriginator() {
        return originator;
    }

    /**
     * Returns the originator's state stored in the memento.
     */
    public Element getState() {
        return state;
    }

}
