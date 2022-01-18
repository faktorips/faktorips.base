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

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.runtime.MessageList;

public class PlainJavaJavaProject extends PlainJavaJavaElement implements AJavaProject {

    private static final String TEST_FOLDER = "test"; //$NON-NLS-1$
    private static final String RESOURCES_FOLDER = "resources"; //$NON-NLS-1$
    private static final String JAVA_FOLDER = "java"; //$NON-NLS-1$
    private static final String MAIN_FOLDER = "main"; //$NON-NLS-1$
    private static final String SRC_FOLDER = "src"; //$NON-NLS-1$

    private final List<AFolder> folders;

    public PlainJavaJavaProject(File project) {
        super(project);
        folders = List.of(
                getProject().getFolder(SRC_FOLDER).getFolder(MAIN_FOLDER).getFolder(JAVA_FOLDER),
                getProject().getFolder(SRC_FOLDER).getFolder(MAIN_FOLDER).getFolder(RESOURCES_FOLDER),
                getProject().getFolder(SRC_FOLDER).getFolder(TEST_FOLDER).getFolder(JAVA_FOLDER),
                getProject().getFolder(SRC_FOLDER).getFolder(TEST_FOLDER).getFolder(RESOURCES_FOLDER));
    }

    @SuppressWarnings("unchecked")
    @Override
    public File unwrap() {
        return super.unwrap();
    }

    File javaProject() {
        return unwrap();
    }

    @Override
    public AProject getProject() {
        return getResource().getProject();
    }

    @Override
    public boolean exists() {
        return getProject().exists();
    }

    @Override
    public boolean hasBuildState() {
        // TODO
        return false;
    }

    @Override
    public Path getOutputLocation() {
        // TODO bin, target, derived...?
        return getProject().getWorkspaceRelativePath()
                .resolve("target"); //$NON-NLS-1$
    }

    @Override
    public APackageFragmentRoot toPackageFragmentRoot(String externalLibraryPath) {
        // TODO: an jar files anpassen
        return Wrappers.wrap(Path.of(externalLibraryPath).toFile())
                .as(APackageFragmentRoot.class);
    }

    @Override
    public APackageFragmentRoot toPackageFragmentRoot(AResource resource) {
        // TODO main/derived...?
        if (!AResourceType.FOLDER.equals(resource.getType())) {
            throw new UnsupportedOperationException(resource + " is not a directory."); //$NON-NLS-1$
        }
        return Wrappers.wrap(PlainJavaFileUtil.internalResource(
                resource.unwrap(), (PlainJavaProject)getProject())).as(APackageFragmentRoot.class);
    }

    @Override
    public Runtime.Version getSourceVersion() {
        // TODO aus pom lesen
        return Runtime.version();
    }

    @Override
    public Set<AJavaProject> getReferencedJavaProjects() {
        // TODO brauchen wir das oder sind alle Referenzen JARs?
        return Set.of();
    }

    @Override
    public boolean isJavaFolder(AResource resource) {
        return Objects.equals(toPackageFragmentRoot(resource).unwrap(), resource.unwrap());
    }

    @Override
    public MessageList validateJavaProjectBuildPath() {
        // TODO
        return MessageList.of();
    }

    @Override
    public Map<String, String> getOptions() {
        // TODO von Maven ableiten?
        return Map.of();
    }

    @Override
    public Set<APackageFragmentRoot> getAllPackageFragmentRoots() {
        // TODO von Maven auslesen
        return folders.stream()
                .map(p -> Wrappers.wrap(p).as(APackageFragmentRoot.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}