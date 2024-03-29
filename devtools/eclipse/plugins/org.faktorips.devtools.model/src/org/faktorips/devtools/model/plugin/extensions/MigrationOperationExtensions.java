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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * {@link IIpsProjectMigrationOperationFactory}-by-{@link AVersion version}-{@link Map}-supplier for
 * all implementations of the extension point {@value #EXTENSION_POINT_ID_MIGRATION_OPERATION}.
 */
public class MigrationOperationExtensions extends
        LazyCollectionExtension<IIpsProjectMigrationOperationFactory, Map<String, Map<AVersion, IIpsProjectMigrationOperationFactory>>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_MIGRATION_OPERATION}.
     */
    public static final String EXTENSION_POINT_ID_MIGRATION_OPERATION = "ipsMigrationOperation"; //$NON-NLS-1$

    /**
     * The name of the {@link IConfigurationElement configuration element} property
     * {@value #CONFIG_ELEMENT_PROPERTY_TARGET_VERSION} for
     * {@link #EXTENSION_POINT_ID_MIGRATION_OPERATION}.
     */
    public static final String CONFIG_ELEMENT_PROPERTY_TARGET_VERSION = "targetVersion"; //$NON-NLS-1$

    public MigrationOperationExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_MIGRATION_OPERATION,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsProjectMigrationOperationFactory.class,
                LinkedHashMap::new,
                (configElement, factory, factoryByVersionByContributorMap) -> {
                    Map<AVersion, IIpsProjectMigrationOperationFactory> factoryByVersionMap = factoryByVersionByContributorMap
                            .computeIfAbsent(configElement.getContributor().getName(), $ -> new LinkedHashMap<>());
                    factoryByVersionMap.put(
                            AVersion.parse(configElement.getAttribute(CONFIG_ELEMENT_PROPERTY_TARGET_VERSION)),
                            factory);
                });
    }

}
