/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.maven.plugin.validation.abstraction.MavenWorkspaceRoot;

/**
 * A dependency that is a {@link IIpsProject}. Can be created from an {@link Artifact} or a
 * {@link MavenProject}. In addition to the version, groupId and artifactId of the dependency, the
 * path to the maven project/artifact is stored. If the dependency is present as a maven project,
 * the maven project is converted to its {@link IIpsProject} which get stored as well.
 */
public record IpsDependency(String artifactId,
        String groupId,
        String version,
        Path path,
        IIpsProject ipsProject,
        MavenProject mavenProject) {

    public static IpsDependency create(Artifact artifact) {
        return new IpsDependency(artifact.getArtifactId(), artifact.getGroupId(),
                artifact.getVersion(), artifact.getFile().toPath(), null, null);
    }

    public static IpsDependency create(MavenProject project) {
        return new IpsDependency(project.getArtifactId(), project.getGroupId(),
                project.getVersion(), project.getBasedir().toPath(), convertMavenProjectToIpsProject(project), project);
    }

    public Optional<MavenProject> getMavenProject() {
        return Optional.ofNullable(mavenProject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        IpsDependency other = (IpsDependency)obj;
        return Objects.equals(artifactId, other.artifactId) && Objects.equals(groupId, other.groupId)
                && Objects.equals(version, other.version);
    }

    private static IIpsProject convertMavenProjectToIpsProject(MavenProject mavenProject) {
        AProject aProject = Abstractions.getWorkspace().getRoot()
                .getProject(MavenWorkspaceRoot.toProjectName(mavenProject));
        return IIpsModel.get().getIpsProject(aProject);
    }
}
