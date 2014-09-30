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

public class CycleSearch extends AbstractSearch {

    private boolean isCycleDetected = false;
    private final IIpsProject initialProject;

    public CycleSearch(IIpsProject initialProject) {
        this.initialProject = initialProject;
    }

    @Override
    public SearchState processEntry(IIpsObjectPathEntry entry) {
        if (entry.getType().equals(IpsObjectPathEntry.TYPE_PROJECT_REFERENCE)) {
            if (initialProject.equals(((IIpsProjectRefEntry)entry).getReferencedIpsProject())) {
                isCycleDetected = true;
                return SearchState.STOP_SEARCH;
            }
        }
        return SearchState.CONTINUE_SEARCH;
    }

    public boolean isCycleDetected() {
        return isCycleDetected;
    }

}
