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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data structure that maps a key to a collection of values. Adding a value to the map is like
 * putting a value to the collection regarding the specified key. If a value is requested by using
 * the key a Collection is returned.
 */
public class MultiMap<K, V> {

    private final ConcurrentHashMap<K, Collection<V>> internalMap;

    private final CollectionFactory<V> collectionFactory;

    /**
     * Creates a new MultiMap with ArrayList as value collections.
     */
    public MultiMap() {
        this(new ArrayListFactory<V>());
    }

    public MultiMap(CollectionFactory<V> collectionFactory) {
        this.collectionFactory = collectionFactory;
        internalMap = new ConcurrentHashMap<K, Collection<V>>(16, 0.75f, 1);
    }

    public static <K, V> MultiMap<K, V> createWithSetsAsValues() {
        MultiMap<K, V> multiMap = new MultiMap<K, V>(new HashSetFactory<V>());
        return multiMap;
    }

    public static <K, V> MultiMap<K, V> createWithListsAsValues() {
        MultiMap<K, V> multiMap = new MultiMap<K, V>(new ArrayListFactory<V>());
        return multiMap;
    }

    /**
     * Adds the specified value to the collection the key maps to.
     * 
     * @param key key indicating the target Collection of the specified value that is to be
     *            associated
     * @param value to be integrated in the Collection of the associated key
     */
    public void put(K key, V value) {
        Collection<V> collection = getCollectionInternal(key);
        collection.add(value);
    }

    /**
     * Adds the collection of values to the collection the key maps to.
     * 
     * @param key key indicating the target Collection of the specified value that is to be
     *            associated
     * @param values The collection of values that should be merged to the maybe existing values
     *            that are mapped by the key.
     */
    public void putAll(K key, Collection<V> values) {
        Collection<V> collection = getCollectionInternal(key);
        collection.addAll(values);
    }

    public void putAll(MultiMap<K, V> otherMultiMap) {
        for (Entry<K, Collection<V>> entry : otherMultiMap.internalMap.entrySet()) {
            putAll(entry.getKey(), entry.getValue());
        }
    }

    private Collection<V> getCollectionInternal(K key) {
        Collection<V> collection = internalMap.get(key);
        if (collection == null) {
            return createNewValue(key);
        } else {
            return collection;
        }
    }

    private Collection<V> createNewValue(K key) {
        Collection<V> collection = collectionFactory.createCollection();
        Collection<V> exitingValue = internalMap.putIfAbsent(key, collection);
        if (exitingValue != null) {
            return exitingValue;
        } else {
            return collection;
        }
    }

    /**
     * Removes the value in the collection the key maps to. If the Collection in the
     * <code>internalMap</code> is empty after the removal, the key will be removed too.
     * <p>
     * The method needs to be manually synchronized because it would delete the collection if it is
     * empty after removing the value.
     * 
     * @param key to which the Collection is mapped
     * @param value to be removed from the collection the key maps to
     */
    public void remove(Object key, Object value) {
        synchronized (this) {
            Collection<V> collection = internalMap.get(key);
            if (collection != null) {
                collection.remove(value);
                if (collection.isEmpty()) {
                    internalMap.remove(key);
                }
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
     * Returns the keySet of the internal map. Changes to this set (especially removing values) are
     * reflected in this {@link MultiMap} according to {@link Map#keySet()}.
     * 
     */
    public Set<K> keySet() {
        return internalMap.keySet();
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        internalMap.clear();
    }

    /**
     * An implementation of this interface creates a collection for the value of the
     * {@link MultiMap}.
     */
    public static interface CollectionFactory<V> {

        /**
         * Create a new collection for the multi map value.
         */
        public Collection<V> createCollection();

    }

    /**
     * This implementation of CollectionFactory creates {@link ArrayList} instances.
     * 
     */
    public static class ArrayListFactory<V> implements CollectionFactory<V> {

        @Override
        public Collection<V> createCollection() {
            return new ArrayList<V>();
        }

    }

    /**
     * This implementation of CollectionFactory creates {@link HashSet} instances.
     * 
     */
    public static class HashSetFactory<V> implements CollectionFactory<V> {

        @Override
        public Collection<V> createCollection() {
            return new HashSet<V>();
        }

    }

}
