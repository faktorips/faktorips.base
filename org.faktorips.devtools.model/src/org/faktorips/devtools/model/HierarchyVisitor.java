/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A visitor that makes it easy to implement a function based on all types in a super type
 * hierarchy. The class provides navigation of the super type hierarchy and stops if a cycle is
 * detected in the type hierarchy. It also provides the information if a cycle exists in the
 * hierarchy.
 * <p>
 * The visitor visits the hierarchy bottom-up, that means the start type is visited first, followed
 * by the direct supertype.
 * 
 * @since 2.3
 * 
 * @author Jan Ortmann
 * @author Peter Kuntz
 */
public abstract class HierarchyVisitor<T> {

    private final IIpsProject ipsProject;
    private LinkedHashSet<T> visitedTypes;
    private boolean cycleDetected;

    /**
     * Constructs a new visitor.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public HierarchyVisitor(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
    }

    /**
     * Returns <code>true</code> if a cycle was detected in the type hierarchy.
     */
    public boolean cycleDetected() {
        return cycleDetected;
    }

    /**
     * Returns the types visited by the visitor in the order they were visited.
     */
    public List<T> getVisited() {
        return Collections.unmodifiableList(new ArrayList<>(visitedTypes));
    }

    public boolean isAlreadyVisited(T object) {
        return getVisited().contains(object);
    }

    /**
     * Starts the visit on the given type. Does nothing if base type is <code>null</code>.
     * <p>
     * If you need to override this method you have to call always this super implementation.
     */
    public void start(T basetype) {
        cycleDetected = false;
        visitedTypes = new LinkedHashSet<>();
        if (basetype == null) {
            return;
        }
        visitInternal(basetype);
    }

    private void visitInternal(T currentType) {
        if (isAlreadyVisited(currentType)) {
            cycleDetected = true;
            return;
        }
        visitedTypes.add(currentType);
        boolean continueVisiting = visit(currentType);
        if (!continueVisiting) {
            return;
        }
        T supertype = findSupertype(currentType, ipsProject);
        if (supertype != null) {
            visitInternal(supertype);
        }
    }

    protected IIpsProject getIpsProject() {
        return ipsProject;
    }

    protected LinkedHashSet<T> getVisitedTypesAsSet() {
        return visitedTypes;
    }

    protected abstract T findSupertype(T currentType, IIpsProject ipsProject);

    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     *             <code>false</code> if not.
     */
    protected abstract boolean visit(T currentType);

}
