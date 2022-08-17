/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class IpsWorkspacePreferences {

    private static final String PLUGIN_ID = IpsUIPlugin.getDefault().getBundle().getSymbolicName();

    private final IPreferencesService preferencesService;

    public IpsWorkspacePreferences() {
        preferencesService = Platform.getPreferencesService();
    }

    IpsWorkspacePreferences(IPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    /**
     * Return the boolean value stored in the preferences with the given id. Returns {@code false}
     * if the preferences do not contain a value with the given id.
     */
    public boolean getBoolean(String preferenceId) {
        return getBoolean(preferenceId, false);
    }

    /**
     * Return the boolean value stored in the preferences with the given id. Returns given default
     * value if the key is not defined.
     */
    public boolean getBoolean(String preferenceId, boolean defaultValue) {
        return preferencesService.getBoolean(PLUGIN_ID, preferenceId, defaultValue, null);
    }

    /** Stores the given boolean with the given id in the preferences. */
    public void putBoolean(String preferenceId, boolean b) {
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        node.putBoolean(preferenceId, b);
    }
}
