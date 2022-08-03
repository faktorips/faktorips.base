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

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

/**
 * An implementation of {@link AbstractSearch} in order to search for duplicate {@link IIpsSrcFile
 * IIpsSrcFiles}.
 */
public class DuplicateIpsSrcFileSearch extends IpsSrcFileSearch {

    private boolean foundIpsSrcFile = false;

    /**
     * @param qualifiedNameType the {@link QualifiedNameType} for the search.
     */
    public DuplicateIpsSrcFileSearch(QualifiedNameType qualifiedNameType) {
        super(qualifiedNameType);
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        IIpsSrcFile currentIpsSrcFile = entry.findIpsSrcFile(getQualifiedNameType());
        if (currentIpsSrcFile != null && currentIpsSrcFile.exists()) {
            if (getIpsSrcFile() != null) {
                foundIpsSrcFile = true;
                setStopSearch();
            } else {
                setIpsSrcFile(currentIpsSrcFile);
            }
        }
    }

    /**
     * Returns <code>true</code> if a duplicate {@link IIpsSrcFile} was found.
     */
    public boolean foundDuplicateIpsSrcFile() {
        return foundIpsSrcFile;
    }
}
