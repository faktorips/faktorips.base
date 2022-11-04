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

import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IIpsProjectConfigurator}-supplier for the single implementation of the extension point
 * {@value #EXTENSION_POINT_ID_ADD_IPS_NATURE}.
 */
public class IpsProjectConfigurerExtension extends LazyListExtension<IIpsProjectConfigurator> {

    /**
     * IpsModelPlugin relative id of the extension point for configuring a Java project as an IPS
     * project.
     */
    public static final String EXTENSION_POINT_ID_ADD_IPS_NATURE = "addIpsNature"; //$NON-NLS-1$

    public IpsProjectConfigurerExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_ADD_IPS_NATURE,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsProjectConfigurator.class);
    }
}
