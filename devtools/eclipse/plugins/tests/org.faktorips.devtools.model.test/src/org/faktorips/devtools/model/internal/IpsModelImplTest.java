/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsModelImplTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
    }

    @Test
    public void testGetIpsObjectPath() {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("newpackage");
        ipsProject.setIpsObjectPath(path);

        // path is created in the first call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        // path is read from the cache in the second call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        IIpsProject secondProject = newIpsProject("TestProject2");
        IIpsObjectPath secondPath = secondProject.getIpsObjectPath();
        secondPath.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("secondpackage");
        secondProject.setIpsObjectPath(secondPath);

        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("secondpackage",
                secondPath.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
    }

}
