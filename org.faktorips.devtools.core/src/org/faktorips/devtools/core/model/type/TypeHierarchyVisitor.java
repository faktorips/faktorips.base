/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy. It also provides the information if a cycle exists in the hierarchy.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class TypeHierarchyVisitor {

    protected IIpsProject ipsProject;
    protected List visitedTypes;
    private boolean cycleDetected;
    
    /**
     * Constructs a new visitor.
     * 
     * @param ipsProject The project which ips object path is used to search.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public TypeHierarchyVisitor(IIpsProject ipsProject) {
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
    public IType[] getVisitedTypes() {
        return (IType[])visitedTypes.toArray(new IType[visitedTypes.size()]);
    }

    /**
     * Starts the visit on the given type. Does nothing if basetype is <code>null</code>.
     */
    public final void start(IType basetype) throws CoreException {
        cycleDetected = false;
        visitedTypes = new ArrayList();
        if (basetype==null) {
            return;
        }
        visitInternal(basetype);
    }

    private void visitInternal(IType currentType) throws CoreException {
        if (visitedTypes.contains(currentType)) {
            cycleDetected = true;
            return;
        }
        visitedTypes.add(currentType);
        boolean continueVisiting = visit(currentType);
        if (!continueVisiting) {
            return;
        }
        IType supertype = currentType.findSupertype(ipsProject);
        if (supertype!=null) {
            visitInternal(supertype);
        }
    }
    
    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     * <code>false</code> if not.
     */
    protected abstract boolean visit(IType currentType) throws CoreException;
    
}
