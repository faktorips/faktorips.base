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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JarFileFactoryTest extends AbstractIpsPluginTest {

    private IFile jarFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String folder = getClass().getPackage().getName().replace(".", "/");
        InputStream is = getClass().getClassLoader().getResourceAsStream(folder + "/helloWorld.jar");
        IProject root = newIpsProject("TestJarLoading").getProject();
        jarFile = root.getFile(new Path("helloWorld.jar"));
        jarFile.create(is, true, null);
    }

    @Test
    public void testGetAbsolutePath_inFileSystem() throws Exception {
        IPath jarPath = new Path("/any/where/file.jar");
        JarFileFactory jarFileFactory = new JarFileFactory(jarPath);

        IPath absolutePath = jarFileFactory.getAbsolutePath(jarPath);

        assertEquals(jarPath, absolutePath);
    }

    @Test
    public void testGetAbsolutePath_inWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        IIpsObject newObject = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT, "anyName");
        IResource resource = newObject.getEnclosingResource();
        IPath path = resource.getFullPath();
        JarFileFactory jarFileFactory = new JarFileFactory(path);

        IPath absolutePath = jarFileFactory.getAbsolutePath(path);

        assertEquals(resource.getLocation(), absolutePath);
    }

    @Test
    public void testLoadAndCloseJar() throws Exception {
        JarFileFactory jarFileFactory = new JarFileFactory(jarFile.getFullPath());
        jarFileFactory.setCloseDelay(500);
        JarFile openJar = jarFileFactory.createJarFile();

        assertThat(openJar, is(notNullValue()));

        jarFileFactory.closeJarFile();

        assertThat(openJar.entries(), is(notNullValue()));

        Thread.sleep(600);

        try {
            openJar.entries();
            fail("expected an exception");
        } catch (IllegalStateException e) {
            // is expected
        }
    }

    @Test
    public void testLoadAndLoadAgainJar() throws Exception {
        JarFileFactory jarFileFactory = new JarFileFactory(jarFile.getFullPath());
        jarFileFactory.setCloseDelay(500);
        JarFile openJar = jarFileFactory.createJarFile();

        assertThat(openJar, is(notNullValue()));
        assertThat(openJar.entries(), is(notNullValue()));

        jarFileFactory.closeJarFile();

        JarFile sameJarOtherHandle = jarFileFactory.createJarFile();
        assertSame(openJar, sameJarOtherHandle);
        assertThat(sameJarOtherHandle.entries(), is(notNullValue()));

        Thread.sleep(300);

        assertThat(sameJarOtherHandle.entries(), is(notNullValue()));

        jarFileFactory.closeJarFile();

        assertThat(sameJarOtherHandle.entries(), is(notNullValue()));

        // need to wait until the file is closed
        Thread.sleep(600);
    }

}
