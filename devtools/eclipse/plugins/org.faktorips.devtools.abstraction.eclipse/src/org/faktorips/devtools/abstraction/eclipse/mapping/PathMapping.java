/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.mapping;

import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Mapping between Eclipse {@link IPath} and plain Java {@link Path}.
 */
public class PathMapping {

    private PathMapping() {
        // util
    }

    /**
     * Maps the given Eclipse {@link IPath} to a plain Java {@link Path}.
     */
    @CheckForNull
    public static Path toJavaPath(@CheckForNull IPath eclipsePath) {
        return eclipsePath == null ? null : eclipsePath.toFile().toPath();
    }

    /**
     * Maps the given plain Java {@link Path} to an Eclipse {@link IPath}.
     */
    @CheckForNull
    public static IPath toEclipsePath(@CheckForNull Path path) {
        return path == null ? null : org.eclipse.core.runtime.Path.fromOSString(path.toString());
    }

}
