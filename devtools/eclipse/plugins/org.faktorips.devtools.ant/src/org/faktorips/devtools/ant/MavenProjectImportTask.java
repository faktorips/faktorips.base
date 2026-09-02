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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;

public class MavenProjectImportTask extends AbstractIpsTask {

    private static final String POM_FILE = "pom.xml";
    private static final String IMPORT_LOCK_DIR = "org.faktorips.import-locks";
    private static final long POLL_INTERVAL_MS = 200L;
    private static final long LOCK_POLL_INTERVAL_MS = 500L;

    /**
     * Monitors per lock file, preventing {@link java.nio.channels.OverlappingFileLockException}
     * when two tasks of the same JVM import the same directory.
     */
    private static final ConcurrentMap<String, Object> JVM_LOCKS = new ConcurrentHashMap<>();

    private String projectDir;
    private long timeout = 5 * 60 * 1000L;
    private long lockTimeout = 10 * 60 * 1000L;

    public MavenProjectImportTask() {
        super("MavenProjektImportTask");
    }

    /**
     * Sets the Ant attribute which describes the location of the maven project to import.
     *
     * @param dir Path to the Project as String
     */
    public void setDir(String dir) {
        projectDir = dir;
    }

    /**
     * Returns the path of the maven project to import as String
     *
     * @return Path as String
     */
    public String getDir() {
        return projectDir;
    }

