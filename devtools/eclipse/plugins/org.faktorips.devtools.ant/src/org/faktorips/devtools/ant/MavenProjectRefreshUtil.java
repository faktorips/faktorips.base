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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;

/**
 * Checks m2e's {@link IMavenProjectRegistry} for projects with unresolved Maven dependencies and
 * triggers a project configuration update to resolve them. Shared by
 * {@link MavenProjectRefreshTask} (refreshes the whole workspace) and {@link BuildTask} (refreshes
 * only the projects whose dependencies failed to resolve).
 */
final class MavenProjectRefreshUtil {

    private MavenProjectRefreshUtil() {
        // utility class
    }

    /**
     * Triggers an m2e project configuration update for the given projects and waits for the
     * resulting build/refresh jobs to finish.
     *
     * @param task the calling task, used to access its
     *            {@code waitForService}/{@code waitForBuildJobs} helpers
     * @param projects the projects to refresh
     */
    static void refresh(AbstractIpsTask task,
            List<IProject> projects,
            boolean offline,
            boolean updateSnapshots,
            boolean updateConfiguration,
            boolean cleanProjects,
            boolean refreshFromFilesystem) throws CoreException, InterruptedException {
        IProjectConfigurationManager pm = task.waitForService(
                MavenPlugin::getProjectConfigurationManager, "IProjectConfigurationManager");
        Map<String, IStatus> updateProjectStatus = updateProjectConfiguration(pm, projects, offline, updateSnapshots,
                updateConfiguration, cleanProjects, refreshFromFilesystem);
        updateProjectStatus.forEach((p, s) -> System.out.println(p + ": " + s));
        task.waitForBuildJobs();
    }

    @SuppressWarnings("restriction")
    private static Map<String, IStatus> updateProjectConfiguration(IProjectConfigurationManager configurationManager,
            List<IProject> projects,
            boolean offline,
            boolean updateSnapshots,
            boolean updateConfiguration,
            boolean cleanProjects,
            boolean refreshFromFilesystem) {
        MavenUpdateRequest mavenUpdateRequest = new MavenUpdateRequest(projects, offline, updateSnapshots);
        return ((org.eclipse.m2e.core.internal.project.ProjectConfigurationManager)configurationManager)
                .updateProjectConfiguration(mavenUpdateRequest, updateConfiguration, cleanProjects,
                        refreshFromFilesystem, new NullProgressMonitor());
    }

    /**
     * Waits until all projects in the m2e registry have their Maven dependencies resolved. This
     * guards against the race condition where m2e's async dependency-resolution jobs finish after
     * {@code task.waitForBuildJobs()} returns, leaving projects with an empty classpath when the
     * IPS builder runs.
     * <p>
     * On each attempt, waits up to {@code resolutionTimeoutMs} for dependencies to resolve on their
     * own. If they are still unresolved afterwards, an explicit Maven project refresh is triggered
     * for just the affected projects (instead of the whole workspace) and the wait is repeated, up to
     * {@code maxRefreshAttempts} times before giving up.
     *
     * @param task the calling task, used to access its
     *            {@code waitForService}/{@code waitForBuildJobs} helpers
     * @param resolutionTimeoutMs how long to wait for dependencies to resolve on their own, per
     *            attempt
     * @param maxRefreshAttempts maximum number of refresh attempts before giving up
     */
    static void waitForDependenciesResolved(AbstractIpsTask task, long resolutionTimeoutMs, int maxRefreshAttempts)
            throws CoreException, InterruptedException {
        IMavenProjectRegistry registry = task.waitForService(
                MavenPlugin::getMavenProjectRegistry, "IMavenProjectRegistry");
        List<IMavenProjectFacade> unresolved = waitForResolution(registry, resolutionTimeoutMs);
        for (int attempt = 1; !unresolved.isEmpty() && attempt <= maxRefreshAttempts; attempt++) {
            System.out.println("Maven dependencies not resolved for: "
                    + unresolved.stream().map(f -> f.getProject().getName()).collect(Collectors.joining(", "))
                    + " - refreshing (attempt " + attempt + "/" + maxRefreshAttempts + ")");
            refresh(task, unresolved.stream().map(IMavenProjectFacade::getProject).toList(),
                    false, false, true, true, true);
            unresolved = waitForResolution(registry, resolutionTimeoutMs);
        }
        if (!unresolved.isEmpty()) {
            logMavenDebugInfo(registry);
            throw new BuildException("Timed out waiting for Maven dependencies to be resolved for: "
                    + unresolved.stream().map(f -> f.getProject().getName()).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Polls {@link #findUnresolvedProjects(IMavenProjectRegistry)} every 500ms until either none are
     * unresolved or {@code timeoutMs} has elapsed.
     */
    private static List<IMavenProjectFacade> waitForResolution(IMavenProjectRegistry registry, long timeoutMs)
            throws CoreException, InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        List<IMavenProjectFacade> unresolved = findUnresolvedProjects(registry);
        while (!unresolved.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(500);
            unresolved = findUnresolvedProjects(registry);
        }
        return unresolved;
    }

    /**
     * Returns the projects in the registry whose Maven dependencies are not yet resolved, i.e. not
     * yet loaded by m2e, or with declared dependencies but an empty classpath
     * ({@code getArtifacts()} empty).
     */
    private static List<IMavenProjectFacade> findUnresolvedProjects(IMavenProjectRegistry registry)
            throws CoreException {
        List<IMavenProjectFacade> unresolved = new ArrayList<>();
        for (IMavenProjectFacade facade : registry.getProjects()) {
            MavenProject mp = facade.getMavenProject(new NullProgressMonitor());
            // mp == null: project not yet loaded by m2e.
            // getDependencies() empty with getArtifacts() empty means the project is a BOM or
            // aggregator/parent with no <dependencies> — those are never waiting for resolution.
            // getDependencies() non-empty with getArtifacts() empty means resolution is still
            // running and the classpath is not yet populated.
            if (mp == null || (!mp.getDependencies().isEmpty() && mp.getArtifacts().isEmpty())) {
                unresolved.add(facade);
            }
        }
        return unresolved;
    }

    private static void logMavenDebugInfo(IMavenProjectRegistry registry) throws CoreException {
        for (IProject projectDebug : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            IMavenProjectFacade projectFacade = registry.getProject(projectDebug);
            if (projectFacade != null) {
                MavenProject m = projectFacade.getMavenProject(new NullProgressMonitor());
                if (m != null) {
                    System.out.println("Dependencies of " + m.getGroupId() + ":" + m.getArtifactId() + ":"
                            + m.getVersion() + ":");
                    for (Artifact a : m.getArtifacts()) {
                        System.out.println("\t" + a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion());
                    }
                }
            }
        }
    }
}
