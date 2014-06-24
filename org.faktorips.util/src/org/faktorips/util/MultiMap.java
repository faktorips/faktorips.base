/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A {@link MultiMap} is a Map holding a key and an associated Collection. Adding a value to the map
 * is like putting a value to the collection regarding the specified key. If a value is wanted by
 * using the key a Collection will be returned.
 * 
 */
public class MultiMap<K, V> {

    private HashMap<K, Collection<V>> internalMap;

    public MultiMap() {
        internalMap = new HashMap<K, Collection<V>>();
    }

    public MultiMap(int initialCapacity) {
        internalMap = new HashMap<K, Collection<V>>(initialCapacity);
    }

    /**
     * 
     * Integrates the specified value in the Collection of the <code>internlMap</code> by using the
     * specified key.
     * 
     * @param key key indicating the target Collection of the specified value that is to be
     *            associated
     * @param value to be integrated in the Collection of the associated key
     */
    public void put(K key, V value) {
        Collection<V> collection = internalMap.get(key);
        if (collection == null) {
            collection = new ArrayList<V>(1);
            internalMap.put(key, collection);
        }
        collection.add(value);
    }

    /**
     * Removes the mapping for the specified value from this map if present. If the Collection in
     * the <code>internalMap</code> is empty after the removal, the key will be removed too.
     * 
     * @param key key whose mapping is to be removed from the map
     */
    public void remove(Object key, Object value) {
        Collection<V> collection = internalMap.get(key);
        if (collection != null) {
            collection.remove(value);
            if (collection.isEmpty()) {
                internalMap.remove(key);
            }
        }
    }

    /**
     * This method returns collection to which the specified key is mapped. If the key does not
     * exist <code>null</code> is returned.
     * 
     * @param key The key containing the wanted associated Collection
     * @return Collection<V> the collection to which the specified key is mapped
     */
    public Collection<V> get(Object key) {
        return internalMap.get(key);
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        internalMap.clear();
    }

}
