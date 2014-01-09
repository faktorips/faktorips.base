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

package org.faktorips.util.memento;

/**
 * An interface that marks an object as one supporting the memento pattern.
 * 
 * @author Jan Ortmann
 */
public interface MementoSupport {

    /**
     * Creates a new memento that holds the object's current state.
     */
    public Memento newMemento();

    /**
     * Sets the object's state to the one stored in the memento.
     * 
     * @throws IllegalArgumentException if the memento does not contain this object's state.
     */
    public void setState(Memento memento);

}