    /**
     * Sets the timeout used to wait for the project import to finish. Defaults to 5 minutes.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the timeout used to wait for another process importing the same directory. Defaults to
     * 10 minutes.
     */
    public void setLockTimeout(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    @Override
    protected void executeInternal() throws Exception {
        checkDir();
        if (!new File(getDir(), POM_FILE).exists()) {
            System.out.println("Skipping import, no " + POM_FILE + " found in: " + getDir());
            return;
        }
        Path lockFile = importLockFile();
        Path lockFileDir = lockFile.getParent();
        if (lockFileDir != null) {
            Files.createDirectories(lockFileDir);
        }
        synchronized (jvmLockFor(lockFile)) {
            try (FileChannel lockChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                FileLock lock = acquireImportLock(lockChannel, lockFile);
                if (lock == null) {
                    return;
                }
                try {
                    importWithRetry();
                } finally {
                    lock.release();
                }
            }
        }
    }

    /**
     * Imports the project, retrying once if the first attempt left the workspace in an inconsistent
     * state. A single retry is enough because {@link #removeStaleRegistrations(IProgressMonitor)}
     * has removed the inconsistent registrations by then, so the retry performs a real import
     * instead of reusing them.
     */
    private void importWithRetry() throws Exception {
        List<String> problems = importOnce();
        if (!problems.isEmpty()) {
            System.out.println("import did not complete, retrying once; problems in first try were: " + problems);
            problems = importOnce();
        }
        if (!problems.isEmpty()) {
            fail("Import of " + getDir() + " failed: " + String.join("; ", problems)
                    + ". The Eclipse workspace at " + ResourcesPlugin.getWorkspace().getRoot().getLocation()
                    + " is inconsistent; delete it and run the build again.");
        }
    }

    /**
     * Runs one full import cycle and returns a description of everything that did not work out, so
     * an empty list means success.
     */
    private List<String> importOnce() throws Exception {
        var monitor = new NullProgressMonitor();
        var projectConfigManager = waitForService(MavenPlugin::getProjectConfigurationManager,
                "IProjectConfigurationManager");
        var scanner = new LocalProjectScanner(
                List.of(getDir()),
                false,
                waitForService(MavenPlugin::getMavenModelManager, "IMavenModelManager"));
        System.out.println("running ProjectScanner");
        scanner.run(monitor);

        Set<MavenProjectInfo> projectSet = projectConfigManager.collectProjects(scanner.getProjects());
        if (projectSet.isEmpty()) {
            System.out.println("No Maven-Projects found in: " + getDir());
            return List.of();
        }

        removeStaleRegistrations(monitor);

        System.out.println("running importProjects");
        List<IMavenProjectImportResult> importResults;
        try {
            importResults = projectConfigManager.importProjects(
                    projectSet,
                    new ProjectImportConfiguration(),
                    monitor);
        } catch (CoreException e) {
            // m2e leaves half-configured projects behind when it fails; report the problem so that
            // the caller can repair the workspace and try again instead of aborting the build.
            e.printStackTrace();
            return List.of("m2e could not import " + getDir() + " (" + e.getMessage() + ")");
        }

        List<File> projectDirs = new ArrayList<>();
        for (IMavenProjectImportResult result : importResults) {
            File dir = directoryOf(result.getMavenProjectInfo());
            projectDirs.add(dir);
            if (result.getProject() instanceof IProject project) {
                System.out.println("importing: " + project.getName());
            } else {
                System.out.println("already in workspace: " + dir);
            }
        }

        List<String> problems = new ArrayList<>(waitForProjects(projectDirs));

        waitForBuildJobs();

        problems.addAll(openClosedProjects(projectDirs, monitor));
        return problems;
    }

    /**
     * Removes workspace registrations that no longer match the file system, so that m2e performs a
     * real import instead of reporting the project as already present. This happens when the
     * Eclipse workspace is reused across builds while the project's {@code .project} file - which
     * is usually not checked in - is not.
     * <p>
     * A project that is open is left alone even without a {@code .project} file: its description is
     * already in memory, and deleting the registration of a project someone else may be importing
     * concurrently would do more harm than good.
     */
    void removeStaleRegistrations(IProgressMonitor monitor) throws CoreException {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            IPath location = project.getLocation();
            if (location == null) {
                System.out.println("removing workspace registration without location: " + project.getName());
            } else if (!location.toFile().isDirectory()) {
                System.out.println("removing workspace registration for missing directory " + location + ": "
                        + project.getName());
            } else if (!project.isOpen() && !descriptionFile(location.toFile()).exists()) {
                System.out.println("removing workspace registration of closed project without "
                        + IProjectDescription.DESCRIPTION_FILE_NAME + ": " + project.getName());
            } else {
                continue;
            }
            project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT | IResource.FORCE, monitor);
        }
    }

    /**
     * Waits until every imported directory is registered in the workspace. The projects are matched
     * by location instead of by name, because the Eclipse project name is derived from m2e's name
     * template and need not equal the Maven artifactId.
     */
    private List<String> waitForProjects(List<File> projectDirs) throws InterruptedException {
        Set<File> pending = new LinkedHashSet<>(projectDirs);
        long deadline = System.currentTimeMillis() + timeout;
        while (!pending.isEmpty() && System.currentTimeMillis() < deadline) {
            System.out.println("Waiting for project(s) to be present in workspace: " + pending);
            pending.removeIf(dir -> findProjectAt(dir) != null);
            if (!pending.isEmpty()) {
                Thread.sleep(POLL_INTERVAL_MS);
            }
        }
        if (pending.isEmpty()) {
            return List.of();
        }
        return List.of("timed out after " + timeout + "ms waiting for the project(s) in " + pending
                + " to be present in the workspace");
    }

    /**
     * After m2e import, projects can be registered in the workspace but still in a closed state
     * (e.g. when a previous build was aborted). Opens any such projects explicitly. A project that
     * cannot be opened is removed from the workspace metadata so that the caller's retry can import
     * it from scratch.
     */
    private List<String> openClosedProjects(List<File> projectDirs, IProgressMonitor monitor)
            throws InterruptedException, CoreException {
        List<String> problems = new ArrayList<>();
        for (File dir : projectDirs) {
            IProject project = findProjectAt(dir);
            if (project == null || project.isOpen()) {
                continue;
            }
            System.out.println("project in workspace but not open, opening: " + project.getName());
            try {
                project.open(monitor);
            } catch (CoreException e) {
                System.out.println("could not open " + project.getName() + ": " + e.getMessage()
                        + ", removing the inconsistent workspace registration");
                project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT | IResource.FORCE, monitor);
                problems.add("project " + project.getName() + " in " + dir + " could not be opened ("
                        + e.getMessage() + ")");
            }
        }
        waitForBuildJobs();
        return problems;
    }

    IProject findProjectAt(File dir) {
        Path canonicalDir = canonicalize(dir);
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            IPath location = project.getLocation();
            if (location != null && canonicalDir.equals(canonicalize(location.toFile()))) {
                return project;
            }
        }
        return null;
    }

    private static File directoryOf(MavenProjectInfo projectInfo) {
        return projectInfo.getPomFile().getParentFile();
    }

    private static File descriptionFile(File projectDir) {
        return new File(projectDir, IProjectDescription.DESCRIPTION_FILE_NAME);
    }

    private static Path canonicalize(File file) {
        try {
            return file.getCanonicalFile().toPath();
        } catch (IOException e) {
            return file.getAbsoluteFile().toPath();
        }
    }

    /**
     * Acquires the lock guarding {@link #getDir()} against concurrent imports, or {@code null} if
     * the task has already been {@link #fail(String) failed} because the lock could not be acquired
     * in time.
     * <p>
     * A separate lock per imported directory is needed because m2e writes {@code .project} and
     * {@code .classpath} into the imported project's own directory. With a parallel Maven build
     * ({@code -T}) several Eclipse processes import the same upstream module, and without this lock
     * they overwrite those files while another process is reading them.
     */
    private FileLock acquireImportLock(FileChannel lockChannel, Path lockFile)
            throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + lockTimeout;
        while (true) {
            FileLock lock = lockChannel.tryLock();
            if (lock != null) {
                return lock;
            }
            if (System.currentTimeMillis() >= deadline) {
                fail("Timed out after " + lockTimeout + "ms waiting for the import lock " + lockFile
                        + ". Another process is importing " + getDir() + " concurrently.");
                return null;
            }
            System.out.println("waiting for another process importing " + getDir());
            Thread.sleep(LOCK_POLL_INTERVAL_MS);
        }
    }

    /**
     * The lock file is placed in the temp directory rather than in the imported project, because
     * the projects using this task check their working tree with {@code git status} and would fail
     * the build for an unexpected file.
     */
    Path importLockFile() {
        String digest = sha1Hex(canonicalize(new File(getDir())).toString());
        return Path.of(System.getProperty("java.io.tmpdir"), IMPORT_LOCK_DIR, digest + ".lock");
    }

    private static String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }

    private static Object jvmLockFor(Path lockFile) {
        return JVM_LOCKS.computeIfAbsent(lockFile.toString(), k -> new Object());
    }

    /**
     * @throws BuildException if {@link #getDir() dir} is not a readable directory.
     */
    private void checkDir() {
        if (getDir() == null || getDir().isBlank()) {
            throw new BuildException("Please provide the 'dir' attribute.");
        }
        File dir = new File(getDir());
        if (!dir.exists()) {
            throw new BuildException("Directory " + getDir() + " doesn't exist.");
        }
        if (!dir.isDirectory()) {
            throw new BuildException("Provided 'dir' " + getDir() + " is not a Directory.");
        }
        if (!dir.canRead()) {
            throw new BuildException("Provided 'dir' " + getDir() + " is not readable.");
        }
    }
}
