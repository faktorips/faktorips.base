/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JarFileFactoryTest extends AbstractIpsPluginTest {

    @Test
    public void testGetAbsolutePath_inFileSystem() throws Exception {
        Path jarPath = Path.of("/any/where/file.jar");
        JarFileFactory jarFileFactory = new JarFileFactory(jarPath);

        Path absolutePath = jarFileFactory.getAbsolutePath(jarPath);

        assertEquals(jarPath, absolutePath);
    }

    @Test
    public void testGetAbsolutePath_inWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        IIpsObject newObject = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "anyName");
        AResource resource = newObject.getEnclosingResource();
        Path path = resource.getWorkspaceRelativePath();
        JarFileFactory jarFileFactory = new JarFileFactory(path);

        Path absolutePath = jarFileFactory.getAbsolutePath(path);

        assertEquals(resource.getLocation(), absolutePath);
    }

}
