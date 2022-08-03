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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract implementation for all {@link SearchStructure structures} that map keys to nested
 * {@link SearchStructure structures} much like a map.
 * <p>
 * This class provides a {@link #put(Object, SearchStructure)} method to add nested structures.
 * Adding multiple structures with the same key will result in those structures being merged. This
 * makes setting up nested map structures easy.
 * <p>
 * This abstract implementation makes no assumption about the underlying map implementation. The map
 * instance is provided by subclasses via the constructor. Thus the {@link #get(Object)} method
 * implementation must also be provided by subclasses using the chosen data structure.
 * 
 * @param <K> The type of key in the underlying map - can be restricted in subclasses
 * @param <V> The type of values in the map. Values are nested {@link SearchStructure structures}
 *            which must:
 *            <ul>
 *            <li>have the same result type <code>R</code> as this {@link AbstractMapStructure}</li>
 *            <li>implement the {@link MergeAndCopyStructure} interface. The class definition
 *            enforces the implementation of {@link MergeAndCopyStructure} without restricting the
 *            type V to a specific {@link SearchStructure}. Therefore the mergable type is bound to
 *            <code>? super V </code>.</li>
 *            </ul>
 * @param <R> The type of the result values. The result type must be the same in every nested
 *            structure.
 * 
 */
public abstract class AbstractMapStructure<K, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> extends
        SearchStructure<R> {

    private static final SearchStructure<?> EMPTY = new EmptySearchStructure<>();

    private final Map<K, V> map;

    /**
     * Creates a new {@link AbstractMapStructure} with the specified map instance.
     * 
     * @param map The map that should be used as underlying data structure.
     */
    protected AbstractMapStructure(Map<K, V> map) {
        this.map = map;
    }

    /**
     * Puts a new element in the map. If there is already a value for the given key, the two values
     * will be merged together. Thus existing values will never be overwritten.
     * 
     * @param key key that maps to the specified value
     * @param value value to be associated with the specified key.
     * @see Map#put(Object, Object)
     * @see #merge(AbstractMapStructure)
     */
    public void put(K key, V value) {
        if (getMap().containsKey(key)) {
            V myValue = getMap().get(key);
            myValue.merge(value);
        } else {
            getMap().put(key, value);
        }
    }

    protected void merge(AbstractMapStructure<K, V, R> otherMap) {
        Map<K, V> otherUnderlyingMap = otherMap.getMap();
        for (Entry<K, V> entry : otherUnderlyingMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    protected <T extends AbstractMapStructure<K, V, R>> T fillCopy(T structure) {
        for (Entry<K, V> entry : getMap().entrySet()) {
            V value = entry.getValue().copy();
            structure.put(entry.getKey(), value);
        }
        return structure;
    }

    @Override
    public Set<R> get() {
        HashSet<R> resultSet = new HashSet<>();
        for (V value : getMap().values()) {
            Set<R> set = value.get();
            resultSet.addAll(set);
        }
        return resultSet;
    }

    protected Map<K, V> getMap() {
        return map;
    }

    /**
     * Checks whether the given result is <code>null</code> and returns a fall-back result (an empty
     * {@link ResultStructure}) in that case.
     * 
     * @param result a valid result or <code>null</code>.
     * @return the given result or an {@link EmptySearchStructure} if the given result is
     *             <code>null</code>. Never returns <code>null</code>.
     */
    protected SearchStructure<R> getValidResult(V result) {
        if (result == null) {
            return emptyResult();
        } else {
            return result;
        }
    }

    /**
     * Simply returns an {@link EmptySearchStructure}. This is the typesafe representation of
     * {@link #EMPTY}.
     */
    @SuppressWarnings("unchecked")
    protected SearchStructure<R> emptyResult() {
        return (SearchStructure<R>)EMPTY;
    }
}
