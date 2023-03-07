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
            IProjectConfigurationManager projectConfigManager = MavenPlugin.getProjectConfigurationManager();
            LocalProjectScanner scanner = new LocalProjectScanner(
                    List.of(getDir()),
                    false,
                    MavenPlugin.getMavenModelManager());
            scanner.run(monitor);

            Set<MavenProjectInfo> projectSet = projectConfigManager.collectProjects(scanner.getProjects());
            if (projectSet.isEmpty()) {
                System.out.println("No Maven-Projects found in: " + getDir());
                return;
            }

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

            Set<String> mavenProjectNames = projectSet.stream()
                    .map(p -> p.getModel().getArtifactId())
                    .collect(Collectors.toSet());
            long startTime = System.currentTimeMillis();
            while (!mavenProjectNames.isEmpty() && startTime + timeout > System.currentTimeMillis()) {
                System.out.println("Waiting for project(s) to be present in workspace: " + mavenProjectNames);
                Set<String> projectNamesInWorkspace = Arrays
                        .stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                        .map(IProject::getName)
                        .collect(Collectors.toSet());
                mavenProjectNames.removeAll(projectNamesInWorkspace);
            }
        }
    }
}
