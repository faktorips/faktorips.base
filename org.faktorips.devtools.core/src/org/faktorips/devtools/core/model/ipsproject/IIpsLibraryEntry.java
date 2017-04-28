/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;

/**
 * The library entry is the common interface for {@link IIpsArchiveEntry} and {@link IpsBundleEntry}
 * . It is an {@link IIpsObjectPathEntry} for bundles and archives.
 * 
 * 
 * @author dirmeier
 */
public interface IIpsLibraryEntry extends IIpsObjectPathEntry {

    /**
     * Initializes the library and set the specified path.
     * 
     * @throws IOException In case of an exception while IO operations when initializing the library
     */
    public void initStorage(IPath path) throws IOException;

    public IPath getPath();
}
