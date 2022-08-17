/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import static org.junit.Assert.assertNotNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsObjectTypeTest extends AbstractIpsPluginTest {

    @Test
    public void testNewObject() {
        IIpsProject ipsProject = newIpsProject();
        IpsObjectType[] types = IIpsModel.get().getIpsObjectTypes();
        for (IpsObjectType type : types) {
            IIpsPackageFragment pack = ipsProject.getIpsPackageFragmentRoots()[0].createPackageFragment("test", true,
                    null);
            // pack.createIpsFile(...) calls newObject() !!!
            IIpsSrcFile file = pack.createIpsFile(type, "TestObject", true, null);
            assertNotNull(file.getIpsObject());
        }
    }

}
