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
 * An object path entry that defines a reference to another IPS project.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectRefEntry extends IIpsObjectPathEntry {

    /**
     * Returns the IPS project being referenced by this entry.
     */
    IIpsProject getReferencedIpsProject();

}
