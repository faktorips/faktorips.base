/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

/**
 * A finder class used to find the most specific common type for a collection of types.
 * 
 * @param <T> the type this finder finds
 */
public class CommonTypeFinder<T extends IType> {

    private final IIpsProject ipsProject;

    /**
     * Returns a new finder that finds that uses the given IPS project to search types in.
     * 
     * @param ipsProject the IPS project to search types in
     */
    public CommonTypeFinder(IIpsProject ipsProject) {
        super();
        this.ipsProject = Preconditions.checkNotNull(ipsProject);
    }

    /**
     * Returns the most specific type that is common to all given types. Returns {@code null} if no
     * types are given or the given types do not have a common type.
     * 
     * @param types the types to find the most specific common type in
     * @return the most specific type that is common to all given type; {@code null} if no types are
     *         given or the given types do not have a common type.
     */
    public T findCommonType(Collection<T> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }

        Iterator<T> typesIterator = types.iterator();
        LinkedHashSet<T> candidates = createTypeHierarchy(typesIterator.next());
        while (typesIterator.hasNext()) {
            T type = typesIterator.next();
            T matchingCandidate = findMostSpecificType(candidates, type);
            if (matchingCandidate == null) {
                return null;
            }
            removeSubtypes(candidates, matchingCandidate);
        }
        // should not happen...
        if (candidates.isEmpty()) {
            return null;
        } else {
            return candidates.iterator().next();
        }
    }

    /**
     * Removes all sub types of the given type from the given set and returns the set. This method
     * assumes that the types in the set are iterated from the most specific type to the most common
     * type (and thus explicitly uses a {@code LinkedHashSet}).
     * 
     * @param types a set of types that iterates from the most specific type to the most common type
     * @param t the type whose sub types should be removed
     * @return the given set without the sub types where
     */
    private LinkedHashSet<T> removeSubtypes(LinkedHashSet<T> types, T t) {
        Iterator<T> iterator = types.iterator();
        while (iterator.hasNext() && !iterator.next().equals(t)) {
            iterator.remove();
        }
        return types;
    }

    /**
     * Returns the given type if it contained in the set or the given type's or most specific super
     * type contained in the set. If the set contains neither the given type nor one of its super
     * types, {@code null} is returned.
     * 
     * @param types a set of types
     * @param t the type whose matching type is searched
     * @return the given type if it contained in the set; the given type's or most specific super
     *         type contained in the set; {@code null} if the set contains neither the given type
     *         nor one of its super types
     */
    private T findMostSpecificType(Set<T> types, T t) {
        if (t == null) {
            return null;
        }
        if (types.contains(t)) {
            return t;
        }
        @SuppressWarnings("unchecked")
        T supertype = (T)t.findSupertype(ipsProject);
        return findMostSpecificType(types, supertype);
    }

    /**
     * Creates a set containing the "upward" type hierarchy starting with the given type. The
     * elements in the set are iterated from the given type to its super-types.
     * 
     * @param t a type
     * @return a {@code LinkedHashSet} containing the "upward" type hierarchy starting with the
     *         given type. The elements in the set are iterated in from the given type to its
     *         super-types
     */
    private LinkedHashSet<T> createTypeHierarchy(T t) {
        TypeHierarchyVisitor<T> superTypeFinder = new TypeHierarchyFinder(ipsProject);
        superTypeFinder.start(t);
        return new LinkedHashSet<T>(superTypeFinder.getVisited());
    }

    /**
     * Creates a new finder that searches for types in the given IPS project.
     * 
     * @param ipsProject the IPS project to search types in
     * @return a new finder that searches for types in the given IPS project.
     */
    public static <U extends IType> CommonTypeFinder<U> in(IIpsProject ipsProject) {
        return new CommonTypeFinder<U>(ipsProject);
    }

    /** A {@link TypeHierarchyVisitor} that collects the entire hierarchy upwards from a given type. */
    private class TypeHierarchyFinder extends TypeHierarchyVisitor<T> {

        public TypeHierarchyFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(T currentType) {
            return true;
        }

    }

}
