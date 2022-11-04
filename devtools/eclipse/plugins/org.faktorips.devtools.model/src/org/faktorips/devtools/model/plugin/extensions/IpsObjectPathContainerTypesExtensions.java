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

import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IIpsObjectPathContainerType}-{@link List}-supplier for all implementations of the
 * extension point {@value #EXTENSION_POINT_ID_IPS_OBJECT_PATH_CONTAINER_TYPE}.
 */
public class IpsObjectPathContainerTypesExtensions extends LazyListExtension<IIpsObjectPathContainerType> {

    /**
     * IpsModelPlugin relative id of the extension point for IPS object path container types.
     * 
     * @see IIpsObjectPathContainerType
     */
    public static final String EXTENSION_POINT_ID_IPS_OBJECT_PATH_CONTAINER_TYPE = "ipsObjectPathContainerType"; //$NON-NLS-1$

    public IpsObjectPathContainerTypesExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_IPS_OBJECT_PATH_CONTAINER_TYPE,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsObjectPathContainerType.class);
    }

}
