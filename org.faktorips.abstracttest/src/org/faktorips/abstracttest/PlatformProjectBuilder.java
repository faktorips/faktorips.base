/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Allows easy creation of {@linkplain IProject projects} with different configurations.
 */
public class PlatformProjectBuilder {

    private String name;

    public PlatformProjectBuilder() {
        // Default name
        name = UUID.randomUUID().toString();
    }

    public PlatformProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public IProject build() throws CoreException {
        return newPlatformProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private IProject newPlatformProject(final String name) throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                internalNewPlatformProject(name);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        return workspace.getRoot().getProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private IProject internalNewPlatformProject(final String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        project.create(null);
        project.open(null);
        return project;
    }

}
