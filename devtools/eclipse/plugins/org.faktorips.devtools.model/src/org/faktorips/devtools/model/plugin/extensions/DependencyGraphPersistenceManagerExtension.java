/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.plugin.DummyDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IDependencyGraphPersistenceManager}-supplier for the single implementation of the
 * extension point {@value #EXTENSION_POINT_ID_DEPENDENCY_GRAPH_PERSISTENCE_MANAGER}.
 */
public class DependencyGraphPersistenceManagerExtension
        extends SimpleSingleLazyExtension<IDependencyGraphPersistenceManager> {
    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_DEPENDENCY_GRAPH_PERSISTENCE_MANAGER}.
     */
    public static final String EXTENSION_POINT_ID_DEPENDENCY_GRAPH_PERSISTENCE_MANAGER = "dependencyGraphPersistenceManager"; //$NON-NLS-1$

    public DependencyGraphPersistenceManagerExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_DEPENDENCY_GRAPH_PERSISTENCE_MANAGER,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IDependencyGraphPersistenceManager.class,
                DummyDependencyGraphPersistenceManager::new);
    }

}
