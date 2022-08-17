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

import java.util.TreeMap;

/**
 * A {@link SearchStructure} that maps ranges to nested {@link SearchStructure SearchStructures}. A
 * {@link RangeStructure} is configured by a {@link RangeType} to define how the bounds of the
 * contained ranges should be handled.
 * <p>
 * Ranges are set up by putting one or more key-value pairs into this structure. The key is of a
 * comparable data type (of course, as it defines a range). The value is a nested
 * {@link SearchStructure}. The given key defines one of the bounds of a range, which one depends on
 * the {@link RangeType}.
 * <p>
 * Example: In a {@link RangeType#LOWER_BOUND_EQUAL} {@link RangeStructure} (keys of type
 * {@link Integer}) by calling <code>put(10, value1)</code> and <code>put(25, value2)</code>, two
 * ranges are defined: [10..24] and [25..infinity]. Calls to {@link #get(Object)} with the keys 10,
 * 24 and all in between will yield value1 as a result. Calls to {@link #get(Object)} with the keys
 * 25 and higher will yield value2 respectively. The keys 9 and lower, however, will return an
 * {@link EmptySearchStructure}.
 * 
 * @see RangeType
 */
public class RangeStructure<K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R>
        extends AbstractMapStructure<K, V, R> implements MergeAndCopyStructure<RangeStructure<K, V, R>> {

    private final RangeType rangeType;

    /**
     * @param rangeType defines how the bounds of ranges should be handled. Must not be
     *            <code>null</code>.
     * @throws NullPointerException if the {@link RangeType} is <code>null</code>
     */
    RangeStructure(RangeType rangeType) {
        super(new TreeMap<K, V>());
        if (rangeType == null) {
            throw new NullPointerException("RangeType must not be null");
        }
        this.rangeType = rangeType;
    }

    /**
     * Creates an empty {@link RangeStructure}.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> RangeStructure<K, V, R> create(
            RangeType keyType) {
        return new RangeStructure<>(keyType);
    }

    /**
     * Creates a new {@link RangeStructure} and adds the given key-value pair.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> RangeStructure<K, V, R> createWith(
            RangeType keyType,
            K key,
            V value) {
        RangeStructure<K, V, R> structure = new RangeStructure<>(keyType);
        structure.put(key, value);
        return structure;
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (key == null) {
            return emptyResult();
        } else {
            @SuppressWarnings("unchecked")
            K kKey = (K)key;
            V result = rangeType.getValue(getMap(), kKey);
            return getValidResult(result);
        }
    }

    @Override
    protected TreeMap<K, V> getMap() {
        return (TreeMap<K, V>)super.getMap();
    }

    @Override
    public void merge(RangeStructure<K, V, R> map) {
        super.merge(map);
    }

    @Override
    public RangeStructure<K, V, R> copy() {
        return fillCopy(new RangeStructure<K, V, R>(rangeType));
    }

}
