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

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragmentRoot;

/**
 * This kind of {@link IpsSrcFileExternal} is located within the eclipse workspace but not within a
 * valid {@link IpsPackageFragmentRoot}. That means it is external.
 */
public class IpsSrcFileOffRoot extends IpsSrcFileExternal {

    private AFile file;

    public IpsSrcFileOffRoot(AFile file) {
        super(IIpsModel.get().getIpsProject("IpsSrcFileImmutableIpsProject") //$NON-NLS-1$
                .getIpsPackageFragmentRoot("immutablePackageFragmentRoot").getDefaultIpsPackageFragment(), //$NON-NLS-1$
                file.getName());
        this.file = file;
    }

    @Override
    public AFile getCorrespondingFile() {
        return file;
    }

}
