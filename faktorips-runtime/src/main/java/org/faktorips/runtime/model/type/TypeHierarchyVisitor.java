/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor that allows to implement functions on a type's hierarchy. As we don't have an explicit
 * type hierarchy class, not the typical implementation with accept/visit methods, but still a
 * visitor.
 * 
 * @author Jan Ortmann
 */
public abstract class TypeHierarchyVisitor {

    private Set<Type> visitedTypes = new HashSet<>();

    public TypeHierarchyVisitor() {
        super();
    }

    /**
     * Visits the given type and all it's super types. Does nothing if the type is <code>null</code>
     * .
     * 
     */
    public void visitHierarchy(Type type) {
        visitTypeInternal(type);
    }

    private void visitTypeInternal(Type type) {
        if ((type == null) || !visitType(type)) {
            return;
        }
        visitedTypes.add(type);
        type.findSuperType().ifPresent(superType -> {
            if (visitedTypes.contains(superType)) {
                throw new RuntimeException("TypeHierarchy of type " + superType + " contains a cycle.");
            }
            visitTypeInternal(superType);
        });
    }

    /**
     * Visits the given type.
     * 
     * @param type The type to visit.
     * @return <code>true</code> if the visitor should continue to visit the supertypes,
     *             <code>false</code> if not.
     */
    public abstract boolean visitType(Type type);
}
