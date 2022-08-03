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

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A Java element is the representation of a Java related resource.
 */
public interface AJavaElement extends AAbstraction {

    /**
     * Returns the corresponding resource. If this element represents some part smaller than a file,
     * the enclosing file resource is returned.
     *
     * @return the corresponding resource
     */
    AResource getResource();

    /**
     * A Java element exists if its corresponding resource exists and contains information for this
     * element.
     *
     * @return whether this element exists
     */
    boolean exists();

    /**
     * Returns the Java project containing this element.
     *
     * @return the containing Java project; may be {@code null} if this element is not contained in
     *             a project
     */
    @CheckForNull
    AJavaProject getJavaProject();

    /**
     * Returns the absolute location of this element in the file system.
     *
     * @return this element's location
     */
    Path getPath();
}
