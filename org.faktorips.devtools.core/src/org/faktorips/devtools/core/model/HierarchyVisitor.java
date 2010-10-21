/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    protected IIpsProject ipsProject;
    protected List<T> visitedTypes;
    private boolean cycleDetected;

    /**
     * Constructs a new visitor.
     * 
     * @param ipsProject The project which ips object path is used to search.
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
        return Collections.unmodifiableList(visitedTypes);
    }

    /**
     * Starts the visit on the given type. Does nothing if base type is <code>null</code>.
     */
    public final void start(T basetype) throws CoreException {
        cycleDetected = false;
        visitedTypes = new ArrayList<T>();
        if (basetype == null) {
            return;
        }
        visitInternal(basetype);
    }

    private void visitInternal(T currentType) throws CoreException {
        if (visitedTypes.contains(currentType)) {
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

    protected abstract T findSupertype(T currentType, IIpsProject ipsProject) throws CoreException;

    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     *         <code>false</code> if not.
     */
    protected abstract boolean visit(T currentType) throws CoreException;

}
