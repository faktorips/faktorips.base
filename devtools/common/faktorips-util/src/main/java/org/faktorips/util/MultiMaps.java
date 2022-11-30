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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * MultiMap utilities
 * 
 * @since 23.6.0
 * 
 */
public class MultiMaps {

    private MultiMaps() {
        // utility class
    }

    /**
     * Returns an unmodifiable view of the specified multi map. Query operations on the returned
     * multi map "read through" to the specified multi map. Attempts to modify the returned multi
     * map, result in an {@code UnsupportedOperationException}.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param map the multi map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified multi map.
     */
    public static <K, V> MultiMap<K, V> unmodifiableMultimap(MultiMap<K, V> map) {
        return new UnmodifiableMultiMap<>(map);
    }

    private static class UnmodifiableMultiMap<K, V> extends MultiMap<K, V> {

        private static final long serialVersionUID = 1L;
        private MultiMap<K, V> map;

        public UnmodifiableMultiMap(MultiMap<K, V> map) {
            if (map == null) {
                throw new NullPointerException();
            }
            this.map = map;
        }

        @Override
        public Collection<V> get(Object key) {
            return map.get(key);
        }

        @Override
        public Map<K, Collection<V>> asMap() {
            return map.asMap();
        }

        @Override
        public int count() {
            return map.count();
        }

        @Override
        public Set<K> keySet() {
            return map.keySet();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Collection<V> values() {
            return map.values();
        }

        @Override
        public String toString() {
            return map.toString();
        }

        @Override
        public void put(K key, Collection<V> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void merge(MultiMap<K, V> otherMultiMap) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putReplace(K key, Collection<V> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<V> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

}
