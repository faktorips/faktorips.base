/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A {@link SearchStructure} that maps ranges to nested {@link SearchStructure SearchStructures}. A
 * {@link TwoColumnRangeStructure} allows for defining ranges by lower and upper bound. Because of
 * this there might be "gaps" in between ranges, however ranges cannot overlap. There are no
 * boundless/infinite ranges (in contrast to a {@link RangeStructure}).
 * <p>
 * Ranges are setup by putting one or more key-key-value tuples into this structure. The keys are of
 * a comparable data type (of course, as they define a range). The value is a nested
 * {@link SearchStructure}. The first key defines the lower bound, the second the upper bound of the
 * range (which includes the bounds). The value can be retrieved by calling {@link #get(Object)}
 * with a key inside the defined range.
 * <p>
 * Example: In a {@link TwoColumnRangeStructure} by calling <code>put(10, 24, value1)</code> and
 * <code>put(25, 50, value2)</code>, two ranges are defined. Calls to {@link #get(Object)} with the
 * keys 10, 24 and all in between will yield value1 as a result. Calls to {@link #get(Object)} with
 * the keys 25, 50 and all in between will yield value2 respectively. Keys outside these ranges,
 * e.g. 0 or 100 will return an {@link EmptySearchStructure}.
 */
public class TwoColumnRangeStructure<K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R>
        extends AbstractMapStructure<TwoColumnRange<K>, V, R> {

    TwoColumnRangeStructure() {
        super(new TreeMap<TwoColumnRange<K>, V>());
    }

    /**
     * Creates an empty {@link TwoColumnRangeStructure}.
     */
    public static <K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R> TwoColumnRangeStructure<K, V, R> create() {
        return new TwoColumnRangeStructure<K, V, R>();
    }

    /**
     * Creates a new {@link TwoColumnRangeStructure} and adds the given range-value pair.
     */
    public static <K extends Comparable<K>, V extends SearchStructure<R> & Mergeable<? super V>, R> TwoColumnRangeStructure<K, V, R> createWith(K lowerBound,
            K upperBound,
            V value) {
        TwoColumnRangeStructure<K, V, R> structure = new TwoColumnRangeStructure<K, V, R>();
        structure.put(lowerBound, upperBound, value);
        return structure;
    }

    /**
     * Defines a range that maps to the given value. The keys define the upper and lower bound of
     * the range.
     * 
     * @param lower the lower bound of the range (included)
     * @param upper the upper bound of the range (included)
     * @param value the nested {@link SearchStructure} to be added
     */
    public void put(K lower, K upper, V value) {
        super.put(new TwoColumnRange<K>(lower, upper), value);
    }

    @Override
    protected TreeMap<TwoColumnRange<K>, V> getMap() {
        return (TreeMap<TwoColumnRange<K>, V>)super.getMap();
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (key == null) {
            return emptyResult();
        } else {
            TwoColumnRange<K> twoColumnKey = createTwoColumnKey(key);
            V result = getMatchingValue(twoColumnKey);
            return getValidResult(result);
        }
    }

    private TwoColumnRange<K> createTwoColumnKey(Object key) {
        @SuppressWarnings("unchecked")
        K kKey = (K)key;
        TwoColumnRange<K> twoColumnKey = new TwoColumnRange<K>(kKey, kKey);
        return twoColumnKey;
    }

    /**
     * Returns the value mapped by the given TwoColumnRange/range or <code>null</code> if no
     * matching value can be found.
     * <p>
     * A simple get on the tree map might not yield the correct result (if any at all). This is due
     * to the fact that {@link TwoColumnRange}'s hashCode() considers only the lowerBound. Thus you
     * will only find a value requesting the same lower bound (when calling the
     * {@link Map#get(Object)} method directly). Mostly though, this is not the case.
     * {@link #get(Object)} is called for values <em>within</em> such a range.
     * <p>
     * This method uses the {@link TreeMap#floorEntry(Object)} method to retrieve the entry holding
     * the value for the next lower key. For example: the map contains key (10-20); Asking for
     * (15-15) will find the entry for (10-20) as it is the one with the next lower bound. Asking
     * for (15-25) will yield <code>null</code> however as the requested range does not fully
     * overlap with the range defined by the {@link TreeMap}.
     */
    private V getMatchingValue(TwoColumnRange<K> twoColumnKey) {
        Entry<TwoColumnRange<K>, V> floorEntry = getMap().floorEntry(twoColumnKey);
        if (isMatchingEntry(twoColumnKey, floorEntry)) {
            return floorEntry.getValue();
        } else {
            return null;
        }
    }

    private boolean isMatchingEntry(TwoColumnRange<K> twoColumnKey, Entry<TwoColumnRange<K>, V> floorEntry) {
        return floorEntry != null && twoColumnKey.isLowerOrEqualUpperBound(floorEntry.getKey());
    }

}