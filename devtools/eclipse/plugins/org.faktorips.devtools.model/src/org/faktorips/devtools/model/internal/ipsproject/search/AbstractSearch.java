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

import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;

/**
 * {@link AbstractSearch} is designed for processing different {@link IpsObjectPathEntry}s.
 */
public abstract class AbstractSearch {

    private boolean isIncludeIndirect = true;
    private SearchState searchState = SearchState.CONTINUE_SEARCH;

    /**
     * Process the {@link IIpsObjectPathEntry}.
     * 
     * @param entry the current {@link IIpsObjectPathEntry}
     */
    public abstract void processEntry(IIpsObjectPathEntry entry);

    public void setIncludeIndirect(boolean isIncludeIndirect) {
        this.isIncludeIndirect = isIncludeIndirect;
    }

    public boolean isIncludeIndirect() {
        return isIncludeIndirect;
    }

    protected boolean isProjectRefEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
    }

    protected boolean isSrcFolderEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_SRC_FOLDER);
    }

    public IIpsProject getReferencedIpsProject(IIpsObjectPathEntry entry) {
        return ((IIpsProjectRefEntry)entry).getReferencedIpsProject();
    }

    /**
     * Returns <code>true</code> if the {@link SearchState} is {@link SearchState#STOP_SEARCH}.
     */
    public boolean isStopSearch() {
        return searchState.isStopSearch();
    }

    protected void setStopSearch() {
        searchState = SearchState.STOP_SEARCH;
    }

    public enum SearchState {
        STOP_SEARCH,
        CONTINUE_SEARCH;

        public boolean isStopSearch() {
            return equals(STOP_SEARCH);
        }
    }
}
