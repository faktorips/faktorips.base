/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
        testProject = newSimpleIpsProject("TestProject"); //$NON-NLS-1$
    }

    @Test
    public void testPlainJavaResource() {
        assertThat(Abstractions.getWorkspace().getRoot().unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.getFolder("src").unwrap(), is(instanceOf(File.class))); //$NON-NLS-1$
        assertThat(testProject.getFile("someFile").unwrap(), is(instanceOf(File.class))); //$NON-NLS-1$
    }

    @Test
    public void testIsAccessible() {
        assertThat(testProject.getFolder("src").isAccessible(), is(true)); //$NON-NLS-1$
    }

    @Test
    public void testGetParent() {
        assertThat(testProject.getFolder("src").getParent(), is(testProject)); //$NON-NLS-1$
    }

    @Test
    public void testGetProjectRelativePath() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        assertThat(sourceFolder.getProjectRelativePath(), is(Path.of("src/main/java"))); //$NON-NLS-1$
    }

    @Test
    public void testRefreshLocal_noMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        long t1 = sourceFolder.getModificationStamp();
        sourceFolder.touch(null);

        testProject.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);

        assertThat(t1 != sourceFolder.getModificationStamp(), is(true));
    }

    @Test
    public void testRefreshLocal_monitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        long t1 = sourceFolder.getModificationStamp();
        sourceFolder.touch(new NullProgressMonitor());

        testProject.refreshLocal(AResourceTreeTraversalDepth.INFINITE, new NullProgressMonitor());

        assertThat(t1 != sourceFolder.getModificationStamp(), is(true));
    }

    @Test
    public void testGetLocation() {
        assertThat(testProject.getFolder("src").getLocation(), is(testProject.getLocation().resolve("src"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetWorkspaceRelativePath() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        assertThat(sourceFolder.getWorkspaceRelativePath(),
                is(Path.of("TestProject", "src", "main", "java"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Test
    public void testDelete_folder() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder toDelete = sourceFolder.getFolder("deleteMe"); //$NON-NLS-1$
        toDelete.create(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true)); //$NON-NLS-1$

        toDelete.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testDelete_recursive() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder toDelete = sourceFolder.getFolder("deleteMe"); //$NON-NLS-1$
        toDelete.create(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true)); //$NON-NLS-1$

        sourceFolder.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false)); //$NON-NLS-1$
        assertThat(testProject.getLocation().resolve("src/main/java").toFile().exists(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testDelete_file() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFile toDelete = sourceFolder.getFile("deleteMe"); //$NON-NLS-1$
        toDelete.create(writeTo("Content"), null); //$NON-NLS-1$

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true)); //$NON-NLS-1$

        toDelete.delete(null);

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testDelete_folderWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder toDelete = sourceFolder.getFolder("deleteMe"); //$NON-NLS-1$
        ((PlainJavaFolder)toDelete).create();

        Path pathToDeletedFolder = testProject.getLocation().resolve("src/main/java/deleteMe"); //$NON-NLS-1$
        assertThat(pathToDeletedFolder.toFile().exists(), is(true));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        toDelete.delete(monitor);

        assertThat(pathToDeletedFolder.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFolder)); //$NON-NLS-1$
    }

    @Test
    public void testDelete_recursiveWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder toDelete = sourceFolder.getFolder("deleteMe"); //$NON-NLS-1$
        ((PlainJavaFolder)toDelete).create();

        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(true)); //$NON-NLS-1$

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        sourceFolder.delete(monitor);

        Path pathToDeletedFolder = testProject.getLocation().resolve("src/main/java"); //$NON-NLS-1$
        assertThat(testProject.getLocation().resolve("src/main/java/deleteMe").toFile().exists(), is(false)); //$NON-NLS-1$
        assertThat(pathToDeletedFolder.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(2));
        assertThat(monitor.getWork(), is(2));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFolder)); //$NON-NLS-1$
    }

    @Test
    public void testDelete_fileWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFile toDelete = sourceFolder.getFile("deleteMe"); //$NON-NLS-1$
        ((PlainJavaFile)toDelete).create();

        Path pathToDeletedFile = testProject.getLocation().resolve("src/main/java/deleteMe"); //$NON-NLS-1$
        assertThat(pathToDeletedFile.toFile().exists(), is(true));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        toDelete.delete(monitor);

        assertThat(pathToDeletedFile.toFile().exists(), is(false));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Deleting " + pathToDeletedFile)); //$NON-NLS-1$
    }

    @Test
    public void testCopy_file() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFile aFile = sourceFolder.getFile("newFile"); //$NON-NLS-1$
        aFile.create(writeTo("Content"), null); //$NON-NLS-1$

        aFile.copy(Path.of("copyFile"), null); //$NON-NLS-1$

        assertThat(sourceFolder.getLocation().resolve("copyFile").toFile().exists(), is(true)); //$NON-NLS-1$
    }

    @Test
    public void testCopy_folder() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder aFolder = srcFolder.getFolder("newFolder"); //$NON-NLS-1$
        aFolder.create(null);

        aFolder.copy(Path.of("copyFolder"), null); //$NON-NLS-1$

        assertThat(srcFolder.getLocation().resolve("copyFolder").toFile().exists(), is(true)); //$NON-NLS-1$
    }

    @Test
    public void testCopy_fileWithMonitor() {
        AFolder sourceFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFile aFile = sourceFolder.getFile("newFile"); //$NON-NLS-1$
        aFile.create(writeTo("Content"), null); //$NON-NLS-1$

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        aFile.copy(Path.of("copyFile"), monitor); //$NON-NLS-1$

        assertThat(sourceFolder.getLocation().resolve("copyFile").toFile().exists(), is(true)); //$NON-NLS-1$
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Copying " + aFile.getLocation())); //$NON-NLS-1$

    }

    @Test
    public void testCopy_folderWithMonitor() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AFolder aFolder = srcFolder.getFolder("newFolder"); //$NON-NLS-1$
        aFolder.create(null);

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        aFolder.copy(Path.of("copyFolder"), monitor); //$NON-NLS-1$

        assertThat(srcFolder.getLocation().resolve("copyFolder").toFile().exists(), is(true)); //$NON-NLS-1$
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(1));
        assertThat(monitor.getName(), is("Copying " + aFolder.getLocation())); //$NON-NLS-1$
    }

    @Test
    public void testMarkers() {
        AMarker marker = testProject.createMarker("Marker_ID"); //$NON-NLS-1$

        assertThat(marker, is(notNullValue()));
        assertThat(marker.unwrap(), is(instanceOf(PlainJavaMarkerImpl.class)));
    }

    @Test
    public void testFindMarkers() {
        AMarker marker = testProject.createMarker("Marker_ID"); //$NON-NLS-1$

        assertThat(testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS), //$NON-NLS-1$
                is(hasItems(marker)));
    }

    @Test
    public void testDeleteMarkers() {
        AProject testProject = newAbstractionProject("TestProject2"); //$NON-NLS-1$
        AMarker marker = testProject.createMarker("Marker_ID"); //$NON-NLS-1$

        assertThat(testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS), //$NON-NLS-1$
                is(hasItems(marker)));

        testProject.deleteMarkers("Marker_ID", true, AResourceTreeTraversalDepth.INFINITE); //$NON-NLS-1$

        assertThat(
                testProject.findMarkers("Marker_ID", true, AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS) //$NON-NLS-1$
                        .isEmpty(),
                is(true));
    }
}
