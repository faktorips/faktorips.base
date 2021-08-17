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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

/**
 * An implementation of {@link AbstractSearch} in order to search {@link IIpsSrcFile IIpsSrcFiles}
 * based on their given {@link IpsObjectType IpsObjectTypes} for an {@link IIpsObjectPathEntry
 * entry}. The found {@link IIpsSrcFile IIpsSrcFiles} are stored in a list. Whereas the
 * {@link IpsObjectType IpsObjectTypes} are stored in an array.
 */
public class IpsSrcFilesSearch extends AbstractSearch {

    private final IpsObjectType[] ipsObjectTypes;

    private final List<IIpsSrcFile> srcFiles = new ArrayList<>();

    /**
     * If no {@link IpsObjectType} as parameter is given all supported {@link IpsObjectType
     * IpsObjectTypes} will be used for the search.
     * 
     * @param ipsObjectTypesVarArg the {@link IpsObjectType} or {@link IpsObjectType IpsObjectTypes}
     *            for the search.
     */
    public IpsSrcFilesSearch(IpsObjectType... ipsObjectTypesVarArg) {
        if (ipsObjectTypesVarArg.length == 0) {
            this.ipsObjectTypes = IIpsModel.get().getIpsObjectTypes();
        } else {
            this.ipsObjectTypes = ipsObjectTypesVarArg;
        }
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        for (IpsObjectType ipsObjectType : ipsObjectTypes) {
            getIpsSrcFiles().addAll(entry.findIpsSrcFiles(ipsObjectType));
        }
    }

    /**
     * Returns all found {@link IIpsSrcFile IIpsSrcFiles}
     */
    public List<IIpsSrcFile> getIpsSrcFiles() {
        return srcFiles;
    }

    IpsObjectType[] getIpsObjectTypes() {
        return ipsObjectTypes;
    }
}
