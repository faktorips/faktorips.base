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

import org.faktorips.runtime.MessageList;

/**
 * A Java project is {@link AProject a project} containing {@link AJavaElement Java elements}.
 */
public interface AJavaProject extends AAbstraction {

    /**
     * Returns the corresponding project.
     */
    AProject getProject();

    /**
     * A Java project exists if its file-system equivalent exists (the path corresponds to an actual
     * file/folder) and is configured to contain Java elements.
     *
     * @return whether this project exists
     */
    boolean exists();

    /**
     * Returns whether this project has previously been built and retains some information about
     * that build.
     */
    boolean hasBuildState();

    /**
     * Returns the default output location for this project as a path relative to the containing
     * workspace.
     */
    Path getOutputLocation();

    /**
     * Returns the package fragment root for an external library
     *
     * @param externalLibraryPath the path to the external library, either a folder or archive file
     */
    APackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath);

    /**
     * Returns the package fragment root for an external library
     *
     * @param resource the path to the root, either a folder or archive file
     */
    APackageFragmentRoot getPackageFragmentRoot(AResource resource);

    /**
     * Returns the Java version number for this project's source code.
     */
    Runtime.Version getSourceVersion();

    /**
     * Returns all other Java projects referenced by this project.
     */
    Set<AJavaProject> getReferencedJavaProjects();

    /**
     * Returns whether the given resource is a Java folder.
     */
    boolean isJavaFolder(AResource resource);

    /**
     * Validates whether this project's build path is configured correctly, adding any errors to the
     * returned list.
     */
    MessageList validateJavaProjectBuildPath();

    /**
     * Returns the Java project matching the given project.
     */
    public static AJavaProject from(AProject project) {
        return Wrappers.wrap(project).as(AJavaProject.class);
    }

}
