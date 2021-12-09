/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;

/**
 * Allows easy creation of {@linkplain IProject projects} with different configurations.
 */
public class PlatformProjectBuilder {

    private String name;
    private IProjectDescription description;

    public PlatformProjectBuilder() {
        // Default name
        name = UUID.randomUUID().toString();
    }

    public PlatformProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PlatformProjectBuilder description(IProjectDescription description) {
        this.description = description;
        return this;
    }

    public IProject build() throws CoreException {
        return newPlatformProject(name, description);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private IProject newPlatformProject(final String name, final IProjectDescription description)
            throws CoreException {
        ICoreRunnable runnable = $ -> internalNewPlatformProject(name, description);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, null);
        return workspace.getRoot().getProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private IProject internalNewPlatformProject(final String name, IProjectDescription description)
            throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        project.create(description, null);
        project.open(null);
        return project;
    }

}
