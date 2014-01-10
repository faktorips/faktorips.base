/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
