/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction.mapping;

import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;

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
    public static Path toJavaPath(IPath eclipsePath) {
        return eclipsePath.toFile().toPath();
    }

    /**
     * Maps the given plain Java {@link Path} to an Eclipse {@link IPath}.
     */
    public static IPath toEclipsePath(Path path) {
        return org.eclipse.core.runtime.Path.fromOSString(path.toString());
    }

}
