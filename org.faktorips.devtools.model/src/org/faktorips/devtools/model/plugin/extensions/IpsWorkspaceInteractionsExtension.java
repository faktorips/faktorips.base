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

import org.faktorips.devtools.model.plugin.DummyIpsWorkspaceInteractions;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IIpsWorkspaceInteractions;

/**
 * {@link IIpsWorkspaceInteractions}-supplier for the single implementation of the extension point
 * {@value #EXTENSION_POINT_ID_WORKSPACE_INTERACTIONS}.
 */
public class IpsWorkspaceInteractionsExtension extends SimpleSingleLazyExtension<IIpsWorkspaceInteractions> {
    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_WORKSPACE_INTERACTIONS}.
     */
    public static final String EXTENSION_POINT_ID_WORKSPACE_INTERACTIONS = "workspaceInteractions"; //$NON-NLS-1$

    public IpsWorkspaceInteractionsExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_WORKSPACE_INTERACTIONS,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsWorkspaceInteractions.class,
                DummyIpsWorkspaceInteractions::new);
    }

}
