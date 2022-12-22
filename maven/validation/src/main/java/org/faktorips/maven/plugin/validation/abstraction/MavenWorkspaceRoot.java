/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation.abstraction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaWorkspaceRoot;

/**
 * A {@link PlainJavaWorkspaceRoot} that identifies {@link AProject projects} by their Maven group-
 * and artifact-IDs.
 */
public class MavenWorkspaceRoot extends PlainJavaWorkspaceRoot {

    private final Map<String, AProject> projects = new LinkedHashMap<>();
    private final List<MavenProject> upstreamProjects;
    private final MavenProject project;

    public MavenWorkspaceRoot(MavenWorkspace workspace, MavenProject project, List<MavenProject> upstreamProjects) {
        super(workspace);
        this.upstreamProjects = upstreamProjects;
        this.project = project;
    }

    String getName(AProject p) {
        return projects.entrySet().stream()
                .filter(e -> e.getValue().equals(p))
                .map(Entry::getKey)
                .findAny()
                .orElseThrow();
    }

    @Override
    public AProject getProject(String name) {
        return projects.computeIfAbsent(name, n -> {
            if (toProjectName(project).equals(n)) {
                return project(project.getBasedir().toPath());
            }
            return upstreamProjects.stream()
                    .filter(p -> toProjectName(p).equals(n))
                    .findFirst()
                    .map(p -> project(p.getBasedir().toPath()))
                    .orElseThrow();
        });
    }

    /**
     * Returns the project name consisting of "&lt;groupId&gt;.&lt;artifactId&gt;".
     */
    public static String toProjectName(MavenProject project) {
        return project.getGroupId() + '.' + project.getArtifactId();
    }

}