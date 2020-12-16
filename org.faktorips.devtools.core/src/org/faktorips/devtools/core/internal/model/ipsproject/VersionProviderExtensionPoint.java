/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.IVersionProviderFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;

/**
 * This class handles the version provider extension point. Is it responsible for loading and
 * finding a extended {@link IVersionProvider} for a specific {@link IIpsProject}.
 * <p>
 * To use this class just instantiate with your project and call
 * {@link #getExtendedVersionProvider()}.
 * <p
 * To only check whether the configured version provider could be found, call
 * {@link #validateExtension()}.
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
     * provider cannot be found.
     */
    public IVersionProvider<?> getExtendedVersionProvider() {
        IConfigurationElement extension = getExtension();
        if (extension != null) {
            return instantiateVersionProvider(extension);
        } else {
            return null;
        }
    }

    private IConfigurationElement getExtension() {
        String versionProviderId = ipsProject.getReadOnlyProperties().getVersionProviderId();
        IConfigurationElement[] configElements = extensionRegistry.getConfigurationElementsFor(IpsPlugin.PLUGIN_ID,
                VERSION_PROVIDER_EXTENSION);
        for (IConfigurationElement confElement : configElements) {
            if (confElement.getAttribute(EXTENSION_ATTRIBUTE_ID).equals(versionProviderId)) {
                return confElement;
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

    public MessageList validateExtension() {
        MessageList result = new MessageList();
        if (getExtension() == null) {
            String text = NLS.bind(Messages.VersionProviderExtensionPoint_error_invalidVersionProvider, ipsProject
                    .getReadOnlyProperties().getVersionProviderId());
            result.newError(IIpsProjectProperties.MSGCODE_INVALID_VERSION_SETTING, text, ipsProject.getProperties(),
                    IIpsProjectProperties.PROPERTY_VERSION_PROVIDER_ID);
        }
        return result;
    }

}
