/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.IVersionProviderFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This class handles the version provider extension point. Is it responsible for loading and
 * finding a extended {@link IVersionProvider} for a specific {@link IIpsProject}.
 * <p>
 * To use this class just instantiate with your project and call
 * {@link #getExtendedVersionProvider()}.
 */
public class VersionProviderExtensionPoint {

    /**
     * IpsPlugin relative id of the extension point for "Version Provider".
     * 
     * @see IVersionProvider
     */
    public static final String VERSION_PROVIDER_EXTENSION = "versionProvider"; //$NON-NLS-1$

    public static final String EXTENSION_ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    private final IIpsProject ipsProject;

    private final IExtensionRegistry extensionRegistry;

    /**
     * Use this constructor to initialize the {@link VersionProviderExtensionPoint} with the
     * specified project using the default {@link IExtensionRegistry} provided by {@link IpsPlugin}.
     */
    public VersionProviderExtensionPoint(IIpsProject ipsProject) {
        this(ipsProject, IpsPlugin.getDefault().getExtensionRegistry());
    }

    /**
     * Use this constructor to initialize the {@link VersionProviderExtensionPoint} with the
     * specified project using a specific {@link IExtensionRegistry} for example for testing
     * purposes.
     */
    public VersionProviderExtensionPoint(IIpsProject ipsProject, IExtensionRegistry extensionRegistry) {
        this.ipsProject = ipsProject;
        this.extensionRegistry = extensionRegistry;
    }

    /**
     * Creates a {@link IVersionProvider} corresponding to the projects configurations. Returns
     * <code>null</code> if there is no version provider configured or the requested version
     * provider cannot be found. In second case an error is logged.
     */
    public IVersionProvider<?> getExtendedVersionProvider() {
        String versionProviderId = ipsProject.getReadOnlyProperties().getVersionProviderId();
        return createExtendedVersionProvider(versionProviderId);
    }

    private IVersionProvider<?> createExtendedVersionProvider(String versionProviderId) {
        IConfigurationElement[] configElements = extensionRegistry.getConfigurationElementsFor(IpsPlugin.PLUGIN_ID,
                VERSION_PROVIDER_EXTENSION);
        for (IConfigurationElement confElement : configElements) {
            if (confElement.getAttribute(EXTENSION_ATTRIBUTE_ID).equals(versionProviderId)) {
                return instantiateVersionProvider(confElement);
            }
        }
        return null;
    }

    private IVersionProvider<?> instantiateVersionProvider(IConfigurationElement confElement) {
        IVersionProviderFactory versionProviderFactory = ExtensionPoints
                .createExecutableExtension(VERSION_PROVIDER_EXTENSION, confElement, ExtensionPoints.ATTRIBUTE_CLASS,
                        IVersionProviderFactory.class);
        if (versionProviderFactory != null) {
            return versionProviderFactory.createVersionProvider(ipsProject);
        } else {
            return null;
        }

    }

}
