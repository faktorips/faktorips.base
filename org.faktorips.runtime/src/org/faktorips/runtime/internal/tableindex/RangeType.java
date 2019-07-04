/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Defines how the bounds of ranges in a {@link RangeStructure} are handled. Ranges in a
 * {@link RangeStructure} are defined by only one value. The {@link RangeType} specifies whether
 * this value is the upper or lower bound of the range. Ranges may thus be boundless/infinite.
 * <p>
 * Example: In a {@link RangeType#LOWER_BOUND_EQUAL} {@link RangeStructure} by calling
 * <code>put(10, value1)</code> and <code>put(25, value2)</code>, two ranges are defined: [10..24]
 * and [25..infinity]. When using {@link RangeType#UPPER_BOUND_EQUAL}, however, the same calls
 * define the ranges [-infinity..10] and [11..25].
 * 
 * @see RangeStructure
 */
public enum RangeType {

    /**
     * Indicates that the keys are meant to be the lower bound of a range (not included).
     * <p>
     * 
     * @deprecated Because using this type in a {@link RangeStructure} results in asymmetrical
     *             behavior of the {@link RangeStructure#put(Object, SearchStructure)} and
     *             {@link RangeStructure#get(Object)} methods. A value that was added by calling
     *             <code>put(10, value)</code> can not be retrieved by calling <code>get(10)</code>.
     */
    @Deprecated
    LOWER_BOUND {
        @Override
        public <K extends Comparable<? super K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> floorEntry = tree.floorEntry(key);
            if (floorEntry == null) {
                return null;
            }
            return getLowerValueIfNeccessary(tree, key, floorEntry);
        }
    },

    /**
     * Indicates that the keys are meant to be the lower bound of a range including the lower bound.
     */
    LOWER_BOUND_EQUAL {

        @Override
        public <K extends Comparable<? super K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> floorEntry = tree.floorEntry(key);
            return getValueOrNull(floorEntry);
        }
    },

    /**
     * Indicates that the keys are meant to be the upper bound of a range (not included).
     * <p>
     * 
     * @deprecated Because using this type in a {@link RangeStructure} results in asymmetrical
     *             behavior of the {@link RangeStructure#put(Object, SearchStructure)} and
     *             {@link RangeStructure#get(Object)} methods. A value that was added by calling
     *             <code>put(10, value)</code> can not be retrieved by calling <code>get(10)</code>.
     */
    @Deprecated
    UPPER_BOUND {

        @Override
        public <K extends Comparable<? super K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> ceilingEntry = tree.ceilingEntry(key);
            if (ceilingEntry == null) {
                return null;
            }
            return getHigherValueIfNeccessary(tree, key, ceilingEntry);
        }
    },

    /**
     * Indicates that the keys are meant to be the upper bound of a range including the upper bound.
     */
    UPPER_BOUND_EQUAL {
        @Override
        public <K extends Comparable<? super K>, V> V getValue(TreeMap<K, V> tree, K key) {
            Entry<K, V> ceilingEntry = tree.ceilingEntry(key);
            return getValueOrNull(ceilingEntry);
        }
    };

    /**
     * Retrieves the matching value from the given map using the given key. The strategy used to
     * retrieve the value depends on the type of key. It differs in how the bounds of ranges are
     * processed.
     * 
     * @return the matching value in the given {@link TreeMap} or <code>null</code> if no matching
     *         value could be found.
     */
    public abstract <K extends Comparable<? super K>, V> V getValue(TreeMap<K, V> tree, K key);

    private static <K extends Comparable<? super K>, V> V getLowerValueIfNeccessary(TreeMap<K, V> tree,
            K key,
            Entry<K, V> floorEntry) {
        if (floorEntry.getKey().compareTo(key) < 0) {
            return floorEntry.getValue();
        } else {
            Entry<K, V> lowerEntry = tree.lowerEntry(floorEntry.getKey());
            return getValueOrNull(lowerEntry);
        }
    }

    private static <K extends Comparable<? super K>, V> V getHigherValueIfNeccessary(TreeMap<K, V> tree,
            K key,
            Entry<K, V> ceilingEntry) {
        if (ceilingEntry.getKey().compareTo(key) > 0) {
            return ceilingEntry.getValue();
        } else {
            Entry<K, V> higherEntry = tree.higherEntry(ceilingEntry.getKey());
            return getValueOrNull(higherEntry);
        }
    }

    private static <K, V> V getValueOrNull(Entry<K, V> entry) {
        if (entry != null) {
            return entry.getValue();
        } else {
            return null;
        }
    }
}