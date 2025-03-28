/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.util;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.faktorips.devtools.abstraction.Abstractions;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class PathUtil {

    private PathUtil() {
        // util
    }

    public static String segment(Path path, int index) {
        return path.getName(index).toString();
    }

    public static String lastSegment(Path path) {
        return path.getName(path.getNameCount() - 1).toString();
    }

    public static Path removeFirstSegments(Path path, int count) {
        return path.subpath(count, path.getNameCount());
    }

    public static Path fromOSString(String pathString) {
        return Path.of(pathString);
    }

    public static String getFileExtension(Path path) {
        return Optional.ofNullable(path.getFileName())
                .map(Path::toString)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf(".") + 1))
                .orElse(null);
    }

    public static Path makeRelativeTo(Path path, Path base) {
        Path relativizedPath = base.relativize(path);
        return relativizedPath.toString().isEmpty() ? path : relativizedPath;

    }

    @CheckForNull
    public static String toPortableString(@CheckForNull Path path) {
        return path == null ? null : FilenameUtils.separatorsToUnix(path.toString());
    }

    public static boolean isAbsoluteInWorkspace(Path path) {
        return Abstractions.getWorkspace()
                .getRoot()
                .getProjects().stream()
                .anyMatch(p -> path.startsWith(p.getWorkspaceRelativePath()));
    }
}
