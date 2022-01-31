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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;

/**
 * This FolderExplorer supports reading files and subfolders of folders.
 * 
 * 
 * @author dicker
 */
public class FolderExplorer {

    /**
     * returns a List of {@link IPath paths}, which represents the absolute paths to the files
     * within the given {@link IPath}, which are not folders.
     * <p>
     * If nothing is found, an empty List will be returned.
     * 
     */
    public List<Path> getFiles(Path path) {
        if (isNotPathRelevant(path)) {
            return Collections.emptyList();
        }

        return createPaths(path, false);
    }

    /**
     * returns a List of {@link IPath paths}, which represents the absolute paths to the folders
     * within the given {@link IPath}.
     * <p>
     * If nothing is found, an empty List will be returned.
     * 
     */
    public List<Path> getFolders(Path path) {
        if (isNotPathRelevant(path)) {
            return Collections.emptyList();
        }
        return createPaths(path, true);
    }

    private boolean isRelevant(File file, boolean mustBeDirectory) {
        if (mustBeDirectory) {
            return file.isDirectory();
        }
        return file.isFile();
    }

    private List<Path> createPaths(Path path, boolean mustBeDirectory) {
        List<Path> paths = new ArrayList<>();
        File[] listFiles = path.toFile().listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (isRelevant(file, mustBeDirectory)) {
                    String name = file.getName();
                    paths.add(path.resolve(name));
                }
            }
        }

        return paths;
    }

    private boolean isNotPathRelevant(Path path) {
        if (path == null) {
            return true;
        }

        File file = path.toFile();
        if (!file.isDirectory()) {
            return true;
        }

        File[] listFiles = file.listFiles();
        if (listFiles.length == 0) {
            return true;
        }

        return false;
    }
}
