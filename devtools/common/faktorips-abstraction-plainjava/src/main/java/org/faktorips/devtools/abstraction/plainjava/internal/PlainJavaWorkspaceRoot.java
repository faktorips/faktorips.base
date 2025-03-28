/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;

public class PlainJavaWorkspaceRoot extends PlainJavaFolder implements AWorkspaceRoot {

    private PlainJavaWorkspace workspace;

    private final Map<Path, PlainJavaResource> resources = new ConcurrentHashMap<>();
    private final Map<String, AProject> projectsByName = new ConcurrentHashMap<>();

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
        AProject project = projectsByName.computeIfAbsent(name, n -> project(directory().toPath().resolve(n)));
        // put it with it's own name again, as this may differ for off-root projects
        projectsByName.put(project.getName(), project);
        return project;
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
        refreshInternal();
        return Stream.concat(getMembers().stream()
                .filter(PlainJavaFolder.class::isInstance)
                .map(PlainJavaFolder.class::cast)
                .map(PlainJavaFolder::directory)
                .map(File::toPath)
                // FIPS-8693: Müssen ggf. .git- und andere Ordner ignoriert werden?
                .map(this::project), projectsByName.values().stream())
                .distinct()
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AProject::getName))));
    }

    PlainJavaResource get(Path path) {
        Path absolutePath = path.isAbsolute() ? path : directory().toPath().resolve(path).toAbsolutePath();
        return resources.computeIfAbsent(absolutePath, p -> {
            File file = p.toFile();
            if (file.isDirectory() || (!file.exists() && !file.getName().contains("."))) { //$NON-NLS-1$
                if (file.equals(directory())) {
                    return this;
                } else {
                    File parentFile = file.getParentFile();
                    if (parentFile == null) {
                        return null;
                    }
                    if (parentFile.equals(directory())) {
                        // FIPS-8693: Müssen ggf. .git- und andere Ordner ignoriert werden?
                        return (PlainJavaProject)wrap(file).as(AProject.class);
                    } else {
                        return (PlainJavaFolder)wrap(file).as(AFolder.class);
                    }
                }
            }
            return (PlainJavaFile)wrap(file).as(AFile.class);
        });
    }

    public void remove(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                        throws IOException {
                    resources.remove(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    resources.remove(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    PlainJavaFile file(Path path) {
        PlainJavaResource resource = resources.computeIfAbsent(path.toAbsolutePath(),
                p -> (PlainJavaFile)wrap(p.toFile()).as(AFile.class));
        if (resource instanceof PlainJavaFile) {
            return (PlainJavaFile)resource;
        }
        throw new IllegalArgumentException(path + " is not a file"); //$NON-NLS-1$
    }

    PlainJavaFolder folder(Path path) {
        PlainJavaResource resource = resources.computeIfAbsent(path.toAbsolutePath(),
                p -> (PlainJavaFolder)wrap(p.toFile()).as(AFolder.class));
        if (resource instanceof PlainJavaFolder) {
            return (PlainJavaFolder)resource;
        }
        throw new IllegalArgumentException(path + " is not a folder"); //$NON-NLS-1$
    }

    protected PlainJavaProject project(Path path) {
        Path absolutePath = path.toAbsolutePath();
        PlainJavaResource resource = resources.computeIfAbsent(absolutePath,
                p -> (PlainJavaProject)wrap(p.toFile()).as(AProject.class));
        if (resource instanceof PlainJavaProject) {
            return (PlainJavaProject)resource;
        }
        if (resource instanceof PlainJavaFolder) {
            PlainJavaProject project = (PlainJavaProject)wrap(((PlainJavaFolder)resource).directory())
                    .as(AProject.class);
            resources.put(absolutePath, project);
            return project;
        }
        throw new IllegalArgumentException(path + " is not a project"); //$NON-NLS-1$
    }

    void deleteProject(PlainJavaProject plainJavaProject) {
        projectsByName.entrySet().removeIf(entry -> Objects.equals(entry.getValue(), plainJavaProject));
    }
}
