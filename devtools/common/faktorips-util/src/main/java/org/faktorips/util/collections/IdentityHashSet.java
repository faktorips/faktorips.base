/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.collections;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * Set backed by an {@link IdentityHashMap}, like {@link HashSet} is backed by a {@link HashMap}.
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Cloneable {

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    private transient IdentityHashMap<E, Object> map;

    /**
     * Constructs a new, empty set.
     */
    public IdentityHashSet() {
        map = new IdentityHashMap<>();
    }

    /**
     * Returns an iterator over the elements in this set. The elements are returned in no particular
     * order.
     *
     * @return an {@link Iterator} over the elements in this set
     * @see ConcurrentModificationException
     */
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element. More formally, returns
     * {@code true} if and only if this set contains an element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present. More formally, adds the
     * specified element {@code e} to this set if this set contains no element {@code e2} such that
     * {@code Objects.equals(e, e2)}. If this set already contains the element, the call leaves the
     * set unchanged and returns {@code false}.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified element
     */
    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    /**
     * Removes the specified element from this set if it is present. More formally, removes an
     * element {@code e} such that {@code Objects.equals(o, e)}, if this set contains such an
     * element. Returns {@code true} if this set contained the element (or equivalently, if this set
     * changed as a result of the call). (This set will not contain the element once the call
     * returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if the set contained the specified element
     */
    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    /**
     * Removes all of the elements from this set. The set will be empty after this call returns.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns a shallow copy of this {@code IdentityHashSet} instance: the elements themselves are
     * not cloned.
     *
     * @return a shallow copy of this set
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            IdentityHashSet<E> newSet = (IdentityHashSet<E>)super.clone();
            newSet.map = (IdentityHashMap<E, Object>)map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return map.keySet().spliterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

}
