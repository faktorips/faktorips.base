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

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProvider;
import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProviders;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class IpsElementDecoratorsProvidersViaEclipsePlugins implements IIpsElementDecoratorsProviders {

    private static /* final */ IpsElementDecoratorsProvidersViaEclipsePlugins instance = new IpsElementDecoratorsProvidersViaEclipsePlugins();

    private final Supplier<List<IIpsElementDecoratorsProvider>> ipsElementDecoratorsProviders;

    public IpsElementDecoratorsProvidersViaEclipsePlugins() {
        this(Platform.getExtensionRegistry());
    }

    public IpsElementDecoratorsProvidersViaEclipsePlugins(IExtensionRegistry extensionRegistry) {
        ExtensionPoints extensionPoints = new ExtensionPoints(extensionRegistry,
                IpsModelDecoratorsPluginActivator.PLUGIN_ID);
        ipsElementDecoratorsProviders = new IpsElementDecoratorsProviderExtensions(extensionPoints);
    }

    /**
     * Returns the singleton instance initialized from the Eclipse {@link Platform}.
     */
    public static IpsElementDecoratorsProvidersViaEclipsePlugins get() {
        return instance;
    }

    @Override
    public List<IIpsElementDecoratorsProvider> getIpsElementDecoratorsProviders() {
        return ipsElementDecoratorsProviders.get();
    }

}
