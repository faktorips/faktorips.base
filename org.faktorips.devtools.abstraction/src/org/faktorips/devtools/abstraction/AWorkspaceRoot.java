/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import java.nio.file.Path;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * The container representing {@link AWorkspace a workspace's} contents.
 */
public interface AWorkspaceRoot extends AContainer {

    /**
     * Returns all projects in this workspace.
     */
    Set<AProject> getProjects();

    /**
     * Returns the project with the given name in this workspace. It may not
     * {@link AResource#exists() exist}.
     *
     * @param name a project's name
     */
    AProject getProject(String name);

    /**
     * Returns the file with the given name in a project inside this workspace.
     *
     * @param location a file location; should reference a resource in one of the projects of this
     *            workspace
     * @return the resource corresponding to the file, or {@code null} if no such file exists in
     *         this workspace
     */
    @CheckForNull
    AFile getFileForLocation(Path location);

}
