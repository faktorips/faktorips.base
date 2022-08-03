/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.mapping.PathMapping.toEclipsePath;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;

public class EclipseWorkspaceRoot extends EclipseContainer implements AWorkspaceRoot {

    EclipseWorkspaceRoot(IWorkspaceRoot workspaceRoot) {
        super(workspaceRoot);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IWorkspaceRoot unwrap() {
        return (IWorkspaceRoot)super.unwrap();
    }

    IWorkspaceRoot workspaceRoot() {
        return unwrap();
    }

    @Override
    public AProject getProject(String name) {
        return wrap(workspaceRoot().getProject(name)).as(AProject.class);
    }

    @Override
    public AFile getFileForLocation(Path location) {
        return wrap(workspaceRoot().getFileForLocation(toEclipsePath(location))).as(AFile.class);
    }

    @Override
    public Set<AProject> getProjects() {
        return wrap(workspaceRoot().getProjects()).asSetOf(AProject.class);
    }

}
