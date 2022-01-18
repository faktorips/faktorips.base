/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaFolderTest extends PlainJavaAbstractionTestSetup {

    private AProject testProject;
    private AFolder testFolder;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject");
        testFolder = testProject.getFolder("TestFolder");
        testFolder.create(null);
    }

    @Test
    public void testPlainJavaFolder() {
        assertThat(testFolder.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetType() {
        assertThat(testProject.getFolder("SubFolder").getType(), is(AResourceType.FOLDER));
    }

    @Test
    public void testCreate() {
        AFolder testFolder = testProject.getFolder("SubFolder");

        testFolder.create(null);

        assertThat(testProject.getFolder(Path.of("SubFolder")).exists(), is(true));
        assertThat(testFolder, is(wrapperOf(testProject.getLocation().resolve("SubFolder").toFile())));
    }

    @Test
    public void testCreate_withMonitor() {
        AFolder testFolder = testProject.getFolder("SubFolder");

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        testFolder.create(monitor);

        assertThat(testProject.getFolder(Path.of("SubFolder")).exists(), is(true));
        assertThat(testFolder, is(wrapperOf(testProject.getLocation().resolve("SubFolder").toFile())));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getName(), is("Creating folder " + testFolder.getLocation()));
    }

    @Test
    public void testGetFolder() {
        AFolder subFolder = testFolder.getFolder("SubFolder");
        subFolder.create(null);

        assertThat(testFolder.getLocation().resolve("SubFolder").toFile().exists(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFolder_notAFolder() {
        AFolder testFolder = testProject.getFolder("SubFolder");
        testFolder.create(null);
        testFolder.getFile("SubSubFile")
                .create(writeTo("Content"), null);

        testFolder.getFolder("SubSubFile");
    }

    @Test
    public void testGetFile() {
        AFolder testFolder = testProject.getFolder("SubFileFolder");
        testFolder.create(null);
        AFile testFile = testFolder.getFile("SubFile");
        testFile.create(writeTo("Content"), null);

        assertThat(testFolder.getFile("SubFile").exists(), is(true));
        assertThat(testFile,
                is(wrapperOf(testProject.getLocation().resolve("SubFileFolder").resolve("SubFile").toFile())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFile_notAFile() {
        AFolder testFolder = testProject.getFolder("SubFileFolder");
        testFolder.create(null);
        AFolder subFileAsFolder = testFolder.getFolder("SubFolder");
        subFileAsFolder.create(null);

        testFolder.getFile("SubFolder");
    }
}
