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

package org.faktorips.devtools.core.model.productcmpttype2;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class ProductCmptTypeHierarchyVisitor {

    protected IIpsProject ipsProject;
    
    public ProductCmptTypeHierarchyVisitor(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
    }

    /**
     * Starts the visit on the given type. Does nothing if basetype is <code>null</code>.
     */
    public final void start(IProductCmptType basetype) throws CoreException {
        if (basetype==null) {
            return;
        }
        visit(basetype, new HashSet());
    }

    private void visit(IProductCmptType currentType, Set typesHandled) throws CoreException {
        boolean continueVisiting = visit(currentType);
        if (!continueVisiting) {
            return;
        }
        IProductCmptType supertype = currentType.findSuperProductCmptType(ipsProject);
        if (supertype!=null && !typesHandled.contains(supertype)) {
            typesHandled.add(supertype);
            visit(supertype, typesHandled);
        }
    }
    
    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     * <code>false</code> if not.
     */
    protected abstract boolean visit(IProductCmptType currentType) throws CoreException;
}
