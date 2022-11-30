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
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This implementation of {@link MultiMap} uses a sorted map and set.
 * <p>
 * Note that this implementation is not synchronized. If multiple threads access a tree set
 * concurrently, and at least one of the threads modifies the set, it must be synchronized
 * externally.
 * 
 * @since 23.6
 */
public class SortedMultiMap<K, V> extends MultiMap<K, V> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a multi map with natural ordering for the keys and the values.
     */
    public SortedMultiMap() {
        super(new TreeMap<K, Collection<V>>(), new SortedSetFactory<V>());
    }

    /**
     * Creates a multi map with the given {@link Comparator Comparators}.
     * 
     * @param keyComparator this comparator is used to sort the keys
     * @param valueComparator this comparator is used to sort the values
     */
    public SortedMultiMap(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        super(
                Collections.synchronizedSortedMap(new TreeMap<K, Collection<V>>(keyComparator)),
                new SortedSetFactory<>(valueComparator));

    }

    /**
     * Creates {@link TreeSet} instances as {@link MultiMap} values.
     */
    public static class SortedSetFactory<V> implements CollectionFactory<V> {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 2534395916683559784L;

        private Comparator<? super V> valueComparator;

        /**
         * Creates a sorted set with a {@code null} {@link Comparator}, therefore using the natural
         * order of its values.
         */
        public SortedSetFactory() {
            valueComparator = null;
        }

        /**
         * Creates a sorted set with the given comparator.
         * 
         * @param valueComparator a comparator
         */
        public SortedSetFactory(Comparator<? super V> valueComparator) {
            this.valueComparator = valueComparator;
        }

        @Override
        public Collection<V> createCollection() {
            return new TreeSet<>(valueComparator);
        }
    }
}
