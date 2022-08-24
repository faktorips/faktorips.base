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

import org.faktorips.devtools.model.internal.preferences.DefaultIpsModelPreferences;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.preferences.IIpsModelPreferencesProvider;

/**
 * {@link IIpsModelPreferences}-supplier for the single implementation of the extension point
 * {@value #EXTENSION_POINT_ID_IPS_MODEL_PREFERENCES}.
 */
public class ModelPreferencesExtension extends SingleLazyExtension<IIpsModelPreferencesProvider, IIpsModelPreferences> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_IPS_MODEL_PREFERENCES}.
     */
    public static final String EXTENSION_POINT_ID_IPS_MODEL_PREFERENCES = "ipsModelPreferences"; //$NON-NLS-1$

    public ModelPreferencesExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_IPS_MODEL_PREFERENCES,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_PROVIDER,
                IIpsModelPreferencesProvider.class,
                IIpsModelPreferencesProvider::getModelPreferences,
                DefaultIpsModelPreferences::new);
    }

}
