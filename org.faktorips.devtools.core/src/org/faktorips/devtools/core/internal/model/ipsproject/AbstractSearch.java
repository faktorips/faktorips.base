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

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;

/**
 * {@link AbstractSearch} is designed for processing different {@link IpsObjectPathEntry}s.
 */
public abstract class AbstractSearch {

    private boolean isIncludeIndirect = true;

    public abstract SearchState processEntry(IIpsObjectPathEntry entry);

    public void setIncludeIndirect(boolean isIncludeIndirect) {
        this.isIncludeIndirect = isIncludeIndirect;
    }

    public boolean isIncludeIndirect() {
        return isIncludeIndirect;
    }

    protected boolean isContainerEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_CONTAINER);
    }

    protected boolean isProjectRefEntry(IIpsObjectPathEntry entry) {
        return entry.getType().equals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE);
    }

    protected IIpsProject getReferencedIpsProject(IIpsObjectPathEntry entry) {
        return ((IIpsProjectRefEntry)entry).getReferencedIpsProject();
    }

    public enum SearchState {
        STOP_SEARCH,
        CONTINUE_SEARCH;
    }
}
