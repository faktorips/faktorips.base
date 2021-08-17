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

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;

/**
 * Created with an initial project this class searches for {@link IIpsProjectRefEntry project ref
 * entries} that references the initial project. In this case a cycle is detected, thusly
 * {@link #isCycleDetected()} returns <code>true</code> after performing the search.
 */
public class CycleSearch extends AbstractSearch {

    private boolean isCycleDetected = false;
    private final IIpsProject initialProject;

    public CycleSearch(IIpsProject initialProject) {
        if (initialProject == null) {
            throw new IllegalArgumentException("The initial project must not be null."); //$NON-NLS-1$
        }
        this.initialProject = initialProject;
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        if (isProjectRefEntry(entry)) {
            if (initialProject.equals(getReferencedIpsProject(entry))) {
                isCycleDetected = true;
                setStopSearch();
            }
        }
    }

    /**
     * Returns <code>true</code> if a cycle was detected during a search, <code>false</code> else.
     */
    public boolean isCycleDetected() {
        return isCycleDetected;
    }

}
