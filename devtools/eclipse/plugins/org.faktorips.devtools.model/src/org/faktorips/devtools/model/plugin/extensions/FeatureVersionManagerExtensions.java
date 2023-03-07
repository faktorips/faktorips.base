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

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.versionmanager.EmptyIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IExtendableVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

/**
 * {@link IIpsFeatureVersionManager}-{@link List}-supplier for all implementations of the extension
 * point {@value #EXTENSION_POINT_ID_FEATURE_VERSION_MANAGER}.
 */
public class FeatureVersionManagerExtensions extends
        LazyCollectionExtension<IIpsFeatureVersionManager, List<IIpsFeatureVersionManager>> {
    /**
     * The extension point id of the extension point <code>faktorIpsFeatureVersionManager</code>.
     */
    public static final String EXTENSION_POINT_ID_FEATURE_VERSION_MANAGER = "faktorIpsFeatureVersionManager"; //$NON-NLS-1$

    public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    public static final String ATTRIBUTE_FEATURE_ID = "featureId"; //$NON-NLS-1$

    public static final String ATTRIBUTE_REQUIRED_FOR_ALL_PROJECTS = "requiredForAllProjects"; //$NON-NLS-1$

    public static final String ATTRIBUTE_BASED_ON_FEATURE_MANAGER = "basedOnFeatureManager"; //$NON-NLS-1$

    public FeatureVersionManagerExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_FEATURE_VERSION_MANAGER,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsFeatureVersionManager.class,
                ArrayList::new,
                FeatureVersionManagerExtensions::initializeIpsFeatureVersionManager,
                FeatureVersionManagerExtensions::addEmptyIpsFeatureVersionManagerIfEmpty);
    }

    private static void initializeIpsFeatureVersionManager(IConfigurationElement configElement,
            IIpsFeatureVersionManager manager,
            List<IIpsFeatureVersionManager> list) {
        if (manager instanceof IExtendableVersionManager extendableVersionManager) {
            extendableVersionManager.setContributorName(configElement.getContributor().getName());
        }
        manager.setFeatureId(configElement.getAttribute(ATTRIBUTE_FEATURE_ID));
        manager.setId(configElement.getAttribute(ATTRIBUTE_ID));
        manager.setPredecessorId(configElement.getAttribute(ATTRIBUTE_BASED_ON_FEATURE_MANAGER));
        manager.setRequiredForAllProjects(Boolean
                .parseBoolean(configElement.getAttribute(ATTRIBUTE_REQUIRED_FOR_ALL_PROJECTS)));
        list.add(manager);
    }

    private static void addEmptyIpsFeatureVersionManagerIfEmpty(List<IIpsFeatureVersionManager> result) {
        if (result.isEmpty()) {
            result.add(EmptyIpsFeatureVersionManager.INSTANCE);
        }
    }

}
