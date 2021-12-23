/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.AResourceDelta;

/**
 * An object path entry for an IPS archive.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchiveEntry extends IIpsLibraryEntry {

    public static final String FILE_EXTENSION = "ipsar"; //$NON-NLS-1$

    /**
     * Returns the IPS archive this entry refers to.
     */
    public IIpsArchive getIpsArchive();

    /**
     * Returns the archive location. Note that the underlying file might not exist and the file
     * might exists outside the workspace.
     */
    public IPath getArchiveLocation();

    /**
     * Returns true if a representation of this entry is part of the provided delta or one of its
     * children.
     * <p>
     * Note: For changes of files outside the workspace which are referenced from the project in any
     * kind no IResourceDelta will be created.
     */
    public boolean isAffectedBy(AResourceDelta delta);

}
