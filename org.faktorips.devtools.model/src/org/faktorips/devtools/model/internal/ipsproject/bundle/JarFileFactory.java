/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.mapping.PathMapping;

/**
 * This class simply creates a JarFile for a previously specified path. After creating a
 * {@link JarFileFactory} with a specified {@link IPath} you could create as many {@link JarFile jar
 * files} as you want by calling {@link #createJarFile()}. You have to verify for yourself that the
 * jar file is closed correctly after use.
 * <p>
 * The path in this {@link JarFileFactory} may either be absolute in the workspace or it is absolute
 * in the file system. The factory first checks if there is a file with the specified path in the
 * workspace. If not it will take the path as being absolute in the file system.
 * 
 * 
 * @author dirmeier
 */
public class JarFileFactory {

    private final Path jarPath;

    /**
     * Create the {@link JarFileFactory}, the jarPath is the absolute path to the jar file this
     * factory will construct. It is either absolute in workspace or absolute in file system.
     * 
     * @param jarPath The absolute path to a jar file
     */
    public JarFileFactory(Path jarPath) {
        this.jarPath = jarPath;
    }

    /**
     * Returns the path of the jar file this factory could construct.
     * 
     * @return The absolute jar file path
     */
    public Path getJarPath() {
        return jarPath;
    }

    /**
     * Creates a new jar file every time you call this method. You have to ensure that the jar file
     * is closed correctly after you do not need it any longer.
     * 
     * @return A new jar file of the path specified in this factory
     * 
     * @throws IOException in case of any IO exception while creating the {@link JarFileFactory}
     * @see JarFile#JarFile(java.io.File)
     */
    public JarFile createJarFile() throws IOException {
        Path absolutePath = getAbsolutePath(jarPath);
        return new JarFile(absolutePath.toFile());
    }

    /* private */Path getAbsolutePath(Path bundlePath) {
        if (isWorkspaceRelativePath(PathMapping.toEclipsePath(bundlePath))) {
            return PathMapping.toJavaPath(getWorkspaceRelativePath(PathMapping.toEclipsePath(bundlePath)));
        } else {
            return bundlePath;
        }
    }

    private boolean isWorkspaceRelativePath(IPath bundlePath) {
        return ResourcesPlugin.getWorkspace().getRoot().exists(bundlePath);
    }

    private IPath getWorkspaceRelativePath(IPath bundlePath) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(bundlePath);
        return file.getLocation();
    }

}
