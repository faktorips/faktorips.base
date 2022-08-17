/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link UniqueResultStructure UniqueResultStructures} are the leaves in the tree of nested
 * {@link SearchStructure SearchStructures}. Each {@link UniqueResultStructure} defines a result of
 * a search. It is designed to hold exactly one value, in contrast to {@link ResultStructure
 * ResultStructures}. Because of this {@link UniqueResultStructure UniqueResultStructures} throw an
 * {@link UnsupportedOperationException} when trying to merge them. The benefit is that setting up a
 * {@link SearchStructure} with ambiguous results provokes an exception, even before it is put to
 * use.
 */
public class UniqueResultStructure<R> extends SearchStructure<R> implements
        MergeAndCopyStructure<UniqueResultStructure<R>> {

    private final R uniqueResult;

    UniqueResultStructure(R result) {
        if (result == null) {
            throw new NullPointerException("Result value must not be null");
        }
        uniqueResult = result;
    }

    /**
     * Creates a new {@link ResultSet} with the given resultValue as its only result value. The
     * resultValue must not be null.
     */
    public static <R> UniqueResultStructure<R> createWith(R resultValue) {
        return new UniqueResultStructure<>(resultValue);
    }

    @Override
    public SearchStructure<R> get(Object key) {
        return this;
    }

    @Override
    public Set<R> get() {
        HashSet<R> result = new HashSet<>();
        result.add(uniqueResult);
        return result;
    }

    @Override
    public void merge(UniqueResultStructure<R> otherStructure) {
        throw new UnsupportedOperationException("Unique key violation: " + this + " cannot be merged with "
                + otherStructure.uniqueResult + "");
    }

    @Override
    public R getUnique(R defaultValue) {
        return uniqueResult;
    }

    @Override
    public R getUnique() {
        return uniqueResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + uniqueResult.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        @SuppressWarnings("unchecked")
        UniqueResultStructure<R> other = (UniqueResultStructure<R>)obj;
        return other.uniqueResult.equals(uniqueResult);
    }

    @Override
    public String toString() {
        return "UniqueResultStructure [" + uniqueResult + "]";
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link UniqueResultStructure} is immutable. Hence it is not needed to create a copy.
     */
    @Override
    public UniqueResultStructure<R> copy() {
        return this;
    }

}
