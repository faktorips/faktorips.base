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

package org.faktorips.runtime.modeltype;

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor that allows to implement functions on a type's hierarchy. As we don't have an explizit
 * type hierarchy class, not the typicall implementation with accept/visit methods, but still a
 * visitor.
 * 
 * @author Jan Ortmann
 */
public abstract class TypeHierarchyVisitor {

    private Set<IModelType> visitedTypes = new HashSet<IModelType>();

    public TypeHierarchyVisitor() {
        super();
    }

    /**
     * Visits the given type and all it's super types. Does nothing if the type is <code>null</code>
     * .
     * 
     */
    public void visitHierarchy(IModelType type) {
        visitTypeInternal(type);
    }

    private void visitTypeInternal(IModelType type) {
        if (type == null) {
            return;
        }
        if (!visitType(type)) {
            return;
        }
        visitedTypes.add(type);
        type = type.getSuperType();
        if (visitedTypes.contains(type)) {
            throw new RuntimeException("TypeHierarchy of type " + type + " contains a cycle.");
        }
        visitTypeInternal(type);
    }

    /**
     * Visits the given type.
     * 
     * @param type The type to visit.
     * @return <code>true</code> if the visitor should continue to visit the supertypes,
     *         <code>false</code> if not.
     */
    public abstract boolean visitType(IModelType type);
}
