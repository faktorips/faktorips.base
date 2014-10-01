/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;

/**
 * An implementation of {@link AbstractSearch} in order to process {@link IIpsSrcFile}.
 */
public class DublicateIpsSrcFileSearch extends IpsSrcFileSearch {

    private boolean foundIpsSrcFile = false;

    /**
     * @param qualifiedNameType the {@link QualifiedNameType} for the search.
     */
    public DublicateIpsSrcFileSearch(QualifiedNameType qualifiedNameType) {
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

    public boolean foundDublicateIpsSrcFile() {
        return foundIpsSrcFile;
    }
}