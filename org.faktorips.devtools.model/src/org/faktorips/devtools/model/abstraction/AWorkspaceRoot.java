/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.model.abstraction.mapping.PathMapping.toEclipsePath;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.faktorips.devtools.model.abstraction.AFile.PlainJavaFile;
import org.faktorips.devtools.model.abstraction.AFolder.PlainJavaFolder;
import org.faktorips.devtools.model.abstraction.AProject.PlainJavaProject;
import org.faktorips.devtools.model.abstraction.AWorkspace.PlainJavaWorkspace;

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

    public static class AEclipseWorkspaceRoot extends AEclipseContainer implements AWorkspaceRoot {

        AEclipseWorkspaceRoot(IWorkspaceRoot workspaceRoot) {
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

    public static class PlainJavaWorkspaceRoot extends PlainJavaFolder implements AWorkspaceRoot {

        private PlainJavaWorkspace workspace;

        private final Map<Path, PlainJavaResource> resources = new HashMap<>();

        public PlainJavaWorkspaceRoot(PlainJavaWorkspace workspace) {
            super(workspace.unwrap());
            this.workspace = workspace;
        }

        @Override
        public PlainJavaWorkspace getWorkspace() {
            return workspace;
        }

        @Override
        public AResourceType getType() {
            return AResourceType.WORKSPACE;
        }

        @Override
        public AProject getProject(String name) {
            return project(directory().toPath().resolve(name));
        }

        @Override
        public AFile getFileForLocation(Path location) {
            if (getProjects().stream()
                    .map(p -> (File)p.unwrap())
                    .map(File::getAbsoluteFile)
                    .map(File::toPath)
                    .anyMatch(location::startsWith)) {
                return file(location);
            }
            return null;
        }

        @Override
        public Set<AProject> getProjects() {
            return getMembers().stream()
                    .filter(PlainJavaFolder.class::isInstance)
                    .map(PlainJavaFolder.class::cast)
                    .map(PlainJavaFolder::directory)
                    .map(File::toPath)
                    // TODO Projekte irgend wie erkennen und filtern?
                    .map(this::project)
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(AProject::getName))));
        }

        PlainJavaResource get(Path path) {
            synchronized (resources) {
                return resources.computeIfAbsent(path.toAbsolutePath(), p -> {
                    File file = p.toFile();
                    if (file.isFile()) {
                        return new PlainJavaFile(file);
                    }
                    if (file.isDirectory()) {
                        if (file.equals(directory())) {
                            return this;
                        } else if (file.getParentFile().equals(directory())) {
                            // TODO Projekte erkennen? Evtl. sind nur IPS-Projekte relevant?
                            return new PlainJavaProject(file);
                        } else {
                            return new PlainJavaFolder(file);
                        }
                    }
                    // TODO was gibt's noch?
                    return null;
                });
            }
        }

        PlainJavaFile file(Path path) {
            synchronized (resources) {
                PlainJavaResource resource = resources.computeIfAbsent(path.toAbsolutePath(),
                        p -> new PlainJavaFile(p.toFile()));
                if (resource instanceof PlainJavaFile) {
                    return (PlainJavaFile)resource;
                }
                throw new IllegalArgumentException(path + " is not a file"); //$NON-NLS-1$
            }
        }

        PlainJavaFolder folder(Path path) {
            synchronized (resources) {
                PlainJavaResource resource = resources.computeIfAbsent(path.toAbsolutePath(),
                        p -> new PlainJavaFolder(p.toFile()));
                if (resource instanceof PlainJavaFolder) {
                    return (PlainJavaFolder)resource;
                }
                throw new IllegalArgumentException(path + " is not a folder"); //$NON-NLS-1$
            }
        }

        PlainJavaProject project(Path path) {
            synchronized (resources) {
                Path absolutePath = path.toAbsolutePath();
                PlainJavaResource resource = resources.computeIfAbsent(absolutePath,
                        p -> new PlainJavaProject(p.toFile()));
                if (resource instanceof PlainJavaProject) {
                    return (PlainJavaProject)resource;
                }
                if (resource instanceof PlainJavaFolder) {
                    PlainJavaProject project = new PlainJavaProject(((PlainJavaFolder)resource).directory());
                    resources.put(absolutePath, project);
                    return project;
                }
                throw new IllegalArgumentException(path + " is not a project"); //$NON-NLS-1$
            }
        }

    }

}
