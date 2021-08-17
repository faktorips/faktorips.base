/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators;

import java.util.List;

import org.faktorips.devtools.model.decorators.internal.IpsElementDecoratorsProvidersViaEclipsePlugins;

/**
 * Provides access to all {@link IIpsElementDecoratorsProvider} instances available in the current
 * Faktor-IPS instance.
 *
 * @since 21.6
 */
public interface IIpsElementDecoratorsProviders {

    /**
     * Returns the singleton instance of {@link IIpsElementDecoratorsProviders}.
     */
    static IIpsElementDecoratorsProviders get() {
        return IpsElementDecoratorsProvidersViaEclipsePlugins.get();
    }

    /**
     * Returns all {@link IIpsElementDecoratorsProvider} instances available in the current
     * Faktor-IPS instance.
     */
    List<IIpsElementDecoratorsProvider> getIpsElementDecoratorsProviders();
}
