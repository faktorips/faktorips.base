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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaProject;

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

    public AProject build() {
        return newPlatformProject(name, description);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private AProject newPlatformProject(final String name, final IProjectDescription description) {
        ICoreRunnable runnable = $ -> internalNewPlatformProject(name, description);
        AWorkspace workspace = Abstractions.getWorkspace();
        workspace.run(runnable, null);
        return workspace.getRoot().getProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private AProject internalNewPlatformProject(final String name, IProjectDescription description)
            throws CoreException {
        AWorkspaceRoot root = Abstractions.getWorkspace().getRoot();
        AProject project = root.getProject(name);
        if (Abstractions.isEclipseRunning()) {
            ((IProject)project.unwrap()).create(description, null);
            ((IProject)project.unwrap()).open(null);
        } else {
            ((PlainJavaProject)project).create();
        }
        return project;
    }
}
