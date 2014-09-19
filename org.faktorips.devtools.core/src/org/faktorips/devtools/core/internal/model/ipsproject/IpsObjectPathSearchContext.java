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

/**
 * The {@link IpsObjectPathSearchContext} stores different {@link IpsObjectPathEntry} within an
 * {@link IIpsObjectPath} in a set. {@link IpsObjectPathSearchContext} can have different states
 * which is amongst others an important indicator to decide whether an {@link IpsObjectPathEntry}
 * shall be stored in the given set or not. Keep in mind, that the
 * {@link IpsObjectPathSearchContext} can not return to it's <code>INITIALIZED_STATE</code> after
 * changing it to <code>SUBSEQUENT_STATE</code>.
 */
public class IpsObjectPathSearchContext {

    private State state;
    private HashSet<IIpsObjectPathEntry> visitedEntries;

    public IpsObjectPathSearchContext() {
        visitedEntries = new HashSet<IIpsObjectPathEntry>();
        setState(State.INITIAL_CALL);
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
     * Returns <code>true</code> if the content of this entry is applicable. If this
     * {@link IpsObjectPathSearchContext} is not in it's initial state, or the entry shall not be
     * considered, this method returns <code>true</code>.
     */
    public boolean considerContentsOf(IIpsObjectPathEntry entry) {
        return isInitialCall() || entry.isReexported();
    }

    /* private */boolean isInitialCall() {
        return getState() == State.INITIAL_CALL;
    }

    private State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
    }

    /**
     * Sets the current State of this {@link IpsObjectPathSearchContext} to
     * <code>SUBSEQUENT_CALL</code>
     */
    public void setSubsequentCall() {
        setState(State.SUBSEQUENT_CALL);
    }

    /**
     * Indicating the State of an {@link IpsObjectPathSearchContext}. The initial state is specified
     * by <code>INITIAL_CALL</code>.
     */
    private enum State {
        INITIAL_CALL,
        SUBSEQUENT_CALL;
    }

}
