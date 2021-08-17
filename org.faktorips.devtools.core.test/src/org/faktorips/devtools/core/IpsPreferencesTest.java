/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Assert;
import org.junit.Test;

public class IpsPreferencesTest extends AbstractIpsPluginTest {
    private IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();

    public IpsPreferencesTest() {
        super();
    }

    @Test
    public void testGetNullPresentation() throws Exception {
        Assert.assertEquals("<null>", ipsPreferences.getNullPresentation());
    }
}
