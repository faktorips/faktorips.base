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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.faktorips.values.ObjectUtil;

/**
 * A {@link SearchStructure} that maps ranges (keys) to nested {@link SearchStructure
 * SearchStructures} i.e. sets of values. Each {@link TwoColumnRange range} in a
 * {@link TwoColumnRangeStructure} is defined by lower and upper bound. Thus there might be "gaps"
 * in between ranges, in contrast to {@link RangeStructure RangeStructures}. The
 * {@link TwoColumnRangeStructure} also supports overlapping ranges, as well as "infinite" ranges.
 * <p>
 * Ranges are set up by putting one or more key-key-value tuples into this structure. The keys are
 * of a comparable data type (of course, as they define a range). The value is a nested
 * {@link SearchStructure}. The first key defines the lower bound, the second the upper bound of the
 * range. The value can be retrieved by calling {@link #get(Object)} with a key inside the defined
 * range.
 * <p>
 * Example: In a {@link TwoColumnRangeStructure} by calling <code>put(10, 24, "AAA")</code> and
 * <code>put(25, 50, "BBB")</code>, two ranges are defined. Calls to {@link #get(Object)} with the:
 * <ul>
 * <li>keys 10, 24 and all in between will yield {"AAA"} as a result</li>
 * <li>keys 25, 50 and all in between will yield {"BBB"} respectively</li>
 * <li>keys outside these ranges, e.g. 0 or 100 will return an {@link EmptySearchStructure}.</li>
 * </ul>
 * <p>
 * Overlapping ranges: In a {@link TwoColumnRangeStructure} by calling
 * <code>put(0, 100, "AAA")</code> and <code>put(50, 200, "BBB")</code>, two ranges are defined.
 * Calls to {@link #get(Object)} with keys:
 * <ul>
 * <li>keys 0, 49 and all in between will yield {"AAA"} as a result</li>
 * <li>keys 101, 200 and all in between will yield {"BBB"} respectively</li>
 * <li>keys in the overlapping, 50, 100 and all in between, will yield the set union {"AAA", "BBB"}
 * as a result</li>
 * <li>keys outside these ranges, e.g. -100 or 500 will return an {@link EmptySearchStructure}.</li>
 * </ul>
 * <p>
 * Calling <code>put(null, 100, "AAA")</code> defines a range from negative infinity to 100, that
 * maps to {"AAA"}. This range includes all values less than and equal to 100. Calling
 * <code>put(0, null, "BBB")</code> in turn defines a range from 0 to positive infinity. This range
 * includes all values greater than and equal to 0.
 */
