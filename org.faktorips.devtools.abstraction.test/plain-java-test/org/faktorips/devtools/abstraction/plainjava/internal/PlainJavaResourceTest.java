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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaResourceTest extends PlainJavaAbstractionTestSetup {

    private AProject testProject;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject");
    }

    @Test
    public void testPlainJavaResource() {
        assertThat(Abstractions.getWorkspace().getRoot().unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.getFolder("src").unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.getFile("someFile").unwrap(), is(instanceOf(File.class)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAdapter() {
        testProject.getFolder("src").getAdapter(File.class);
    }

    @Test
    public void testIsAccessible() {
        assertThat(testProject.getFolder("src").isAccessible(), is(true));
    }

    @Test
    public void testGetParent() {
        assertThat(testProject.getFolder("src").getParent(), is(testProject));
    }

    @Test
    public void testGetProjectRelativePath() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");

        assertThat(sourceFolder.getProjectRelativePath(), is(Path.of("src/main/java")));
    }

    @Test
    public void testRefreshLocal_noMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        long t1 = sourceFolder.getModificationStamp();
        sourceFolder.touch(null);

        testProject.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);

        assertThat(t1 != sourceFolder.getModificationStamp(), is(true));
    }

    @Test
    public void testRefreshLocal_monitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        long t1 = sourceFolder.getModificationStamp();
        sourceFolder.touch(new NullProgressMonitor());

        testProject.refreshLocal(AResourceTreeTraversalDepth.INFINITE, new NullProgressMonitor());

        assertThat(t1 != sourceFolder.getModificationStamp(), is(true));
    }

    @Test
    public void testGetLocation() {
        assertThat(testProject.getFolder("src").getLocation(), is(testProject.getLocation().resolve("src")));
    }

    @Test
    public void testGetWorkspaceRelativePath() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");

        assertThat(sourceFolder.getWorkspaceRelativePath(),
                is(Path.of("TestProject", "src", "main", "java")));
    }

    @Test
    public void testDelete_folder() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        toDelete.create(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true));

        toDelete.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false));
    }

    @Test
    public void testDelete_recursive() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        toDelete.create(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true));

        sourceFolder.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false));
        assertThat(testProject.getLocation().resolve("src/main/java").toFile().exists(), is(false));
    }

    @Test
    public void testDelete_file() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFile toDelete = sourceFolder.getFile("deleteMe");
        toDelete.create(writeTo("Content"), null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true));

        toDelete.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false));
    }

    @Test
    public void testDelete_folderWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        ((PlainJavaFolder)toDelete).create();

        Path pathToDeletedFolder = testProject.getLocation().resolve("src/main/java/deleteMe");
        assertThat(pathToDeletedFolder.toFile().exists(), is(true));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        toDelete.delete(monitor);

        assertThat(pathToDeletedFolder.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFolder));
    }

    @Test
    public void testDelete_recursiveWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        ((PlainJavaFolder)toDelete).create();

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        sourceFolder.delete(monitor);

        Path pathToDeletedFolder = testProject.getLocation().resolve("src/main/java");
        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false));
        assertThat(pathToDeletedFolder.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(2));
        assertThat(monitor.getWork(), is(2));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFolder));
    }

    @Test
    public void testDelete_fileWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFile toDelete = sourceFolder.getFile("deleteMe");
        ((PlainJavaFile)toDelete).create();

        Path pathToDeletedFile = testProject.getLocation().resolve("src/main/java/deleteMe");
        assertThat(pathToDeletedFile.toFile().exists(), is(true));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        toDelete.delete(monitor);

        assertThat(pathToDeletedFile.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFile));
    }

    @Test
    public void testCopy_file() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFile aFile = sourceFolder.getFile("newFile");
        aFile.create(writeTo("Content"), null);

        aFile.copy(Path.of("copyFile"), null);

        assertThat(sourceFolder.getLocation().resolve("copyFile").toFile().exists(), is(true));
    }

    @Test
    public void testCopy_folder() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder aFolder = srcFolder.getFolder("newFolder");
        aFolder.create(null);

        aFolder.copy(Path.of("copyFolder"), null);

        assertThat(srcFolder.getLocation().resolve("copyFolder").toFile().exists(), is(true));
    }

    @Test
    public void testCopy_fileWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFile aFile = sourceFolder.getFile("newFile");
        aFile.create(writeTo("Content"), null);

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        aFile.copy(Path.of("copyFile"), monitor);

        assertThat(sourceFolder.getLocation().resolve("copyFile").toFile().exists(), is(true));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Copying " + aFile.getLocation()));

    }

    @Test
    public void testCopy_folderWithMonitor() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFolder aFolder = srcFolder.getFolder("newFolder");
        aFolder.create(null);

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        aFolder.copy(Path.of("copyFolder"), monitor);

        assertThat(srcFolder.getLocation().resolve("copyFolder").toFile().exists(), is(true));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Copying " + aFolder.getLocation()));
    }

    @Test
    public void testMarkers() {
        AMarker marker = testProject.createMarker("Marker_ID");

        assertThat(marker, is(notNullValue()));
        assertThat(marker.unwrap(), is(instanceOf(PlainJavaMarkerImpl.class)));
    }

    @Test
    public void testFindMarkers() {
        AMarker marker = testProject.createMarker("Marker_ID");

        assertThat(testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS),
                is(hasItems(marker)));
    }

    @Test
    public void testDeleteMarkers() {
        AProject testProject = newAbstractionProject("TestProject2");
        AMarker marker = testProject.createMarker("Marker_ID");

        assertThat(testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS),
                is(hasItems(marker)));

        testProject.deleteMarkers("Marker_ID", true, AResourceTreeTraversalDepth.INFINITE);

        assertThat(
                testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS)
                        .isEmpty(),
                is(true));
    }
}
