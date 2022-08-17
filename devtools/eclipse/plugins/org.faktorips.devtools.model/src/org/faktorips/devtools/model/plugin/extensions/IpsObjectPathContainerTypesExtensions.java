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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IIpsObjectPathContainerType}-{@link List}-supplier for all implementations of the
 * extension point {@value ExtensionPoints#IPS_OBJECT_PATH_CONTAINER_TYPE}.
 */
public class IpsObjectPathContainerTypesExtensions extends
        LazyCollectionExtension<IIpsObjectPathContainerType, List<IIpsObjectPathContainerType>> {

    public IpsObjectPathContainerTypesExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                ExtensionPoints.IPS_OBJECT_PATH_CONTAINER_TYPE,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsObjectPathContainerType.class,
                ArrayList::new,
                ($, containerType, list) -> list.add(containerType));
    }

}
