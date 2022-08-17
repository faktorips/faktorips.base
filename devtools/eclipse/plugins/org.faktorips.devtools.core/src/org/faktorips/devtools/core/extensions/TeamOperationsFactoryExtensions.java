/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.extensions;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.extensions.LazyCollectionExtension;

public class TeamOperationsFactoryExtensions
        extends LazyCollectionExtension<ITeamOperationsFactory, Set<ITeamOperationsFactory>> {

    private static final String EXTENSION_POINT_ID_TEAM_OPERATIONS_FACTORY = "teamOperationsFactory"; //$NON-NLS-1$

    public TeamOperationsFactoryExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_TEAM_OPERATIONS_FACTORY,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                ITeamOperationsFactory.class,
                HashSet::new,
                ($, factory, set) -> set.add(factory));
    }

}
