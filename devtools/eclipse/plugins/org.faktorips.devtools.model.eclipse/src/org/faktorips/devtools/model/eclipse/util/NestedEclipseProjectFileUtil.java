/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.util;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * {@link IWorkspaceRoot#getFileForLocation(IPath)} may return unexpected results if there are
 * nested projects. If a file is found in multiple (e.g. parent and child) projects, the one in the
 * alphabetically first project is returned. {@link NestedEclipseProjectFileUtil#getFile(String)}
 * searches for all files and returns the one with the shortest path, thus the one from the
 * innermost nested project.
 */
public enum NestedEclipseProjectFileUtil {
    /* no instances */;

    /**
     * Looks for all files and returns the one with the shortest path - the one from the innermost
     * nested project.
     */
    public static IFile getFile(String filename) {
        Path path = new Path(filename);
        URI uri = URIUtil.toURI(path);
        if (!uri.isAbsolute()) {
            return null;
        }
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
        return getInnermostFile(files);
    }

    private static IFile getInnermostFile(IFile[] files) {
        int shortestPathSegmentCount = Integer.MAX_VALUE;
        IFile shortestPath = null;
        for (IFile resource : files) {
            if (!resource.exists()) {
                continue;
            }
            IPath fullPath = resource.getFullPath();
            int segmentCount = fullPath.segmentCount();
            if (segmentCount < shortestPathSegmentCount) {
                shortestPath = resource;
                shortestPathSegmentCount = segmentCount;
            }
        }
        return shortestPath;
    }
}
