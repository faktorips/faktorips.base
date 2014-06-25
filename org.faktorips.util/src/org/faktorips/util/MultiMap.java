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
import java.util.Collections;
import java.util.HashMap;

/**
 * A data structure that maps a key to a collection of values. Adding a value to the map is like
 * putting a value to the collection regarding the specified key. If a value is requested by using
 * the key a Collection is returned.
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
     * Adds the specified value to the collection the key maps to.
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
     * Removes the value in the collection the key maps to. If the Collection in the
     * <code>internalMap</code> is empty after the removal, the key will be removed too.
     * 
     * @param key to which the Collection is mapped
     * @param value to be removed from the collection the key maps to
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
     * exist an empty List is returned.
     * 
     * @param key The key containing the wanted associated Collection
     * @return Collection<V> the collection to which the specified key is mapped
     */
    public Collection<V> get(Object key) {
        Collection<V> collection = internalMap.get(key);
        if (collection == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(collection);
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        internalMap.clear();
    }

}
