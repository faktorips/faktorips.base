/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.junit.Before;
import org.junit.Test;

public class EclipseFolderTest extends EclipseAbstractionTestSetup {

    private AProject testProject;
    private AFolder testFolder;
    private IFolder eclipseFolder;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject");
        testFolder = testProject.getFolder("TestFolder");
        testFolder.create(null);
        eclipseFolder = ((IProject)testProject.unwrap()).getFolder("TestFolder");
    }

    @Test
    public void testAEclipseFolder() {
        assertThat(testFolder.unwrap(), is(instanceOf(IFolder.class)));
    }

    @Test
    public void testGetFolder() {
        AFolder newFolder = testFolder.getFolder("SubFolder");
        newFolder.create(new NullProgressMonitor());

        assertThat(newFolder, is(wrapperOf(eclipseFolder.getFolder("SubFolder"))));
    }

    @Test
    public void testGetFile() {
        AFile newFile = testFolder.getFile("SubFile");
        newFile.create(writeTo("Content"), new NullProgressMonitor());

        assertThat(newFile, is(wrapperOf(eclipseFolder.getFile("SubFile"))));
    }

    @Test
    public void testGetFolder_notAFolder() {
        AFile newFile = testFolder.getFile("SubFile");
        newFile.create(writeTo("Content"), new NullProgressMonitor());

        assertThat(testFolder.getFolder(Path.of("SubFile")).exists(), is(false));
    }

    @Test
    public void testGetFile_notAFile() {
        AFolder newFolder = testFolder.getFolder("SubFolder");
        newFolder.create(new NullProgressMonitor());

        assertThat(testFolder.getFile(Path.of("SubFolder")).exists(), is(false));
    }

    @Test
    public void testGetType() {
        assertThat(testProject.getFolder("newFolder").getType(), is(AResourceType.FOLDER));
    }
}
