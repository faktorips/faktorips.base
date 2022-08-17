/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data structure that maps a key to a collection of values. Adding a value to the map is like
 * putting a value to the collection regarding the specified key. If a value is requested by using
 * the key a collection is returned.
 * <p>
 * Different kinds of {@link MultiMap multi maps} can be created using different
 * {@link CollectionFactory collection factories}, or by calling the creator methods
 * {@link #createWithListsAsValues()} and {@link #createWithSetsAsValues()}. The map then contains
 * only lists or only sets respectively, depending on the use case.
 */
public class MultiMap<K, V> implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -1167623033749612550L;

    private final CollectionFactory<V> collectionFactory;

    private final ConcurrentHashMap<K, Collection<V>> internalMap;

    /**
     * Creates a new {@link MultiMap} with {@link ArrayList} instances as values.
     */
    public MultiMap() {
        this(new ArrayListFactory<V>());
    }

    public MultiMap(CollectionFactory<V> collectionFactory) {
        this.collectionFactory = collectionFactory;
        internalMap = new ConcurrentHashMap<>(16, 0.75f, 1);
    }

    public static <K, V> MultiMap<K, V> createWithSetsAsValues() {
        return new MultiMap<>(new HashSetFactory<V>());
    }

    public static <K, V> MultiMap<K, V> createWithListsAsValues() {
        return new MultiMap<>(new ArrayListFactory<V>());
    }

    public static <K, V> MultiMap<K, V> createWithLinkedSetAsValues() {
        return new MultiMap<>(new LinkedHashSetFactory<V>());
    }

    /**
     * Adds the specified values to the collection the key maps to.
     * 
     * @param key key indicating the target Collection of the specified value that is to be
     *            associated
     * @param values to be integrated in the Collection of the associated key
     */
    @SafeVarargs
    public final void put(K key, V... values) {
        Collection<V> collection = getCollectionInternal(key);
        collection.addAll(Arrays.asList(values));
    }

    /**
     * Adds the specified values to the collection the key maps to.
     * 
     * @param key key indicating the target Collection of the specified value that is to be
     *            associated
     * @param values to be integrated in the Collection of the associated key
     */
    public void put(K key, Collection<V> values) {
        Collection<V> collection = getCollectionInternal(key);
        collection.addAll(values);
    }

    /**
     * Merges the specified multi map to this multi map. That means: for every entry in the other
     * multi map call {@link #put(Object, Collection)}.
     * 
     * @param otherMultiMap The map that should be merged into this multi map
     */
    public void merge(MultiMap<K, V> otherMultiMap) {
        for (Entry<K, Collection<V>> entry : otherMultiMap.internalMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Put the collection of values into the map, identified by the specified key. If the key
     * already maps another collection, the old collection is replaced.
     * 
     * @param key key indicating the specified values in the map
     * @param values The collection of values that should be associated with the key
     */
    public void putReplace(K key, Collection<V> values) {
        Collection<V> newCollection = collectionFactory.createCollection();
        newCollection.addAll(values);
        internalMap.put(key, newCollection);
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
     * Removes the value in the collection the key maps to.
     * 
     * @param key to which the Collection is mapped
     * @param value to be removed from the collection the key maps to
     */
    public void remove(Object key, Object value) {
        Collection<V> collection = internalMap.get(key);
        if (collection != null) {
            collection.remove(value);
        }
    }

    /**
     * Removes key and its associated collection from the map.
     * 
     * @param key to which the Collection is mapped
     * @return The collection that was stored in the list. Returns an empty collection if there was
     *             no such key.
     */
    public Collection<V> remove(Object key) {
        Collection<V> removedCollection = internalMap.remove(key);
        if (removedCollection != null) {
            return removedCollection;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * This method returns collection to which the specified key is mapped. If the key does not
     * exist an empty List is returned.
     * 
     * @param key The key containing the wanted associated Collection
     * @return the collection to which the specified key is mapped
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
     * Returns the key-set of the internal map. Changes to this set (especially removing values) are
     * reflected in this {@link MultiMap} according to {@link Map#keySet()}.
     */
    public Set<K> keySet() {
        return internalMap.keySet();
    }

    public Collection<V> values() {
        Collection<V> collection = collectionFactory.createCollection();
        for (Collection<V> value : internalMap.values()) {
            collection.addAll(value);
        }
        return collection;
    }

    /**
     * Returns the count of objects contained in this multi map. That means the sum of the size of
     * all value collections.
     * 
     * @return The count of objects in this multi map;
     */
    public int count() {
        int count = 0;
        for (Collection<V> collection : internalMap.values()) {
            count += collection.size();
        }
        return count;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        internalMap.clear();
    }

    /**
     * Factory for creating collection instances to be used as values in a {@link MultiMap}.
     */
    public interface CollectionFactory<V> extends Serializable {

        /**
         * Creates collections that are used as values in a {@link MultiMap}
         */
        Collection<V> createCollection();

    }

    /**
     * Creates {@link ArrayList} instances as {@link MultiMap} values.
     */
    public static class ArrayListFactory<V> implements CollectionFactory<V> {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -1282726028020257888L;

        @Override
        public Collection<V> createCollection() {
            return new ArrayList<>();
        }

    }

    /**
     * Creates {@link HashSet} instances as {@link MultiMap} values.
     */
    public static class HashSetFactory<V> implements CollectionFactory<V> {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 2534395916683559784L;

        @Override
        public Collection<V> createCollection() {
            return new HashSet<>();
        }

    }

    /**
     * Creates {@link HashSet} instances as {@link MultiMap} values.
     */
    public static class LinkedHashSetFactory<V> implements CollectionFactory<V> {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 2534395916683559784L;

        @Override
        public Collection<V> createCollection() {
            return new LinkedHashSet<>();
        }

    }

}
