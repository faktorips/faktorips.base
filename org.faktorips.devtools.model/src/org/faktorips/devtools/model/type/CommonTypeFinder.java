/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * A finder class used to find the most specific common type for a collection of product components.
 * 
 */
public class CommonTypeFinder {

    /**
     * Returns the most specific type that is common to all given product components. Returns
     * {@code null} if no product components are given or the given product components do not have a
     * common type.
     * 
     * @param cmpts the product components to find the most specific common type
     * @return the most specific type that is common to all given product components; {@code null}
     *         if no product components are given or the given product components do not have a
     *         common type.
     */
    public IProductCmptType findCommonType(Collection<IProductCmpt> cmpts) {
        if (cmpts == null || cmpts.isEmpty()) {
            return null;
        }

        Iterator<IProductCmpt> cmptsIterator = cmpts.iterator();
        LinkedHashSet<IProductCmptType> candidates = createTypeHierarchy(cmptsIterator.next());
        while (cmptsIterator.hasNext()) {
            IProductCmpt nextCmpt = cmptsIterator.next();
            IProductCmptType matchingCandidate = findMostSpecificType(candidates, nextCmpt);
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
    private LinkedHashSet<IProductCmptType> removeSubtypes(LinkedHashSet<IProductCmptType> types, IProductCmptType t) {
        Iterator<IProductCmptType> iterator = types.iterator();
        while (iterator.hasNext() && !iterator.next().equals(t)) {
            iterator.remove();
        }
        return types;
    }

    /**
     * Returns the type of the given product component if it is contained in the set of candidates
     * or the most specific super type contained in the set. If the set contains neither the type of
     * the given product component nor one of its super types, {@code null} is returned.
     * 
     * @param candidateTypes the potential common type candidates
     * @param cmpt the product component whose matching type is searched
     * @return the given type if it contained in the set; the given type's or most specific super
     *         type contained in the set; {@code null} if the set contains neither the given type
     *         nor one of its super types
     */
    private IProductCmptType findMostSpecificType(Set<IProductCmptType> candidateTypes, IProductCmpt cmpt) {
        CommonTypeVisitor<IProductCmptType> typeHierarchyVisitor = new CommonTypeVisitor<>(
                cmpt.getIpsProject(), candidateTypes);
        typeHierarchyVisitor.start(cmpt.findProductCmptType(cmpt.getIpsProject()));
        return typeHierarchyVisitor.getCommonType();
    }

    /**
     * Creates a set containing the "upward" type hierarchy starting with the given type. The
     * elements in the set are iterated from the given type to its super-types.
     * 
     * @param cmpt a product component
     * @return a {@code LinkedHashSet} containing the "upward" type hierarchy starting with the
     *         given type. The elements in the set are iterated in from the given type to its
     *         super-types
     */
    private LinkedHashSet<IProductCmptType> createTypeHierarchy(IProductCmpt cmpt) {
        TypeHierarchyVisitor<IProductCmptType> superTypeFinder = new TypeHierarchyFinder<>(
                cmpt.getIpsProject());
        superTypeFinder.start(cmpt.findProductCmptType(cmpt.getIpsProject()));
        return new LinkedHashSet<>(superTypeFinder.getVisited());
    }

    /**
     * Determines the type of the given product components and finds the common type using
     * {@link #findCommonType(Collection)}. Product components without type are ignored.
     * 
     * @return the common type of the specified list of product components.
     */
    public static IProductCmptType commonTypeOf(Collection<IProductCmpt> prodctCmpts) {
        return new CommonTypeFinder().findCommonType(prodctCmpts);
    }

    private static class CommonTypeVisitor<T extends IType> extends TypeHierarchyVisitor<T> {

        private final Set<T> candidateTypes;

        private T commonType = null;

        private CommonTypeVisitor(IIpsProject ipsProject, Set<T> candidateTypes) {
            super(ipsProject);
            this.candidateTypes = candidateTypes;
        }

        public T getCommonType() {
            return commonType;
        }

        @Override
        protected boolean visit(T currentType) {
            boolean isCommonType = candidateTypes.contains(currentType);
            if (isCommonType) {
                commonType = currentType;
            }
            return !isCommonType;
        }
    }

    /**
     * A {@link TypeHierarchyVisitor} that collects the entire hierarchy upwards from a given type.
     */
    private static class TypeHierarchyFinder<T extends IType> extends TypeHierarchyVisitor<T> {

        public TypeHierarchyFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(T currentType) {
            return true;
        }

    }

}
