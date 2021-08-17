/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

public interface IDependencyGraphPersistenceManager {

    /**
     * Returns the last persisted dependency graph for the provided IpsProject if available.
     * Otherwise <code>null</code> will be returned.
     * 
     * @throws NullPointerException if the provided project is <code>null</code>
     */
    IDependencyGraph getDependencyGraph(IIpsProject project);

}