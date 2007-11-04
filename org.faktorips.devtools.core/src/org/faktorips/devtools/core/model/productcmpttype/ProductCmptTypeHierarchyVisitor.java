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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class ProductCmptTypeHierarchyVisitor extends TypeHierarchyVisitor {

    public ProductCmptTypeHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    /**
     * Returns the product component types visited by the visitor in the order they were visited.
     */
    public IProductCmptType[] getVisitedProductCmptTypes() {
        return (IProductCmptType[])visitedTypes.toArray(new IProductCmptType[visitedTypes.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    final protected boolean visit(IType currentType) throws CoreException {
        return visit((IProductCmptType)currentType);
    }

    /**
     * Template method in that subclasses realize the function for the given type.
     * 
     * @return <code>true</code> if the visitor should continue to navigate up the hierarchy.
     * <code>false</code> if not.
     */
    protected abstract boolean visit(IProductCmptType currentType) throws CoreException;
}
