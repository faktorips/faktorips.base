/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProvider;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.extensions.LazyCollectionExtension;

/**
 * {@link IIpsElementDecoratorsProvider}-{@link List}-supplier for all implementations of the
 * extension point {@value #EXTENSION_POINT_ID_DECORATORS_PROVIDER}.
 */
public class IpsElementDecoratorsProviderExtensions extends
        LazyCollectionExtension<IIpsElementDecoratorsProvider, List<IIpsElementDecoratorsProvider>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_DECORATORS_PROVIDER}.
     */
    public static final String EXTENSION_POINT_ID_DECORATORS_PROVIDER = "decoratorsProvider"; //$NON-NLS-1$

    public IpsElementDecoratorsProviderExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_DECORATORS_PROVIDER,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsElementDecoratorsProvider.class,
                ArrayList::new,
                ($, provider, list) -> list.add(provider));
    }

}
