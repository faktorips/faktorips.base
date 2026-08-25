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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

public class MavenProjectRefreshTask extends AbstractIpsTask {

    private boolean offline = false;
    private boolean updateSnapshots = false;
    private boolean updateConfiguration = true;
    private boolean refreshFromFilesystem = true;
    private boolean cleanProjects = true;

    public MavenProjectRefreshTask() {
        super("MavenProjektRefreshTask");
    }

    /**
     * Offline.
     * <p>
     * Same as: -o,--offline Work offline
     *
     * @return default is {@code false}
     */
    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    /**
     * Force Update of Snapshots/Release.
     * <p>
     * Same as: -U,--update-snapshots Forces a check for missing releases and updated snapshots on
     * remote repositories
     *
     * @return default is {@code false}
     */
    public boolean isUpdateSnapshots() {
        return updateSnapshots;
    }

    public void setUpdateSnapshots(boolean updateSnapshots) {
        this.updateSnapshots = updateSnapshots;
    }

    /**
     * Update project configuration from pom.xml.
     *
     * @return default is {@code true}
     */
    public boolean isUpdateConfiguration() {
        return updateConfiguration;
    }

    public void setUpdateConfiguration(boolean updateConfiguration) {
        this.updateConfiguration = updateConfiguration;
    }

    /**
     * Refresh workspace resources from local file system.
     *
     * @return default is {@code true}
     */
    public boolean isRefreshFromFilesystem() {
        return refreshFromFilesystem;
    }

    public void setRefreshFromFilesystem(boolean refreshFromFilesystem) {
        this.refreshFromFilesystem = refreshFromFilesystem;
    }

    /**
     * Clean projects.
     */
    public boolean isCleanProjects() {
        return cleanProjects;
    }

    public void setCleanProjects(boolean cleanProjects) {
        this.cleanProjects = cleanProjects;
    }

    @Override
    protected void executeInternal() throws Exception {
        IMavenProjectRegistry projectRegistry = waitForService(
                MavenPlugin::getMavenProjectRegistry, "IMavenProjectRegistry");
        List<IProject> projects = projectRegistry
                .getProjects().stream()
                .map(IMavenProjectFacade::getProject)
                .peek(p -> System.out.println("refreshing: " + p.getName()))
                .toList();

        System.out.println("calling updateProjectConfiguration");
        MavenProjectRefreshUtil.refresh(this, projects,
                isOffline(), isUpdateSnapshots(), isUpdateConfiguration(), isCleanProjects(),
                isRefreshFromFilesystem());

        checkForImportErrors();
    }

    private void checkForImportErrors() throws Exception {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            for (IMarker marker : markers) {
                Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
                if (severity != null && severity == IMarker.SEVERITY_ERROR) {
                    fail("Import/Refresh of project " + project.getName() + " resulted in errors: "
                            + marker.getAttribute(IMarker.MESSAGE, "unknown error"));
                    return;
                }
            }
        }
    }
}
