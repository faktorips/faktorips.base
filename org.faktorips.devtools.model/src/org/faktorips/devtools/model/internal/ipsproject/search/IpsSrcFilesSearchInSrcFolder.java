/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

/**
 * An implementation of {@link AbstractSearch} in order to process {@link IpsObjectType
 * IpsObjectTypes} in IIpsObjectPathEntry.TYPE_SRC_FOLDER.
 */
public class IpsSrcFilesSearchInSrcFolder extends IpsSrcFilesSearch {

    public IpsSrcFilesSearchInSrcFolder(IpsObjectType... ipsObjectTypesVarArg) {
        super(ipsObjectTypesVarArg);
        setIncludeIndirect(false);
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        if (isSrcFolderEntry(entry)) {
            super.processEntry(entry);
        }
    }
}
