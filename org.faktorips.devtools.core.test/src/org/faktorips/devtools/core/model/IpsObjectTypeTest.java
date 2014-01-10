/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsObjectTypeTest extends AbstractIpsPluginTest {

    @Test
    public void testNewObject() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IpsObjectType[] types = IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes();
        for (IpsObjectType type : types) {
            IIpsPackageFragment pack = ipsProject.getIpsPackageFragmentRoots()[0].createPackageFragment("test", true,
                    null);
            // pack.createIpsFile(...) calls newObject() !!!
            IIpsSrcFile file = pack.createIpsFile(type, "TestObject", true, null);
            assertNotNull(file.getIpsObject());
        }
    }

}
