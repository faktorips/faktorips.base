/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentRoot;

/**
 * This kind of {@link IpsSrcFile} is located within the eclipse workspace but not within a valid
 * {@link IpsPackageFragmentRoot}. That means it is external.
 */
public class IpsSrcFileOffRoot extends IpsSrcFile {

    private IFile file;

    public IpsSrcFileOffRoot(IFile file) {
        super(IpsPlugin.getDefault().getIpsModel().getIpsProject("IpsSrcFileImmutableIpsProject") //$NON-NLS-1$
                .getIpsPackageFragmentRoot("immutablePackageFragmentRoot").getDefaultIpsPackageFragment(), //$NON-NLS-1$
                file.getName());
        this.file = file;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public IFile getCorrespondingFile() {
        return file;
    }

    @Override
    public boolean isContainedInIpsRoot() {
        // default implementation will also come to this result but this is faster
        return false;
    }

}
