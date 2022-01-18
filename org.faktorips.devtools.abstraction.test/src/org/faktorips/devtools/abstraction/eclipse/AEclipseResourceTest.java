/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class AEclipseResourceTest extends AEclipseAbstractionTestSetup {

    private AProject testProject1;
    private IProject eclipseProject;

    @Before
    public void setUp() {
        testProject1 = newSimpleIpsProject("testProject");
        eclipseProject = testProject1.unwrap();
    }

    @Test
    public void testAEclipseResource() {
        assertThat(Abstractions.getWorkspace().getRoot().unwrap(), is(instanceOf(IResource.class)));
        assertThat(testProject1.unwrap(), is(instanceOf(IResource.class)));
        assertThat(testProject1.getFile(".ipsproject").unwrap(), is(instanceOf(IResource.class)));
        assertThat(testProject1.getFolder("src").unwrap(), is(instanceOf(IResource.class)));
    }

    @Test
    public void testGetAdapter() {
        AResource folder = testProject1.getFolder("src");
        IFolder adapter = folder.getAdapter(IFolder.class);

        assertThat(adapter, is(notNullValue()));
    }

    @Test
    public void testIsAccessible() {
        assertThat(testProject1.getFolder("src").isAccessible(), is(true));
    }

    @Test
    public void testGetParent() {
        assertThat(testProject1.getFolder("src").getParent(), is(testProject1));
    }

    @Test
    public void testGetProjectRelativePath() {
        AFolder sourceFolder = testProject1.getFolder("src");

        assertThat(sourceFolder.getProjectRelativePath(), is(Path.of("src")));
    }

    @Test
    public void testRefreshLocal() throws IOException {
        AFolder srcFolder = testProject1.getFolder("src");
        FileUtils.write(srcFolder.getLocation().resolve("nonEclipseAddedFile").toFile(), "Content",
                Charset.defaultCharset());

        assertThat(eclipseProject.getFolder("src").getFile("nonEclipseAddedFile").exists(), is(false));

        testProject1.refreshLocal(AResourceTreeTraversalDepth.INFINITE, new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFile("nonEclipseAddedFile").exists(), is(true));
    }

    @Test
    public void testGetLocation() {
        assertThat(testProject1.getFolder("src").getLocation(),
                is(Abstractions.getWorkspace().getRoot().getLocation().resolve("testProject").resolve("src")));
    }

    @Test
    public void testGetWorkspaceRelativePath() {
        AFolder sourceFolder = testProject1.getFolder("src");

        assertThat(sourceFolder.getWorkspaceRelativePath(), is(Path.of("/testProject", "src")));
    }

    @Test
    public void testDelete_folder() {
        AFolder sourceFolder = testProject1.getFolder("src");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        toDelete.create(new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFolder("deleteMe").exists(), is(true));

        toDelete.delete(new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFolder("deleteMe").exists(), is(false));
    }

    @Test
    public void testDelete_recursive() {
        AFolder sourceFolder = testProject1.getFolder("src");
        AFolder toDelete = sourceFolder.getFolder("deleteMe");
        toDelete.create(new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFolder("deleteMe").exists(), is(true));

        sourceFolder.delete(new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFolder("deleteMe").exists(), is(false));
        assertThat(eclipseProject.getFolder("src").exists(), is(false));
    }

    @Test
    public void testDelete_file() {
        AFolder sourceFolder = testProject1.getFolder("src");
        AFile toDelete = sourceFolder.getFile("deleteMe");
        toDelete.create(writeTo("Content"), new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFile("deleteMe").exists(), is(true));

        sourceFolder.delete(new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFile("deleteMe").exists(), is(false));
    }

    @Test
    public void testGetModificationStamp() {
        AFile aFile = testProject1.getFolder("src").getFile("newFile");

        assertThat(aFile.getModificationStamp(), is(Long.valueOf(IResource.NULL_STAMP)));

        aFile.create(writeTo("Content"), new NullProgressMonitor());

        long t2 = aFile.getModificationStamp();

        assertThat(t2 > 0L, is(true));
    }

    @Test
    public void testGetLocalTimeStamp() {
        AFile aFile = testProject1.getFolder("src").getFile("newFile");

        assertThat(aFile.getLocalTimeStamp(), is(Long.valueOf(IResource.NULL_STAMP)));

        aFile.create(writeTo("Content"), new NullProgressMonitor());

        long t2 = aFile.getLocalTimeStamp();

        assertThat(t2 > 0L, is(true));
    }

    @Test
    public void testDeleteMarker() {
        testProject1.createMarker("TestMarker");

        testProject1.deleteMarkers("TestMarker", true, AResourceTreeTraversalDepth.INFINITE);

        assertThat(testProject1.findMarkers("TestMarker", true, AResourceTreeTraversalDepth.INFINITE).isEmpty(),
                is(true));
    }

    @Test
    public void testFindMarker() {
        AMarker marker = testProject1.createMarker("TestMarker");

        assertThat(testProject1.findMarkers("TestMarker", true, AResourceTreeTraversalDepth.INFINITE),
                is(hasItems(marker)));
    }

    @Test
    public void testCreateMarker() {
        AMarker marker = testProject1.createMarker("TestMarker");

        assertThat(marker, is(notNullValue()));
    }

    @Test
    public void testDerived() {
        AFile aFile = testProject1.getFolder("src").getFile("newFile");
        aFile.create(writeTo("Content"), new NullProgressMonitor());

        assertThat(aFile.isDerived(), is(false));

        aFile.setDerived(true, new NullProgressMonitor());

        assertThat(aFile.isDerived(), is(true));
    }

    @Test
    public void testGetWorkspace() {
        AWorkspace ws = Abstractions.getWorkspace();
        AFolder aFolder = testProject1.getFolder("src");
        AFile aFile = testProject1.getFile(".ipsproject");

        assertThat(Abstractions.getWorkspace().getRoot().getWorkspace(), is(sameInstance(ws)));
        assertThat(testProject1.getWorkspace(), is(sameInstance(ws)));
        assertThat(aFolder.getWorkspace(), is(sameInstance(ws)));
        assertThat(aFile.getWorkspace(), is(sameInstance(ws)));
    }

    @Test
    public void testCopy_file() {
        AFolder srcFolder = testProject1.getFolder("src");
        AFile aFile = srcFolder.getFile("newFile");
        aFile.create(writeTo("Content"), new NullProgressMonitor());

        aFile.copy(Path.of("copyFile"), new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFile("copyFile").exists(), is(true));
    }

    @Test
    public void testCopy_folder() {
        AFolder srcFolder = testProject1.getFolder("src");
        AFolder aFolder = srcFolder.getFolder("newFolder");
        aFolder.create(new NullProgressMonitor());

        aFolder.copy(Path.of("copyFolder"), new NullProgressMonitor());

        assertThat(eclipseProject.getFolder("src").getFolder("copyFolder").exists(), is(true));
    }

    @Test
    public void testTouch() throws InterruptedException {
        AFile aFile = testProject1.getFolder("src").getFile("newFile");
        aFile.create(writeTo("Content"), new NullProgressMonitor());
        long t1 = aFile.getModificationStamp();

        Thread.sleep(500);

        aFile.touch(new NullProgressMonitor());

        assertThat(t1 != aFile.getModificationStamp(), is(true));
    }

    @Test
    public void testIsSynchronized() throws IOException {
        assertThat(testProject1.isSynchronized(AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS), is(true));

        testProject1.getLocation().resolve("nonEclipseAddedFile").toFile().createNewFile();

        assertThat(testProject1.isSynchronized(AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS), is(false));
    }
}
