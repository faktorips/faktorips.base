/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.util;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.values.Decimal;

/**
 * Class that is used to get a histogram of elements' values, i.e. an overview how often which value
 * occurs. The value of an element is determined using a {@link Function}. Equality of values can be
 * determined using an {@link Comparator}.
 * 
 * @param <V> the type of values over which this histogram is created
 * @param <E> the type of elements over whose values this histogram is created
 */
public class Histogram<V, E> {

    /** Scale to which decimal values in {@link #getRelativeDistribution()} are rounded. */
    public static final int SCALE = 2;

    /**
     * Map containing the mapping of a value to the elements containing that value. The amount of
     * elements is the distribution how often the value occurs.
     */
    private final TreeMultimap<V, E> valueToElements;

    /** Function to determine an element's value. */
    private final Function<E, V> elementToValueFunction;

    /** Total number of elements in this histogram. */
    private final int totalCount;

    private Comparator<? super V> valueComparator;

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the values' equals method.
     */
    @SafeVarargs
    public Histogram(Function<E, V> elementToValueFunction, E... elements) {
        this(elementToValueFunction, new EqualToComparator<V>(), Arrays.asList(elements));
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the values' equals method.
     */
    public Histogram(Function<E, V> elementToValueFunction, Collection<E> elements) {
        this(elementToValueFunction, new EqualToComparator<V>(), elements);
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the given comparator.
     */
    @SafeVarargs
    public Histogram(Function<E, V> elementToValueFunction, Comparator<? super V> valueComparator, E... elements) {
        this(elementToValueFunction, valueComparator, Arrays.asList(elements));
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the given comparator.
     */
    public Histogram(Function<E, V> elementToValueFunction, Comparator<? super V> valueComparator,
            Collection<E> elements) {
        super();
        this.valueComparator = valueComparator;
        this.totalCount = elements.size();
        this.elementToValueFunction = elementToValueFunction;
        this.valueToElements = TreeMultimap.create(valueComparator, new SameInstanceComparator<E>());
        initValueToElementsMap(elements);
    }

    /**
     * Returns the absolute distribution of values in this histogram as a sorted map. The keys in
     * the map are the values in this histogram, the associated value in the map is the total count
     * how often the value occurs. The map is sorted so that values occurring more often come first.
     */
    public SortedMap<V, Integer> getAbsoluteDistribution() {
        TreeMap<V, Integer> sortedDistribution = Maps
                .newTreeMap(new DistributionComparator<>(valueToElements, valueComparator));
        sortedDistribution.putAll(transformToOccurenceCountMap(valueToElements));
        return Collections.unmodifiableSortedMap(sortedDistribution);
    }

    /**
     * Returns the relative distribution of values in this histogram as a sorted map. The keys in
     * the map are the values in this histogram, the associated value in the map is a decimal n with
     * 0 &lt; n &le; 1 that indicates how often an value occurs relative to the other values in the
     * histogram. The decimal is rounded to a scale of {@link #SCALE}. The map is sorted so that
     * values occurring more often come first.
     */
    public SortedMap<V, Decimal> getRelativeDistribution() {
        SortedMap<V, Integer> absoluteDistribution = getAbsoluteDistribution();
        SortedMap<V, Decimal> relativeDistribution = transformToRelativeDistribution(absoluteDistribution);
        return Collections.unmodifiableSortedMap(relativeDistribution);
    }

    /**
     * Returns the distribution of values and elements with these values.
     * 
     * @return a multimap that contains all values in this histogram (the map's keys) and the
     *         elements with these values (the map's values)
     */
    public Multimap<V, E> getDistribution() {
        return Multimaps.unmodifiableMultimap(valueToElements);
    }

    /**
     * Gives the amount of elements in this histogram.
     */
    public int countElements() {
        return valueToElements.size();
    }

    /**
     * Returns the elements in this histogram that have the given value.
     * 
     * @param value a value
     * @return the elements in this histogram that have the given value. The set is empty if no
     *         elements in this histogram have the given value.
     */
    public Set<E> getElements(V value) {
        return Collections.unmodifiableSet(valueToElements.get(value));
    }

    /**
     * Returns {@code true} when this histogram contains no elements/values.
     * 
     * @return {@code true} when this histogram contains no elements/values.
     */
    public boolean isEmpty() {
        return valueToElements.isEmpty();
    }

    /**
     * Transforms the given map containing the absolute distribution of values to a new map
     * containing the relative distribution of values.
     */
    private SortedMap<V, Decimal> transformToRelativeDistribution(SortedMap<V, Integer> map) {
        return Maps.transformEntries(map,
                (value, elementCount) -> Decimal.valueOf(elementCount).divide(totalCount, SCALE, RoundingMode.HALF_UP));
    }

    /**
     * Transforms the given {@code Multimap} to a simple {@code Map}, the keys are the key from the
     * {@code Multimap}, the value is the number of values to that key in the {@code Multimap}.
     */
    private Map<V, Integer> transformToOccurenceCountMap(Multimap<V, E> map) {
        return Maps.transformEntries(map.asMap(), ($, elements) -> CollectionUtils.size(elements));
    }

    /**
     * Initializes the distribution map. Values in the map are the values of the elements in the
     * given list, keys in the map are the counts how often those values occur.
     * <p>
     * Note that by using a {@link TreeMap} with a comparator, we explicitly want to determine
     * equality of values using said comparator (instead of the values' equals methods). In other
     * words, values that are equal according to the comparator are counted together, no matter if
     * they would be equal according to their equal method or not.
     */
    private void initValueToElementsMap(Collection<E> elements) {
        for (E e : elements) {
            valueToElements.put(elementToValueFunction.apply(e), e);
        }
    }

    private static int compareAnyObjects(Object o1, Object o2) {
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            Comparable<?> c1 = (Comparable<?>)o1;
            Comparable<?> c2 = (Comparable<?>)o2;
            return ObjectUtils.compare(c1, c2);
        } else {
            int idCompare = Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
            if (idCompare == 0) {
                // Fallback for (hopefully extremely) rare cases where objects are not equal but
                // do have the same identity hash code. Return a value != 0 to prevent different
                // objects overwriting each other in the distribution map. TreeMap probably
                // won't find the entries, but hey, at least we tried...
                return 1;
            }
            return idCompare;
        }
    }

    /**
     * Returns the best value (regarding the relative distribution) whose occurrence is greater than
     * of equal to the given threshold.
     * 
     * @param threshold the relative occurrence at which a value is used.
     * @return the best value (regarding the relative distribution) whose occurrence is greater than
     *         or equal to the threshold. Never returns <code>null</code>. Returns a
     *         {@link BestValue} with {@link BestValue#isPresent()} <code>false</code> if there is
     *         no best value.
     */
    public BestValue<V> getBestValue(Decimal threshold) {
        SortedMap<V, Decimal> relativeDistribution = getRelativeDistribution();
        V candidateValue = relativeDistribution.firstKey();
        return getBestValue(threshold, relativeDistribution, candidateValue);
    }

    /**
     * Returns a {@link BestValue} for the given candidate value if its relative distribution in the
     * given map is above the given threshold. Returns {@code BestValue.missingValue()} if it is
     * not.
     * 
     * @param threshold the threshold to use
     * @param relativeDistribution the sorted map containing the relative distributions
     * @param candidateValue the candidate value from the relative distribution (i.e. a key from the
     *            given map)
     * @return the {@link BestValue} for the given candidate value if its relative distribution is
     *         above the given threshold or {@code BestValue.missingValue()} if it is not
     */
    protected BestValue<V> getBestValue(Decimal threshold,
            SortedMap<V, Decimal> relativeDistribution,
            V candidateValue) {
        Decimal relDist = getRelativeDistribution(relativeDistribution, candidateValue);
        if (relDist.greaterThanOrEqual(threshold)) {
            return new BestValue<>(candidateValue, relDist);
        } else {
            return BestValue.<V> missingValue();
        }
    }

    /**
     * We had several times the problem that a datatype comparator was not implemented correctly.
     * 
     * This method reports the problem and tries to give best information to fix the problem. To
     * have an instant workaround this method returns <code>0</code> to always ignore the
     * problematic property.
     */
    protected Decimal getRelativeDistribution(SortedMap<V, Decimal> relativeDistribution, V candidateValue) {
        Decimal relDist = relativeDistribution.get(candidateValue);
        if (relDist == null) {
            IpsLog.log(new IpsStatus(IStatus.ERROR, "There seems to be an error in a datatype comparator: " //$NON-NLS-1$
                    + valueToElements.values().iterator().next() + " Value: " + candidateValue)); //$NON-NLS-1$
            return Decimal.valueOf(0);
        }
        return relDist;
    }

    @SuppressWarnings("unchecked")
    public static <E, V> Histogram<E, V> emptyHistogram() {
        return new Histogram<>(null, (Collection<V>)Collections.emptyList());
    }

    /**
     * Comparator that compares values by their occurrence count in
     * {@link Histogram#valueToElements} so that a value e1 occurring more often than e2 is less
     * than e2 and thus is sorted before e2. In other words this comparator implements the inverse
     * natural order of the occurrence count of values.
     */
    private static class DistributionComparator<V> implements Comparator<V>, Serializable {

        private static final long serialVersionUID = 1L;

        private final TreeMultimap<V, ?> valueToElements;

        private Comparator<? super V> valueComparator;

        public DistributionComparator(TreeMultimap<V, ?> valueToElements, Comparator<? super V> valueComparator) {
            this.valueToElements = valueToElements;
            this.valueComparator = valueComparator;
        }

        @Override
        public int compare(V value1, V value2) {
            int occurences1 = valueToElements.get(value1).size();
            int occurences2 = valueToElements.get(value2).size();
            if (occurences1 == occurences2) {
                return valueComparator.compare(value1, value2);
            } else {
                // reverse natural order
                return Integer.compare(occurences2, occurences1);
            }
        }

    }

    /**
     * A {@link Comparator} that uses object identity to compare objects. This comparator returns
     * <ul>
     * <li>0 when tow objects are the same instance</li>
     * <li>1 or -1 by comparing the objects' {@link System#identityHashCode(Object)} if they are not
     * the same instance according to their equals method</li>
     * </ul>
     */
    private static class SameInstanceComparator<U> implements Comparator<U>, Serializable {

        private static final long serialVersionUID = -5480214299260180838L;

        @Override
        public int compare(U o1, U o2) {
            if (o1 == o2) {
                return 0;
            } else {
                return compareAnyObjects(o1, o2);
            }
        }
    }

    /**
     * A {@link Comparator} that uses the equals method to compare objects. This comparator returns
     * <ul>
     * <li>0 for objects that are equal according to their equals method</li>
     * <li>1 or -1 by comparing the objects' {@link System#identityHashCode(Object)} if they are not
     * equal according to their equals method</li>
     * </ul>
     */
    private static class EqualToComparator<U> implements Comparator<U>, Serializable {

        private static final long serialVersionUID = -5480214299260180838L;

        @Override
        public int compare(U o1, U o2) {
            if (Objects.equals(o1, o2)) {
                return 0;
            } else {
                return compareAnyObjects(o1, o2);
            }
        }
    }

    /**
     * BestValue represents the best value from a value distribution in a {@link Histogram}. It may
     * represent a missing value, {@link #isPresent()} returns <code>false</code> in that case. Its
     * value may be <code>null</code> (while {@link #isPresent()} returns <code>true</code>), thus
     * <code>null</code> is a valid value. This distinguishes BestValue from Optional.
     */
    public static class BestValue<V> {

        private final V value;
        private final Decimal relativeFrequency;
        private final boolean isPresent;

        public BestValue(V value, Decimal relativeFrequency) {
            this.value = value;
            this.relativeFrequency = relativeFrequency;
            isPresent = true;
        }

        public BestValue() {
            this.value = null;
            relativeFrequency = Decimal.NULL;
            isPresent = false;
        }

        public boolean isPresent() {
            return isPresent;
        }

        /**
         * @return the value or <code>null</code> if <code>null</code> is the best value found.
         */
        public V getValue() {
            return value;
        }

        public static <V> BestValue<V> missingValue() {
            return new BestValue<>();
        }

        /**
         * Return the relative frequency with which this value occurs within the {@link Histogram}.
         * For example, if 4 of 5 entries had this value, the relative frequency would be 0.8.
         */
        public Decimal getRelativeFrequency() {
            return relativeFrequency;
        }

    }

}
