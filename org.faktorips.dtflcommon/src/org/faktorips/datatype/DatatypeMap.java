/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * A map that stores <code>Datatype</code>s as values and uses their name as the access key.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeMap {

    /**
     * Returns the Datatype with the given name, if the registry contains a datatype with the given
     * name. Returns null, if the map does not contain a datatype with the given name.
     * 
     * @throws IllegalArgumentException if the name is null.
     */
    public abstract Datatype getDatatype(String name) throws IllegalArgumentException;

    /**
     * Returns the datatyppes available in the map.
     */
    public abstract Datatype[] getDatatypes();

}
