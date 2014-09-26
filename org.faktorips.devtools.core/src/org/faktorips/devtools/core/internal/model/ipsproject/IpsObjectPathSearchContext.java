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

import java.util.HashSet;

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * The {@link IpsObjectPathSearchContext} stores different {@link IpsObjectPathEntry}s within an
 * {@link IIpsObjectPath} in a set. It also stores the name of the project where a method was called
 * the first time. The initial project name is used to distinct if there is an initial or a
 * subsequent method call.
 */
public class IpsObjectPathSearchContext {

    private final String nameOfInitialProject;
    private HashSet<IIpsObjectPathEntry> visitedEntries;

    public IpsObjectPathSearchContext(IIpsProject initialProject) {
        if (initialProject == null || initialProject.getName() == null) {
            throw new IllegalArgumentException("The name of initial project must not be null."); //$NON-NLS-1$
        }
        visitedEntries = new HashSet<IIpsObjectPathEntry>();
        nameOfInitialProject = initialProject.getName();
    }

    private IpsObjectPathSearchContext(String nameOfInitialProject2, HashSet<IIpsObjectPathEntry> visitedEntries2) {
        visitedEntries = new HashSet<IIpsObjectPathEntry>(visitedEntries2);
        nameOfInitialProject = nameOfInitialProject2;
    }

    /**
     * Returns the name of the initial project. This is the project where a method (for example
     * <code>findIpsSourceFile()</code>) was called the first time.
     */
    private String getNameOfInitialProject() {
        return nameOfInitialProject;
    }

    /**
     * Returns <code>true</code> if the content of the entry shall be considered and if this entry
     * doesn't yet exist in the set of <code>visitedEntries</code> in this
     * {@link IpsObjectPathSearchContext}. Otherwise it returns <code>false</code>.
     */
    public boolean visitAndConsiderContentsOf(IIpsObjectPathEntry entry) {
        return visit(entry) && considerContentsOf(entry);
    }

    /**
     * Checks whether the given entry has been visited yet and caches it as visited at the same
     * time. If the entry has already been visited, <code>false</code> is returned. If the entry has
     * not yet been visited, it is registered as a visited entry and <code>true</code> is returned.
     * 
     * @param entry the entry to be visited
     * @return whether the entry can be visited.
     */
    public boolean visit(IIpsObjectPathEntry entry) {
        boolean canVisit = canVisit(entry);
        addAsVisited(entry);
        return canVisit;
    }

    private boolean canVisit(IIpsObjectPathEntry entry) {
        return !visitedEntries.contains(entry);
    }

    private void addAsVisited(IIpsObjectPathEntry entry) {
        visitedEntries.add(entry);
    }

    /**
     * Returns <code>true</code> if the content of this entry is applicable. If there is no initial
     * call, or the entry shall not be considered, this method returns <code>true</code>.
     */
    public boolean considerContentsOf(IIpsObjectPathEntry entry) {
        return isInitialCall(entry) || entry.isReexported();
    }

    /**
     * Returns <code>true</code> if the initial project and the project of the
     * {@link IIpsObjectPathEntry} are the same. This case indicates an initial method call. If the
     * projects are not the same <code>false</code> is returned, which indicates a subsequent method
     * call.
     */
    /* private */boolean isInitialCall(IIpsObjectPathEntry entry) {
        return getNameOfInitialProject().equals(entry.getIpsProject().getName());
    }

    /**
     * Returns a copy of this {@link IpsObjectPathSearchContext}.
     */
    public IpsObjectPathSearchContext getCopy() {
        return new IpsObjectPathSearchContext(nameOfInitialProject, visitedEntries);
    }

}
