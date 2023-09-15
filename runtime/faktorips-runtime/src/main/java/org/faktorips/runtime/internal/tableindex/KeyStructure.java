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

import java.util.HashMap;

/**
 * An implementation of {@link AbstractMapStructure} mapping keys to nested {@link SearchStructure
 * SearchStructures} much like a map. This structure is useful for values that can be identified by
 * their key. The generic types are described in {@link AbstractMapStructure}.
 * 
 * @see AbstractMapStructure
 */
public class KeyStructure<K, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> extends
        AbstractMapStructure<K, V, R> implements MergeAndCopyStructure<KeyStructure<K, V, R>> {

    KeyStructure() {
        super(new HashMap<>());
    }

    /**
     * Creates a new empty {@link KeyStructure}.
     */
    public static <K, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> KeyStructure<K, V, R> create() {
        return new KeyStructure<>();
    }

    /**
     * Creates a new {@link KeyStructure} and put the given key value pair.
     */
    public static <K, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> KeyStructure<K, V, R> createWith(
            K key,
            V value) {
        KeyStructure<K, V, R> structure = new KeyStructure<>();
        structure.put(key, value);
        return structure;
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (key == null) {
            return emptyResult();
        } else {
            V result = getMap().get(key);
            return getValidResult(result);
        }
    }

    @Override
    public void merge(KeyStructure<K, V, R> map) {
        super.merge(map);
    }

    @Override
    public KeyStructure<K, V, R> copy() {
        return fillCopy(new KeyStructure<>());
    }
}
