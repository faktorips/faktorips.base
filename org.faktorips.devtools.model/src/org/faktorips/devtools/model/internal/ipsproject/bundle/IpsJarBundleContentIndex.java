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

import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.abstraction.util.PathUtil;

/**
 * The {@link IpsJarBundleContentIndex} reads the list of entries of a {@link JarFile} and caches
 * some information about the qualified names and folders.
 * 
 * @author dicker
 */
public class IpsJarBundleContentIndex extends AbstractIpsBundleContentIndex {

    /**
     * Create an {@link IpsJarBundleContentIndex} reading from the specified {@link JarFile}. The
     * JarFile should be ready to read and it will be closed after the object was constructed.
     * <p>
     * Every file located in any of the given model folders is registered in the index. The files
     * are indexed relative to the model folder.
     * 
     * @param jarFile The {@link JarFile} that should be read and indexed
     * @param modelFolders The list of model folders, the paths are relativ to the root of the jar
     *            file
     */
    public IpsJarBundleContentIndex(JarFile jarFile, List<Path> modelFolders) {

        Assert.isNotNull(jarFile, "jarFile must not be null"); //$NON-NLS-1$
        Assert.isNotNull(modelFolders, "modelFolders must not be null"); //$NON-NLS-1$

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();

            registerJarEntry(jarEntry, modelFolders);
        }
    }

    private final void registerJarEntry(JarEntry jarEntry, List<Path> modelFolders) {
        String pathToFile = jarEntry.getName();

        Path path = Path.of(pathToFile);

        registerPath(path, modelFolders);
    }

    protected final void registerPath(Path path, List<Path> modelFolders) {
        for (Path modelPath : modelFolders) {
            if (path.startsWith(modelPath)) {
                Path relativePath = PathUtil.makeRelativeTo(path, modelPath);
                registerPath(modelPath, relativePath);
                return;
            }
        }
    }
}
