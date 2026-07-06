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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectImportResult;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.LocalProjectScanner;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;

public class MavenProjectImportTask extends AbstractIpsTask {

    private String projectDir;
    private long timeout = 5 * 60 * 1000L;

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

    @Override
    protected void executeInternal() throws Exception {
        NullProgressMonitor monitor = new NullProgressMonitor();

        if (new File(getDir(), "pom.xml").exists()) {
            IProjectConfigurationManager projectConfigManager = waitForService(
                    MavenPlugin::getProjectConfigurationManager, "IProjectConfigurationManager", timeout);
            LocalProjectScanner scanner = new LocalProjectScanner(
                    List.of(getDir()),
                    false,
                    waitForService(MavenPlugin::getMavenModelManager, "IMavenModelManager", timeout));
            System.out.println("running ProjectScanner");
            scanner.run(monitor);

            Set<MavenProjectInfo> projectSet = projectConfigManager.collectProjects(scanner.getProjects());
            if (projectSet.isEmpty()) {
                System.out.println("No Maven-Projects found in: " + getDir());
                return;
            }
            System.out.println("running importProjects");
            ProjectImportConfiguration configuration = new ProjectImportConfiguration();
            List<IMavenProjectImportResult> importResults = projectConfigManager.importProjects(
                    projectSet,
                    configuration,
                    monitor);

            for (IMavenProjectImportResult result : importResults) {
                if (result.getProject() != null) {
                    System.out.println("importing: " + result.getProject().getName());
                } else {
                    System.out.println("already in workspace: " + getDir());
                }
            }

            Set<String> importedProjectNames = projectSet.stream()
                    .map(p -> p.getModel().getArtifactId())
                    .collect(Collectors.toSet());
            Set<String> pendingProjectNames = new HashSet<>(importedProjectNames);
            long startTime = System.currentTimeMillis();
            while (!pendingProjectNames.isEmpty() && startTime + timeout > System.currentTimeMillis()) {
                System.out.println("Waiting for project(s) to be present in workspace: " + pendingProjectNames);
                Set<String> projectNamesInWorkspace = Arrays
                        .stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                        .map(IProject::getName)
                        .collect(Collectors.toSet());
                pendingProjectNames.removeAll(projectNamesInWorkspace);
                if (!pendingProjectNames.isEmpty()) {
                    Thread.sleep(200);
                }
            }
            if (!pendingProjectNames.isEmpty()) {
                System.out.println("WARNING: Timed out waiting for projects: " + pendingProjectNames);
            }

            waitForBuildJobs();

            openClosedProjects(importedProjectNames);
        }
    }

    /**
     * After m2e import, projects can be registered in the workspace but still in a closed state
     * (e.g. when a previous build was aborted). Opens any such projects explicitly.
     */
    private void openClosedProjects(Set<String> projectNames) throws Exception {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if (projectNames.contains(project.getName()) && project.exists() && !project.isOpen()) {
                System.out.println("project in workspace but not open, opening: " + project.getName());
                project.open(new NullProgressMonitor());
            }
        }
        waitForBuildJobs();
    }
}
