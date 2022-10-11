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

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IpsObjectType}-{@link List}-supplier for all implementations of the extension point
 * {@value ExtensionPoints#PRODUCT_COMPONENT_NAMING_STRATEGY}.
 */
public class IpsObjectTypeExtensions extends LazyListExtension<IpsObjectType> {

    public IpsObjectTypeExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                ExtensionPoints.IPS_OBJECT_TYPE,
                ExtensionPoints.ATTRIBUTE_CLASS,
                IpsObjectType.class);
    }

}
