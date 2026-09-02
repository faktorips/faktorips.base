/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.Test;

/**
 * Tests the recovery of workspace registrations that no longer match the file system. The actual
 * m2e import is not exercised here: it needs an active Maven embedder, and the multi-process race
 * the import lock guards against is only observable in a parallel CI build.
 */
public class MavenProjectImportTaskTest extends AbstractIpsPluginTest {

    private final MavenProjectImportTask task = new MavenProjectImportTask();

    @Test
    public void testRemoveStaleRegistrations_ClosedProjectWithoutDescriptionFile() throws Exception {
        IProject project = newEclipseProject();
        File location = project.getLocation().toFile();
        project.close(new NullProgressMonitor());
        Files.delete(descriptionFile(location));

        task.removeStaleRegistrations(new NullProgressMonitor());

        assertFalse(project.exists(), "the inconsistent registration should have been removed");
        assertTrue(location.isDirectory(), "the project content must be kept on disk");
    }

    @Test
    public void testRemoveStaleRegistrations_ClosedProjectWithDescriptionFile() throws Exception {
        IProject project = newEclipseProject();
        project.close(new NullProgressMonitor());

        task.removeStaleRegistrations(new NullProgressMonitor());

        assertTrue(project.exists(), "a closed project with a .project file is consistent");
    }

    @Test
    public void testRemoveStaleRegistrations_OpenProjectWithoutDescriptionFile() throws Exception {
        IProject project = newEclipseProject();
        Files.delete(descriptionFile(project.getLocation().toFile()));

        task.removeStaleRegistrations(new NullProgressMonitor());

        assertTrue(project.exists(), "an open project already has its description in memory");
    }

    @Test
    public void testRemoveStaleRegistrations_MissingProjectDirectory() throws Exception {
        IProject project = newEclipseProject();
        File location = project.getLocation().toFile();
        project.close(new NullProgressMonitor());
        deleteRecursively(location.toPath());

        task.removeStaleRegistrations(new NullProgressMonitor());

        assertFalse(project.exists());
    }

    @Test
    public void testFindProjectAt() throws Exception {
        IProject project = newEclipseProject();

        assertThat(task.findProjectAt(project.getLocation().toFile()), is(project));
        assertThat(task.findProjectAt(new File(project.getLocation().toFile(), "no-project-here")),
                is(nullValue()));
    }

    @Test
    public void testImportLockFile_OutsideProjectDirectoryAndDeterministic() {
        task.setDir("/some/project");
        Path lockFile = task.importLockFile();
        Path sameAgain = task.importLockFile();

        assertThat(lockFile, is(sameAgain));
        assertFalse(lockFile.startsWith(Path.of("/some/project")),
                "the lock file must not pollute the imported project's working tree");

        task.setDir("/another/project");
        assertThat(task.importLockFile(), is(not(lockFile)));
    }

    @Test
    public void testExecuteInternal_WithoutPomXml() throws Exception {
        IProject project = newEclipseProject();
        task.setDir(project.getLocation().toFile().toString());

        task.executeInternal();

        assertFalse(Files.exists(task.importLockFile()), "no lock is needed when there is nothing to import");
    }

    @Test
    public void testExecuteInternal_MissingDir() {
        task.setDir(new File(System.getProperty("java.io.tmpdir"), "does-not-exist-" + System.nanoTime()).toString());

        assertThrows(BuildException.class, task::executeInternal);
    }

    @Test
    public void testExecuteInternal_WithoutDir() {
        assertThrows(BuildException.class, task::executeInternal);
    }

    private IProject newEclipseProject() {
        IIpsProject ipsProject = newIpsProject();
        return ResourcesPlugin.getWorkspace().getRoot().getProject(ipsProject.getName());
    }

    private static Path descriptionFile(File projectDir) {
        return projectDir.toPath().resolve(IProjectDescription.DESCRIPTION_FILE_NAME);
    }

    private static void deleteRecursively(Path path) throws Exception {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not delete " + p, e);
                }
            });
        }
    }
}
