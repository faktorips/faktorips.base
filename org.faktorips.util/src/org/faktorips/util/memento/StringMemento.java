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

/**
 * A memento that stores the originator's state as string.
 */
public class StringMemento implements Memento {

    private Object originator;
    private String state;

    /**
     * Creates a new memento.
     * 
     * @param originator the object this is a memento for.
     * @param state the originator's state as string.
     */
    public StringMemento(Object originator, String state) {
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
    public String getState() {
        return state;
    }

}
