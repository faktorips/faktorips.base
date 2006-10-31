/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class PolicyCmptTypeHierarchyVisitor {

    public PolicyCmptTypeHierarchyVisitor() {
    }

    /**
     * Starts the visit on the given type. Does nothing if basetype is <code>null</code>.
     */
    public final void start(IPolicyCmptType basetype) throws CoreException {
        if (basetype==null) {
            return;
        }
        visit(basetype, new HashSet());
    }

    private void visit(IPolicyCmptType currentType, Set typesHandled) throws CoreException {
        visit(currentType);
        IPolicyCmptType supertype = currentType.findSupertype();
        if (supertype!=null && !typesHandled.contains(supertype)) {
            typesHandled.add(supertype);
            visit(supertype, typesHandled);
        }
    }
    
    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navgiate up the hierarchy.
     * <code>false</code> if not.
     */
    protected abstract boolean visit(IPolicyCmptType currentType);
}
