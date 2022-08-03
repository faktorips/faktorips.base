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

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;

/**
 * A file is a resource in the file-system that contains data.
 */
public interface AFile extends AResource {

    /**
     * Returns the corresponding file's extension (the part after the last '.'), if there is any.
     *
     * @return this file's extension or an empty String if the file has no extension
     */
    String getExtension();

    /**
     * Returns whether this file is read-only (meaning data can be read but not written).
     *
     * @return whether this file is read-only
     */
    boolean isReadOnly();

    /**
     * Creates this file in the file-system with data from the given {@link InputStream}.
     *
     * @param source provides the data to be written to the new file
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     * @throws IpsException if the file already exists or creation fails
     */
    void create(InputStream source, IProgressMonitor monitor);

    /**
     * Returns the file's data contents as an {@link InputStream}.
     *
     * @return the file's contents
     * @throws IpsException if the file can't be read
     */
    InputStream getContents();

    /**
     * Overwrites this file in the file-system with data from the given {@link InputStream}.
     *
     * @param source provides the data to be written to the new file
     * @param keepHistory whether to keep a history of the content (if supported by the workspace)
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     * @throws IpsException if the file can't be written or does not exist
     */
    void setContents(InputStream source, boolean keepHistory, IProgressMonitor monitor);

}
