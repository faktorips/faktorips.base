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

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A {@link SearchStructure} is a data structure that is used by tables to optimize index key access
 * on their data. The fundamental idea is that a {@link SearchStructure} is a self containing
 * composite, that means every structure contains another structure.
 * <p>
 * For each part of an index a nested {@link SearchStructure} exists. The contained
 * {@link SearchStructure} can be retrieved via the {@link #get(Object)} method, called with the key
 * that is to be searched for. The last call is {@link #get()} which then retrieves the resulting
 * values as a {@link Set}.
 * <p>
 * To prevent {@link NullPointerException}s without checking for <code>null</code>, every
 * {@link #get(Object)} will return a valid {@link SearchStructure}. If there is no nested structure
 * for any given key, an {@link EmptySearchStructure} is returned as a fall-back. Calling
 * {@link #get(Object)} on it (with any key) simply returns the {@link EmptySearchStructure} itself.
 * In other words an {@link EmptySearchStructure} is a kind of null-Object.
 * <p>
 * Example: given a nested structure Map → Tree → Tree and the call
 * <code>get(x).get(y).get(z)</code> on it. If <code>get(x)</code> on the map yields no result an
 * {@link EmptySearchStructure} is returned. Nevertheless the following <code>get(y).get(z)</code>
 * can be called without checking for <code>null</code>. The empty result is returned on every
 * subsequent call.
 * 
 * @param <R> The type of the resulting values.
 * @see ResultStructure
 * 
 * 
 */
public abstract class SearchStructure<R> {

    /**
     * Returns the nested {@link SearchStructure} for the given key. This method never returns
     * <code>null</code>. If no value exists for a given key an {@link EmptySearchStructure} is
     * returned as a fall-back.
     * 
     * @param key The key for the requested nested {@link SearchStructure}
     * @return The nested {@link SearchStructure} or an {@link EmptySearchStructure} if the key does
     *         not exist.
     */
    public abstract SearchStructure<R> get(Object key);

    /**
     * Returns the set of resulting values. If this {@link SearchStructure} is no
     * {@link ResultStructure} this method simply aggregates every nested {@link SearchStructure
     * structures'} results. Beware that the aggregation of nested elements has linear complexity.
     * 
     * @return The set of resulting values that are reachable by this {@link SearchStructure}
     */
    public abstract Set<R> get();

    /**
     * Returns the value if there is exactly one value.
     * <p>
     * Use this method if you know there should be exactly one result value. This method throws an
     * {@link AssertionError} if there is more than one values. If there is no value it throws an
     * {@link NoSuchElementException} exception.
     * 
     * @return The one and only result hold by this {@link SearchStructure}.
     * @throws AssertionError if your assertion that there is at most one element is wrong and hence
     *             there is more than one value.
     * @throws NoSuchElementException If there is no element at all.
     */
    public R getUnique() {
        Set<R> set = get();
        if (set.size() > 1) {
            throw new AssertionError("There are multiple values for a unique key.");
        } else {
            return set.iterator().next();
        }
    }

    /**
     * Returns the value if there is exactly one value or the given defaultValue if this structure
     * is empty.
     * <p>
     * Use this method if you know there should be at most one result value. This method throws an
     * {@link AssertionError} if there is more than one value.
     * 
     * @param defaultValue The defaultValue which is returned if this {@link SearchStructure} is
     *            empty.
     * @return The result hold by this {@link SearchStructure} or the defaultValue if the structure
     *         is empty.
     * @throws AssertionError if your assertion that there is at most one element is wrong and hence
     *             there are more than one result values.
     */
    public R getUnique(R defaultValue) {
        Set<R> set = get();
        if (set.size() > 1) {
            throw new AssertionError("There are multiple values for a unique key.");
        } else if (set.isEmpty()) {
            return defaultValue;
        } else {
            return set.iterator().next();
        }
    }
}