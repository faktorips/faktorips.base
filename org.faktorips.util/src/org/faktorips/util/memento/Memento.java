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
 * A memento stores the state of another object called the originator in memory. The memento can be
 * used to reset the originators state to the state saved in the memento.
 */
public interface Memento {

    /**
     * Returns the object this is a memento of.
     */
    public Object getOriginator();

}
