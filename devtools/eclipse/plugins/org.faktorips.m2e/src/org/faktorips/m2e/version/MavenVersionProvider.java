/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e.version;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;

/**
 * A {@link IVersionProvider} reading the version from a {@link MavenProject}'s {@code pom.xml}.
 */
public class MavenVersionProvider implements IVersionProvider<MavenVersion> {

    private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$

    private final IProject project;
    private MavenProject mavenProject;
    private final IMavenProjectChangedListener mavenUpdateListener;

    public MavenVersionProvider(IIpsProject ipsProject) {
        project = ipsProject.getProject().unwrap();
        mavenProject = findMavenProject();
        mavenUpdateListener = (events, $) -> mavenProjectChanged(events);
        MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(mavenUpdateListener);
    }

    private MavenProject findMavenProject() {
        try {
            IFile pom = project.getFile(POM_FILE_NAME);
            IMavenProjectFacade mavenProjectFacade = MavenPlugin.getMavenProjectRegistry().create(pom, true,
                    new NullProgressMonitor());
            if (mavenProjectFacade == null) {
                return null;
            }
            // need to use the version with monitor to search the project if it's not in the cache
            return mavenProjectFacade.getMavenProject(new NullProgressMonitor());
        } catch (CoreException e) {
            IpsLog.log(e);
            return null;
        }
    }

    private void mavenProjectChanged(List<MavenProjectChangedEvent> events) {
        List<MavenProjectChangedEvent> eventsForThisProject = getEventsForThisProject(events);

        if (!eventsForThisProject.isEmpty()) {
            if (isProjectDeleted(eventsForThisProject)) {
                MavenPlugin.getMavenProjectRegistry().removeMavenProjectChangedListener(mavenUpdateListener);
            } else {
                mavenProject = findMavenProject();
            }
        }
    }

    private List<MavenProjectChangedEvent> getEventsForThisProject(List<MavenProjectChangedEvent> events) {
        ArrayList<MavenProjectChangedEvent> updateEventsForThisProject = new ArrayList<>();
        for (MavenProjectChangedEvent event : events) {
            IMavenProjectFacade changedMavenProject = event.getMavenProject();
            if (changedMavenProject != null && project.equals(changedMavenProject.getProject())) {
                updateEventsForThisProject.add(event);
            }
        }
        return updateEventsForThisProject;
    }

    private boolean isProjectDeleted(List<MavenProjectChangedEvent> eventsForThisProject) {
        for (MavenProjectChangedEvent event : eventsForThisProject) {
            if (MavenProjectChangedEvent.KIND_REMOVED == event.getKind() && !project.exists()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCorrectVersionFormat(String version) {
        return MavenVersion.isCorrectVersionFormat(version);
    }

    @Override
    public String getVersionFormat() {
        return MavenVersion.getVersionFormat();
    }

    @Override
    public MavenVersion getVersion(String versionAsString) {
        return new MavenVersion(versionAsString);
    }

    @Override
    public MavenVersion getProjectVersion() {
        if (mavenProject != null) {
            return getVersion(mavenProject.getVersion());
        } else {
            return new MavenVersion("0.0"); //$NON-NLS-1$
        }
    }

    @Override
    public void setProjectVersion(IVersion<MavenVersion> version) {
        if (mavenProject != null) {
            mavenProject.setVersion(version.asString());
        }
    }

}
