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

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaWorkspace;

/**
 * A {@link PlainJavaWorkspace} with a {@link MavenWorkspaceRoot}, which identifies {@link AProject
 * projects} by their Maven group- and artifact-IDs.
 */
public class MavenWorkspace extends PlainJavaWorkspace {

    private static MavenWorkspaceRoot root;

    public MavenWorkspace(MavenProject project, List<MavenProject> upstreamProjects) {
        super(project.getBasedir().getParentFile());
        root = new MavenWorkspaceRoot(this, project, upstreamProjects);
    }

    @Override
    public MavenWorkspaceRoot getRoot() {
        return root;
    }

    @Override
    public String getName(PlainJavaProject project) {
        return root.getName(project);
    }

}