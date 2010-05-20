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

package org.faktorips.devtools.core.model.pctype;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.type.IType;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class PolicyCmptTypeHierarchyVisitor {

    /**
     * Starts the visit on the given type. Does nothing if base type is <code>null</code>.
     */
    public final void start(IPolicyCmptType basetype) throws CoreException {
        if (basetype == null) {
            return;
        }
        visit(basetype, new HashSet<IType>());
    }

    private void visit(IPolicyCmptType currentType, Set<IType> typesHandled) throws CoreException {
        boolean continueVisiting = visit(currentType);
        if (!continueVisiting) {
            return;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)currentType.findSupertype(currentType.getIpsProject());
        if (supertype != null && !typesHandled.contains(supertype)) {
            typesHandled.add(supertype);
            visit(supertype, typesHandled);
        }
    }

    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     *         <code>false</code> if not.
     */
    protected abstract boolean visit(IPolicyCmptType currentType) throws CoreException;

}
