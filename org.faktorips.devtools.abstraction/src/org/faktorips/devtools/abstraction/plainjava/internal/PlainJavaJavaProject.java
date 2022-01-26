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

import java.lang.Runtime.Version;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWrapper;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.runtime.MessageList;

public class PlainJavaJavaProject extends AWrapper<PlainJavaProject> implements AJavaProject {

    public PlainJavaJavaProject(PlainJavaProject project) {
        super(project);
    }

    @Override
    public AProject getProject() {
        return unwrap();
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
        // TODO main/derived...?
        return getProject().getWorkspaceRelativePath().resolve("src/main/java"); //$NON-NLS-1$
    }

    @Override
    public APackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath) {
        // TODO
        return Wrappers.wrap(Path.of(externalLibraryPath).toFile())
                .as(APackageFragmentRoot.class);
    }

    @Override
    public APackageFragmentRoot getPackageFragmentRoot(AResource resource) {
        // TODO main/derived...?
        return Wrappers
                .wrap(getProject().getFolder("src").getFolder("main").getFolder("java") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .getFile(resource.getProjectRelativePath()).unwrap())
                .as(APackageFragmentRoot.class);
    }

    @Override
    public Version getSourceVersion() {
        return Runtime.version();
    }

    @Override
    public Set<AJavaProject> getReferencedJavaProjects() {
        // TODO brauchen wir das oder sind alle Referenzen JARs?
        return Set.of();
    }

    @Override
    public boolean isJavaFolder(AResource resource) {
        return Objects.equals(getPackageFragmentRoot(resource).unwrap(), resource.unwrap());
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

}