/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Represents an external, immutable {@link IpsSrcFile}. This file is read-only and imported from
 * another {@link IpsPackageFragmentRoot}.
 * 
 * @author Florian Orendi
 */
public class IpsSrcFileExternal extends IpsSrcFile {

    /**
     * The mutable, existing counterpart of this immutable file. This {@link IIpsSrcFile} can be
     * used to access the data hidden by the immutable file.
     */
    private IIpsSrcFile mutableSrcFile;

    public IpsSrcFileExternal(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * Getter for the mutable counterpart of this immutable file.
     * 
     * @return The mutable {@link IIpsSrcFile}
     */
    public IIpsSrcFile getMutableIpsSrcFile() {
        return mutableSrcFile;
    }

    /**
     * Setter for the mutable counterpart of this immutable file.
     * 
     * @param mutableSrcFile The mutable {@link IIpsSrcFile}
     */
    public void setMutableIpsSrcFile(IIpsSrcFile mutableSrcFile) {
        this.mutableSrcFile = mutableSrcFile;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isContainedInIpsRoot() {
        // default implementation will also come to this result but this is faster
        return false;
    }
}
