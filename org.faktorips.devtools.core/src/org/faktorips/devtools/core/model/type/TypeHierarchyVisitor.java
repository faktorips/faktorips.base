/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A visitor that makes it easy to implement a function based on all types in a supertype hierarchy.
 * The class provides navigation of the supertype hierarchy and stops if a cycle is detected in the
 * type hierarchy. It also provides the information if a cycle exists in the hierarchy.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class TypeHierarchyVisitor extends HierarchyVisitor<IType> {

    /**
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws NullPointerException If <tt>ipsProject</tt> is <code>null</code>.
     */
    public TypeHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    /**
     * Returns the types visited by the visitor in the order they were visited.
     */
    public IType[] getVisitedTypes() {
        return visitedTypes.toArray(new IType[visitedTypes.size()]);
    }

    @Override
    protected IType findSupertype(IType currentType, IIpsProject ipsProject) throws CoreException {
        return currentType.findSupertype(ipsProject);
    }

}
