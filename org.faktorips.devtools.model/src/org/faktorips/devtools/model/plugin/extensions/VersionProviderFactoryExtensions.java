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

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IVersionProviderFactory}-by-id-{@link Map}-supplier for all implementations of the
 * extension point {@value #EXTENSION_POINT_ID_VERSION_PROVIDER}.
 */
public class VersionProviderFactoryExtensions
        extends LazyCollectionExtension<IVersionProviderFactory, Map<String, IVersionProviderFactory>> {

    /**
     * The extension point id of the extension point {@value #EXTENSION_POINT_ID_VERSION_PROVIDER}.
     */
    public static final String EXTENSION_POINT_ID_VERSION_PROVIDER = "versionProvider"; //$NON-NLS-1$

    public static final String EXTENSION_ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    public VersionProviderFactoryExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_VERSION_PROVIDER,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IVersionProviderFactory.class,
                HashMap::new,
                (configElement, factory, factoriesByIdMap) -> factoriesByIdMap
                        .put(configElement.getAttribute(EXTENSION_ATTRIBUTE_ID), factory));
    }

}
