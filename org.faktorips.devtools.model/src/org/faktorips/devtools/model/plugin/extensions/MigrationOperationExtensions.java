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

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.osgi.framework.Version;

/**
 * {@link IIpsProjectMigrationOperationFactory}-by-{@link Version}-{@link Map}-supplier for all
 * implementations of the extension point {@value #EXTENSION_POINT_ID_MIGRATION_OPERATION}.
 */
public class MigrationOperationExtensions extends
        LazyCollectionExtension<IIpsProjectMigrationOperationFactory, Map<Version, IIpsProjectMigrationOperationFactory>> {

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
                HashMap::new,
                (configElement, factory, factoryByVersionMap) -> factoryByVersionMap.put(
                        Version.parseVersion(configElement.getAttribute(CONFIG_ELEMENT_PROPERTY_TARGET_VERSION)),
                        factory));
    }

}
