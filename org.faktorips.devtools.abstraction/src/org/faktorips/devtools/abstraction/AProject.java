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

import java.nio.charset.Charset;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A project is a special container wrapping resources and settings.
 */
public interface AProject extends AContainer {

    /**
     * Returns whether this project is a Faktor-IPS project.
     */
    boolean isIpsProject();

    /**
     * Returns the file with the given name inside this project. It may not {@link #exists() exist}.
     *
     * @param name a file name, will be resolved as a path relative to this project.
     * @return the file identified by the name
     */
    AFile getFile(String name);

    /**
     * Returns the folder with the given name inside this project. It may not {@link #exists()
     * exist}.
     *
     * @param name a folder name, will be resolved as a path relative to this project.
     * @return the folder identified by the name
     */
    AFolder getFolder(String name);

    /**
     * Returns all other projects this project references.
     */
    Set<AProject> getReferencedProjects();

    /**
     * Builds this project. The {@link ABuildKind buildKind} parameter determines, whether an
     * incremental or full build is done and whether output folders are cleaned beforehand.
     *
     * @param buildKind the kind of build to perform
     * @param monitor a progress monitor that is notified about the build process. Individual file
     *            processing is reported to the monitor to allow fine-grained progress reporting.
     *            The monitor may be {@code null} when progress does not need to be reported.
     */
    void build(ABuildKind buildKind, IProgressMonitor monitor);

    /**
     * Returns the default character set used for files in this project.
     */
    Charset getDefaultCharset();

}