public class TwoColumnRangeStructure<K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R>
        extends AbstractMapStructure<TwoColumnRange<K>, V, R> implements
        MergeAndCopyStructure<TwoColumnRangeStructure<K, V, R>> {

    TwoColumnRangeStructure() {
        super(new TreeMap<>());
    }

    /**
     * Creates an empty {@link TwoColumnRangeStructure}.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> TwoColumnRangeStructure<K, V, R> create() {
        return new TwoColumnRangeStructure<>();
    }

    /**
     * Creates a new {@link TwoColumnRangeStructure} and adds the given range-value pair.
     */
    public static <K extends Comparable<? super K>, V extends SearchStructure<R> & MergeAndCopyStructure<V>, R> TwoColumnRangeStructure<K, V, R> createWith(
            K lowerBound,
            K upperBound,
            V value) {
        TwoColumnRangeStructure<K, V, R> structure = new TwoColumnRangeStructure<>();
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
        TwoColumnRange<K> key = new TwoColumnRange<>(lower, upper, lowerInclusive, upperInclusive);
        put(key, value);
    }

    @Override
    public void put(TwoColumnRange<K> key, V value) {
        new OverlappingRangePutter<>(getMap()).put(key, value);
    }

    @Override
    protected TreeMap<TwoColumnRange<K>, V> getMap() {
        return (TreeMap<TwoColumnRange<K>, V>)super.getMap();
    }

    @Override
    public SearchStructure<R> get(Object key) {
        if (ObjectUtil.isNull(key)) {
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
        return new TwoColumnRange<>(kKey, kKey);
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

    @Override
    public void merge(TwoColumnRangeStructure<K, V, R> map) {
        super.merge(map);
    }

    @Override
    public TwoColumnRangeStructure<K, V, R> copy() {
        return fillCopy(new TwoColumnRangeStructure<>());
    }

    /**
     * Helper class managing overlapping ranges when putting into a {@link TwoColumnRangeStructure}.
     * When an overlapping occurs this
     * {@link org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure.OverlappingRangePutter}
     * splits the overlapping ranges into multiple non-overlapping ranges.
     * <p>
     * Example: A range structure contains several ranges (for example rangeA), and rangeB is added.
     * The algorithm executes the following steps:
     * <ul>
     * <li>search the map for an range-entry less than rangeB (compared by lower bound). Check
     * whether the found range (rangeA) overlaps with rangeB.
     * <ul>
     * <li>If they overlap, split up the ranges (see below)</li>
     * <li>else search an entry greater than rangeB (compared by lower bound). Check whether the
     * found range (rangeA) overlaps with rangeB.
     * <ul>
     * <li>If they overlap, split up the ranges (see below).</li>
     * <li>If the ranges do not overlap, simply add rangeB to the structure. There is no overlapping
     * with other ranges.</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * <li>Splitting up ranges: The existing range rangeA has proven to overlap with the new rangeB.
     * Split up the two overlapping into (up to) three non-overlapping ranges in the following
     * steps:
     * 
     * <ul>
     * <li>Remove rangeA (the existing range) from the structure.</li>
     * <li>Do not add rangeB (new range) to the structure.</li>
     * <li>Instead create (up to) three new ranges:</li>
     * <ul>
     * <li>Lower range. Maps to the value of either rangeA or rangeB. This range can be safely added
     * to the structure as there can be no further overlappings. If rangeA's and rangeB's lower
     * bounds match exactly, this range may not be required and is thusly not created.</li>
     * <li>Middle range, the actual overlapping. Create a new search structure, that contains a set
     * union of the values of both rangeA and rangeB. This range can be safely added to the
     * structure as there can be no further overlappings (all previous overlappings were removed in
     * a previous put()-call).</li>
     * <li>Upper range. Maps to the value of either rangeB or rangeA. This range might overlap with
     * other ranges in the rangeStructure. Recursively call the
     * {@link #putRespectingOverlapping(RangeEntry)} to let this putter handle further overlappings.
     * If rangeA's and rangeB's upper bounds match exactly, this range may not be required and is
     * thusly not created.</li>
     * </ul>
     * </ul>
     * </li>
     * </ul>
     * Example:
     * <ul>
     * <li>RangeA: [0..20] -> {"A", "B"}. Read: the values 0 to 20, and all in between map to a set
     * containing the strings "A" and "B".</li>
     * <li>RangeB: [10..50] -> {"X", "Y"}. Read: the values 10 to 50, and all in between map to a
     * set containing the strings "X" and "Y".</li>
     * </ul>
     * Assuming rangeA exists in a {@link TwoColumnRangeStructure} and rangeB is added by calling
     * {@link #put(TwoColumnRange, MergeAndCopyStructure)}:
     * <ul>
     * <li>search for a range lower than [10..50]. This finds rangeA, as the lower bound (0) is less
     * than rangeB's lower bound (10).</li>
     * <ul>
     * <li>the ranges overlap, because rangeA's upper bound (20) is greater than rangeB's lower
     * bound (10).</li>
     * </ul>
     * 
     * <li>Splitting up ranges:</li>
     * <ul>
     * <li>Remove rangeA (the existing range) from the structure.</li>
     * <li>Do not add rangeB (new range) to the structure.</li>
     * <li>Instead create three new ranges:</li>
     * <ul>
     * <li>Lower range: [0..10[ -> {"A", "B"}. This range does not include 10 and maps to a set
     * containing the strings "A" and "B". It can be safely added to the structure as there can be
     * no further overlappings.</li>
     * <li>Middle range, the actual overlapping. Create a new search structure, that contains a set
     * union of the values of both rangeA and rangeB: [10..20] -> {"A", "B", "X", "Y"}. This range
     * can be safely added to the structure as there can be no further overlappings (all previous
     * overlappings were removed in a previous put()-call).</li>
     * <li>Upper range. ]20..50] -> {"X", "Y"}. This range does not include 20 and maps to a set
     * containing the strings "X" and "Y". However, it might overlap with other ranges in the
     * rangeStructure. Recursively call the {@link #putRespectingOverlapping(RangeEntry)} to let
     * this putter handle further overlappings.</li>
     * </ul>
     * </ul>
     * </li>
     * </ul>
     * See also FIPS-2451.
     */
    private static class OverlappingRangePutter<K extends Comparable<? super K>, V extends MergeAndCopyStructure<V>> {

        private TreeMap<TwoColumnRange<K>, V> treeMap;

        private RangeEntry<K, V> overlappedEntry;
        private RangeEntry<K, V> newEntry;
        private RangeEntry<K, V> lo;
        private RangeEntry<K, V> mid;
        private RangeEntry<K, V> hi;

        public OverlappingRangePutter(TreeMap<TwoColumnRange<K>, V> treeMap) {
            this.treeMap = treeMap;
        }

        /**
         * Adds the specified range to the {@link TwoColumnRangeStructure} and takes care of
         * overlapping ranges by splitting them up into multiple non-overlapping, smaller ranges.
         * 
         * @param key the range to be added
         * @param value the value the added range maps to
         */
        public void put(TwoColumnRange<K> key, V value) {
            putRespectingOverlapping(new RangeEntry<>(key, value));
        }

        /**
         * Adds a
         * {@link org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure.OverlappingRangePutter.RangeEntry}
         * to this {@link TwoColumnRangeStructure} and takes care of overlapping ranges by splitting
         * them up into multiple non-overlapping, smaller ranges.
         * 
         * @param entry the
         *            {@link org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure.OverlappingRangePutter.RangeEntry}
         *            to be added.
         */
        private void putRespectingOverlapping(RangeEntry<K, V> entry) {
            this.newEntry = entry;
            if (findOverlappedRange()) {
                splitUpOverlappingRanges();
            } else {
                putNonOverlappingRange(entry);
            }
        }

        /**
         * Splits up the overlapped and the new range into three non-overlapping ranges. If however,
         * both ranges' bounds match exactly, the lower and/or higher range might not be required
         * and thusly not created.
         */
        private void splitUpOverlappingRanges() {
            createLoMidHiRanges();
            removeOverlappedRange();
            if (lo != null) {
                putNonOverlappingRange(lo);
            }
            putNonOverlappingRange(mid);
            if (hi != null) {
                putRespectingOverlapping(hi);
            }
        }

        /**
         * Create three ranges from the overlapped and the new range.
         */
        private void createLoMidHiRanges() {
            int compareLowerBound = newEntry.key.compareTo(overlappedEntry.key);
            int compareUpperBound = newEntry.key.compareToUpperBound(overlappedEntry.key);
            if (compareLowerBound < 0) {
                // e.g. newEntry = [1..?] and overlappedEntry = [4..?]
                createLoMidHiRanges(compareUpperBound, newEntry, overlappedEntry);
            } else {
                // e.g. newEntry = [4..?] and overlappedEntry = [1..?]
                createLoMidHiRanges(-compareUpperBound, overlappedEntry, newEntry);
            }
            if (compareLowerBound == 0) {
                // the created range is invalid, e.g. [1 - 1[
                lo = null;
            }
        }

        private void createLoMidHiRanges(int compareUpperBound,
                RangeEntry<K, V> lowerEntry,
                RangeEntry<K, V> higherEntry) {
            createLower(lowerEntry, higherEntry);
            if (compareUpperBound < 0) {
                // e.g. lowerEntry = [1..6] and upperEntry = [4..10]
                createMiddle(lowerEntry, higherEntry);
                createHigher(lowerEntry, higherEntry);
            } else {
                // e.g. lowerEntry = [1..10] and upperEntry = [4..6]
                mergedMiddle(higherEntry, lowerEntry.value);
                if (compareUpperBound == 0) {
                    hi = null;
                } else {
                    createHigher(higherEntry, lowerEntry);
                }
            }
        }

        private void createLower(RangeEntry<K, V> lowerEntry, RangeEntry<K, V> upperEntry) {
            TwoColumnRange<K> keyBelow = new TwoColumnRange<>(lowerEntry.key.getLowerBound(),
                    upperEntry.key.getLowerBound(), lowerEntry.key.isLowerInclusive(),
                    !upperEntry.key.isLowerInclusive());
            lo = new RangeEntry<>(keyBelow, lowerEntry.value);
        }

        private void createMiddle(RangeEntry<K, V> lowerEntry, RangeEntry<K, V> upperEntry) {
            TwoColumnRange<K> keyBelow = new TwoColumnRange<>(upperEntry.key.getLowerBound(),
                    lowerEntry.key.getUpperBound(), upperEntry.key.isLowerInclusive(),
                    lowerEntry.key.isUpperInclusive());
            V mergedValue = upperEntry.value.copy();
            mergedValue.merge(lowerEntry.value);
            mid = new RangeEntry<>(keyBelow, mergedValue);
        }

        /**
         * Special case: instead of creating a new range, the existing range can be reused by
         * merging the overlapped range's values into it. This is the case if the overlapped range
         * is a strict subset of the added range, or vice versa.
         */
        private void mergedMiddle(RangeEntry<K, V> rangeEntry, V newValue) {
            V mergedValue = rangeEntry.value.copy();
            mergedValue.merge(newValue);
            mid = new RangeEntry<>(rangeEntry.key, mergedValue);
        }

        private void createHigher(RangeEntry<K, V> lowerEntry, RangeEntry<K, V> upperEntry) {
            TwoColumnRange<K> keyAbove = new TwoColumnRange<>(lowerEntry.key.getUpperBound(),
                    upperEntry.key.getUpperBound(), !lowerEntry.key.isUpperInclusive(),
                    upperEntry.key.isUpperInclusive());
            hi = new RangeEntry<>(keyAbove, upperEntry.value);
        }

        private void removeOverlappedRange() {
            treeMap.remove(overlappedEntry.key);
        }

        private void putNonOverlappingRange(RangeEntry<K, V> pair) {
            treeMap.put(pair.key, pair.value);
        }

        /**
         * Searches for an overlapped range in the {@link TwoColumnRangeStructure} by searching for
         * the range less than and the range greater than if necessary.
         * 
         * @return if an overlapped range was found
         */
        private boolean findOverlappedRange() {
            Entry<TwoColumnRange<K>, V> floorEntry = treeMap.floorEntry(newEntry.key);
            if (floorEntry != null && floorEntry.getKey().isOverlapping(newEntry.key)) {
                overlappedEntry = new RangeEntry<>(floorEntry.getKey(), floorEntry.getValue());
                return true;
            } else {
                Entry<TwoColumnRange<K>, V> ceilingEntry = treeMap.ceilingEntry(newEntry.key);
                if (ceilingEntry != null && newEntry.key.isOverlapping(ceilingEntry.getKey())) {
                    overlappedEntry = new RangeEntry<>(ceilingEntry.getKey(), ceilingEntry.getValue());
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * Entry class holding a range and the value it maps to in a single object.
         * 
         */
        private static class RangeEntry<K extends Comparable<? super K>, V extends MergeAndCopyStructure<V>> {
            private final TwoColumnRange<K> key;

            private final V value;

            public RangeEntry(TwoColumnRange<K> key, V value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String toString() {
                return getClass().getSimpleName() + " [key=" + key + ", value=" + value + "]";
            }
        }
    }

}
