/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;

public class NestedProjectFileUtilTest extends AbstractIpsPluginTest {

    @Test
    public void testGetFile_createdInSubproject() throws CoreException {
        String parent = "P" + UUID.randomUUID().toString();
        String child = "X" + UUID.randomUUID().toString();
        IProject parentProject = newPlatformProject(parent);
        IFolder childFolder = parentProject.getFolder(child);
        childFolder.create(false, true, null);

        IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(child);
        description.setLocation(childFolder.getLocation());
        IProject childProject = newPlatformProject(child, description);

        IFile fileInChildProject = childProject.getFile("fileInChildProject");
        fileInChildProject.create(new ByteArrayInputStream("Content".getBytes()), true, new NullProgressMonitor());

        String filename = fileInChildProject.getRawLocation().toOSString();

        IFile foundFile = NestedProjectFileUtil.getFile(filename);

        assertThat(foundFile.getProject(), is(childProject.getProject()));
    }

    @Test
    public void testGetFile_createdInParentProjectsSubFolder() throws CoreException {
        String parent = "P" + UUID.randomUUID().toString();
        String child = "X" + UUID.randomUUID().toString();
        IProject parentProject = newPlatformProject(parent);
        IFolder childFolder = parentProject.getFolder(child);
        childFolder.create(false, true, null);
        IFile fileInFolder = childFolder.getFile("fileInFolder");
        fileInFolder.create(new ByteArrayInputStream("Content".getBytes()), true, new NullProgressMonitor());
        String filename = fileInFolder.getRawLocation().toOSString();

        IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(child);
        description.setLocation(childFolder.getLocation());
        IProject childProject = newPlatformProject(child, description);

        IFile file = NestedProjectFileUtil.getFile(filename);

        assertThat(file.getProject(), is(childProject.getProject()));
    }
}
