/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JarFileFactoryTest extends AbstractIpsPluginTest {

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

}
