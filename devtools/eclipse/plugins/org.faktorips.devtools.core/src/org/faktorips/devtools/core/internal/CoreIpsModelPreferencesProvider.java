/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.preferences.IIpsModelPreferencesProvider;

public class CoreIpsModelPreferencesProvider implements IIpsModelPreferencesProvider {

    @Override
    public IIpsModelPreferences getModelPreferences() {
        return IpsPlugin.getDefault().getIpsPreferences();
    }

}
