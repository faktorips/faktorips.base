/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plugin;

import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class DummyDependencyGraphPersistenceManager implements IDependencyGraphPersistenceManager {

    @Override
    public IDependencyGraph getDependencyGraph(IIpsProject project) {
        throw new UnsupportedOperationException("Dependency Graph Persistence is not yet implemented");
    }

}
