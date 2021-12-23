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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * A folder is a file-system resource that contains files and/or other folders.
 */
public interface AFolder extends AContainer {

    /**
     * Returns the file with the given name contained in this folder.
     * <p>
     * Note that that file does not necessarily {@link AResource#exists() exist}.
     *
     * @param name the file's name
     * @return the file with the given name
     */
    AFile getFile(String name);

    /**
     * Returns the folder with the given name contained in this folder.
     * <p>
     * Note that that folder does not necessarily {@link AResource#exists() exist}.
     *
     * @param name the folder name
     * @return the folder with the given name
     */
    AFolder getFolder(String name);

    /**
     * Creates this folder in the file-system.
     *
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     *
     * @throws CoreRuntimeException if the folder already exists or creation fails
     */
    void create(IProgressMonitor monitor);

}
