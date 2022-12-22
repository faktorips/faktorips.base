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

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A dependency that is a {@link IIpsProject}. Can be created from an {@link Artifact} or a
 * {@link MavenProject}. In addition to the version, groupId and artifactId of the dependency, the
 * path to the maven project/artifact is stored. If the dependency is present as a maven project,
 * the maven project is converted to its {@link IIpsProject} which get stored as well.
 */
public class IpsDependency {
    private String artifactId;
    private String groupId;
    private String version;
    private Path path;
    private IIpsProject ipsProject;
    private MavenProject mavenProject;

    public static IpsDependency create(Artifact artifact) {
        IpsDependency ipsDependency = new IpsDependency();
        ipsDependency.setArtifactId(artifact.getArtifactId());
        ipsDependency.setGroupId(artifact.getGroupId());
        ipsDependency.setVersion(artifact.getVersion());
        ipsDependency.setPath(artifact.getFile().toPath());
        ipsDependency.setIpsProject(null);
        return ipsDependency;
    }

    public static IpsDependency create(MavenProject project) {
        IpsDependency ipsDependency = new IpsDependency();
        ipsDependency.setArtifactId(project.getArtifactId());
        ipsDependency.setGroupId(project.getGroupId());
        ipsDependency.setVersion(project.getVersion());
        ipsDependency.setPath(project.getBasedir().toPath());
        ipsDependency.setIpsProject(convertMavenProjectToIpsProject(project));
        ipsDependency.setMavenProject(project);
        return ipsDependency;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isProject() {
        return ipsProject != null;
    }

    /**
     * If the {@link IpsDependency} is created from a {@link MavenProject}, this method returns the
     * {@link IIpsProject} that belongs to the Maven project. Otherwise it returns {@code null}.
     */
    @CheckForNull
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Optional<MavenProject> getMavenProject() {
        return Optional.ofNullable(mavenProject);
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
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

    protected static IIpsProject convertMavenProjectToIpsProject(MavenProject mavenProject) {
        AProject aProject = Abstractions.getWorkspace().getRoot()
                .getProject(MavenWorkspaceRoot.toProjectName(mavenProject));
        return IIpsModel.get().getIpsProject(aProject);
    }
}
