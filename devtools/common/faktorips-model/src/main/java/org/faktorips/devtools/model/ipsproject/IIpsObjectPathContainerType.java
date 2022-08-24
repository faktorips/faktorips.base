/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

/**
 * Represents an IPS object path container type. While IPS object path container exist per project,
 * (or to be precise: per IPS object path entry, although there are rarely two containers of the
 * same container type) a container type exists only once in an IPS model.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathContainerType {

    /**
     * Returns the container type's id that is unique in the IPS model.
     */
    String getId();

    /**
     * Creates and initializes a new container for the given project.
     * 
     * @param ipsProject The IPS project.
     * @param optionalPath An optional path.
     * 
     * @return The new container.
     * 
     * @throws NullPointerException if ipsProject or optionalPath is <code>null</code>.
     */
    IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String optionalPath);

}
