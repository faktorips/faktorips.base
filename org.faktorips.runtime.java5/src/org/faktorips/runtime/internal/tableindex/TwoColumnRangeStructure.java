/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
public class TwoColumnRangeStructure<K extends Comparable<? super K>, V extends SearchStructure<R> & Mergeable<? super V>, R>
        extends AbstractMapStructure<TwoColumnRange<K>, V, R> {

    TwoColumnRangeStructure() {
        super(new TreeMap<TwoColumnRange<K>, V>());
    }

    /**
     * Creates an empty {@link TwoColumnRangeStructure}.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & Mergeable<? super V>, R> TwoColumnRangeStructure<K, V, R> create() {
        return new TwoColumnRangeStructure<K, V, R>();
    }

    /**
     * Creates a new {@link TwoColumnRangeStructure} and adds the given range-value pair.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & Mergeable<? super V>, R> TwoColumnRangeStructure<K, V, R> createWith(K lowerBound,
            K upperBound,
            V value) {
        TwoColumnRangeStructure<K, V, R> structure = new TwoColumnRangeStructure<K, V, R>();
        structure.put(lowerBound, upperBound, value);
        return structure;
    }

    /**
     * Defines a range that maps to the given value. The keys define the upper and lower bound of
     * the range. The lower and upper bounds are inclusive.
     * 
     * @param lower the lower bound of the range (included)
     * @param upper the upper bound of the range (included)
     * @param value the nested {@link SearchStructure} to be added
     */
    public void put(K lower, K upper, V value) {
        put(lower, upper, true, true, value);
    }

    /**
     * Defines a range that maps to the given value. The keys define the upper and lower bound of
     * the range.
     * <p>
     * If the lower bound is greater than upper bound, the lower and upper bound are automatically
     * switched (including lower- and upper-inclusive parameter).
     * 
     * @param lower the lower bound of the range (included)
     * @param upper the upper bound of the range (included)
     * @param lowerInclusive <code>true</code> if the lower bound should be inclusive,
     *            <code>false</code> if it should be exclusive
     * @param upperInclusive <code>true</code> if the upper bound should be inclusive,
     *            <code>false</code> if it should be exclusive
     * @param value the nested {@link SearchStructure} to be added
     */
    public void put(K lower, K upper, boolean lowerInclusive, boolean upperInclusive, V value) {
        TwoColumnRange<K> key = new TwoColumnRange<K>(lower, upper, lowerInclusive, upperInclusive);
        new OverlappingRangePutter<K, V>(getMap()).put(key, value);
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
     * to the fact that {@link TwoColumnRange}'s equals()/hashCode() considers only the lowerBound.
     * Thus it will only find a value requesting the same lower bound (when calling the
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
        return floorEntry != null && twoColumnKey.compareToUpperBound(floorEntry.getKey()) <= 0;
    }

    public Mergeable<AbstractMapStructure<TwoColumnRange<K>, V, R>> copy() {
        TwoColumnRangeStructure<K, V, R> twoColumnRangeStructure = new TwoColumnRangeStructure<K, V, R>();
        return copyOriginalMap(twoColumnRangeStructure);
    }

    private static class OverlappingRangePutter<K extends Comparable<? super K>, V extends Mergeable<? super V>> {

        private TreeMap<TwoColumnRange<K>, V> treeMap;

        private KeyValuePair<K, V> overlappedEntry;

        private KeyValuePair<K, V> lo;

        private KeyValuePair<K, V> mid;

        private KeyValuePair<K, V> hi;

        private KeyValuePair<K, V> newEntry;

        public OverlappingRangePutter(TreeMap<TwoColumnRange<K>, V> treeMap) {
            this.treeMap = treeMap;
        }

        public void put(TwoColumnRange<K> key, V value) {
            putRespectingOverlapping(new KeyValuePair<K, V>(key, value));
        }

        private void putRespectingOverlapping(KeyValuePair<K, V> entry) {
            this.newEntry = entry;
            if (findOverlapping()) {
                putOverlappingEntry();
            } else {
                putNonOverlappingEntry(entry);
            }
        }

        private void putOverlappingEntry() {
            createLoMidHi();
            removeOverlappedEntry();
            if (lo != null) {
                putNonOverlappingEntry(lo);
            }
            putNonOverlappingEntry(mid);
            if (hi != null) {
                putRespectingOverlapping(hi);
            }
        }

        private void createLoMidHi() {
            int compareLowerBound = newEntry.key.compareTo(overlappedEntry.key);
            int compareUpperBound = newEntry.key.compareToUpperBound(overlappedEntry.key);
            if (compareLowerBound < 0) {
                // e.g. newEntry = [1..?] and overlappedEntry = [4..?]
                createLoMidHi(compareUpperBound, newEntry, overlappedEntry);
            } else {
                // e.g. newEntry = [4..?] and overlappedEntry = [1..?]
                createLoMidHi(-compareUpperBound, overlappedEntry, newEntry);
            }
            if (compareLowerBound == 0) {
                // the created range is invalid, e.g. [1 - 1[
                lo = null;
            }
        }

        private void createLoMidHi(int compareUpperBound, KeyValuePair<K, V> lowerEntry, KeyValuePair<K, V> higherEntry) {
            createLo(lowerEntry, higherEntry);
            if (compareUpperBound < 0) {
                // e.g. lowerEntry = [1..6] and upperEntry = [4..10]
                createMid(lowerEntry, higherEntry);
                createHi(lowerEntry, higherEntry);
            } else {
                // e.g. lowerEntry = [1..10] and upperEntry = [4..6]
                mergedMid(higherEntry, lowerEntry.value);
                if (compareUpperBound == 0) {
                    hi = null;
                } else {
                    createHi(higherEntry, lowerEntry);
                }
            }
        }

        private void createLo(KeyValuePair<K, V> lowerPair, KeyValuePair<K, V> upperPair) {
            TwoColumnRange<K> keyBelow = new TwoColumnRange<K>(lowerPair.key.getLowerBound(),
                    upperPair.key.getLowerBound(), lowerPair.key.isLowerInclusive(), !upperPair.key.isLowerInclusive());
            lo = new KeyValuePair<K, V>(keyBelow, lowerPair.value);
        }

        private void createMid(KeyValuePair<K, V> loPair, KeyValuePair<K, V> upPair) {
            TwoColumnRange<K> keyBelow = new TwoColumnRange<K>(upPair.key.getLowerBound(), loPair.key.getUpperBound(),
                    upPair.key.isLowerInclusive(), loPair.key.isUpperInclusive());
            V mergedValue = (V)upPair.value.copy();
            mergedValue.merge(loPair.value);
            mid = new KeyValuePair<K, V>(keyBelow, mergedValue);
        }

        private void mergedMid(KeyValuePair<K, V> keyValuePair, V newValue) {
            V mergedValue = (V)keyValuePair.value.copy();
            mergedValue.merge(newValue);
            mid = new KeyValuePair<K, V>(keyValuePair.key, mergedValue);
        }

        private void createHi(KeyValuePair<K, V> lowerPair, KeyValuePair<K, V> upperPair) {
            TwoColumnRange<K> keyAbove = new TwoColumnRange<K>(lowerPair.key.getUpperBound(),
                    upperPair.key.getUpperBound(), !lowerPair.key.isUpperInclusive(), upperPair.key.isUpperInclusive());
            hi = new KeyValuePair<K, V>(keyAbove, upperPair.value);
        }

        private void removeOverlappedEntry() {
            treeMap.remove(overlappedEntry.key);
        }

        private void putNonOverlappingEntry(KeyValuePair<K, V> pair) {
            treeMap.put(pair.key, pair.value);
        }

        private boolean findOverlapping() {
            Entry<TwoColumnRange<K>, V> floorEntry = treeMap.floorEntry(newEntry.key);
            if (floorEntry != null && floorEntry.getKey().isOverlapping(newEntry.key)) {
                overlappedEntry = new KeyValuePair<K, V>(floorEntry.getKey(), floorEntry.getValue());
                return true;
            } else {
                Entry<TwoColumnRange<K>, V> ceilingEntry = treeMap.ceilingEntry(newEntry.key);
                if (ceilingEntry != null && newEntry.key.isOverlapping(ceilingEntry.getKey())) {
                    overlappedEntry = new KeyValuePair<K, V>(ceilingEntry.getKey(), ceilingEntry.getValue());
                    return true;
                } else {
                    return false;
                }
            }
        }

        private static class KeyValuePair<K extends Comparable<? super K>, V extends Mergeable<? super V>> {

            private final TwoColumnRange<K> key;

            private final V value;

            public KeyValuePair(TwoColumnRange<K> key, V value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String toString() {
                return "KeyValuePair [key=" + key + ", value=" + value + "]";
            }

        }
    }

}