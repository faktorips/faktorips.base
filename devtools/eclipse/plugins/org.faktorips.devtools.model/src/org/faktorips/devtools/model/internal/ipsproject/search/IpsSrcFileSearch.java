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
 * An implementation of {@link AbstractSearch} in order to search an {@link IIpsSrcFile} at an
 * {@link IIpsObjectPathEntry entry}. The search is based on the given {@link QualifiedNameType}.
 */
public class IpsSrcFileSearch extends AbstractSearch {

    private final QualifiedNameType qualifiedNameType;
    private IIpsSrcFile ipsSrcFile;

    /**
     * @param qualifiedNameType the {@link QualifiedNameType} for the search.
     */
    public IpsSrcFileSearch(QualifiedNameType qualifiedNameType) {
        this.qualifiedNameType = qualifiedNameType;
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        ipsSrcFile = entry.findIpsSrcFile(getQualifiedNameType());
        if (ipsSrcFile != null && ipsSrcFile.exists()) {
            setStopSearch();
        } else {
            ipsSrcFile = null;
        }
    }

    /**
     * Returns found {@link IIpsSrcFile} or <code>null</code>.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    public QualifiedNameType getQualifiedNameType() {
        return qualifiedNameType;
    }

    public void setIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }
}
