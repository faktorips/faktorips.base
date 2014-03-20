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

import java.io.Serializable;
import java.util.TreeMap;

/**
 * An immutable data object defining a range between two columns. It is used by the
 * {@link TwoColumnRangeStructure} as key object for the internal map.
 * 
 * @see TwoColumnRangeStructure
 */
class TwoColumnRange<K extends Comparable<? super K>> implements Comparable<TwoColumnRange<K>>, Serializable {

    private static final long serialVersionUID = 42L;

    private final Bound<K> lowerBound;

    private final Bound<K> upperBound;

    /**
     * @param lowerBound The lowerBound of this TwoColumnRange.
     * @param upperBound The upperBound of this TwoColumnRange.
     */
    TwoColumnRange(K lowerBound, K upperBound) {
        this(lowerBound, upperBound, true, true);
    }

    /**
     * @param lowerBound The lowerBound of this TwoColumnRange.
     * @param upperBound The upperBound of this TwoColumnRange.
     */
    TwoColumnRange(K lowerBound, K upperBound, boolean lowerInclusive, boolean upperInclusive) {
        if (lowerBound == null) {
            this.lowerBound = Bound.negativeInfinity();
        } else {
            this.lowerBound = new Bound<K>(lowerBound, IntervalDirection.getLowerBoundDirection(lowerInclusive));
        }
        if (upperBound == null) {
            this.upperBound = Bound.positiveInfinity();
        } else {
            this.upperBound = new Bound<K>(upperBound, IntervalDirection.getUpperBoundDirection(upperInclusive));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Comparing two {@link TwoColumnRange} always means to only compare the lower bound. The upper
     * bound needs to be compared manually by calling {@link #isLowerInclusive()} or
     * {@link #compareToUpperBound(TwoColumnRange)}. This is useful because when storing the
     * {@link TwoColumnRange} in {@link TreeMap} we need to have a distinct order which is only
     * achieved by comparing one bound.
     * <p>
     * This method is called by {@link TwoColumnRangeStructure#get(Object)} and hence needs to be
     * performance optimized!
     */
    public int compareTo(TwoColumnRange<K> other) {
        return lowerBound.compareTo(other.lowerBound);
    }

    /**
     * Compares this {@link TwoColumnRange} to the other {@link TwoColumnRange} according to their
     * upper bounds.
     * <p>
     * Returns a negative number if the upper bound of this range is below the upper bound of the
     * specified other range, returns positive number if the upper bound of this range is above the
     * other range. If both upper bounds are equal the result depends on the upper bound inclusive
     * flags:
     * <ul>
     * <li>both inclusive or both exclusive: 0</li>
     * <li>this upper bound exclusive: -1</li>
     * <li>this upper bound inclusive: 1</li>
     * </ul>
     * <p>
     * In {@link #compareTo(TwoColumnRange)} and {@link #equals(Object)} only the lower bound is
     * checked. Using this method we could check the upper bound is also matching.
     * <p>
     * This method is called by {@link TwoColumnRangeStructure#get(Object)} and hence needs to be
     * performance optimized! Thats why we do not simply use {@link #isOverlapping(TwoColumnRange)}
     * 
     * @param otherKey The other key that's upper bound is compared to this one
     * @return <code>true</code> if this upper bound is below other's upper bound
     * 
     */
    public int compareToUpperBound(TwoColumnRange<K> otherKey) {
        return upperBound.compareTo(otherKey.upperBound);
    }

    /**
     * Checks whether this range overlaps with the other range.
     * <p>
     * For example:
     * <ul>
     * <li>
     * [0..10] and [5..20] overlap</li>
     * <li>
     * [3..5] and [5..8] overlap</li>
     * <li>
     * [3..5[ and [5..8] do not overlap, however, as [3..5[ does not include 5.</li>
     * </ul>
     * 
     * @param otherRange The other range that is tested to overlaps this range
     * @return <code>true</code> if the ranges overlap, <code>false</code> if they are disjoint
     */
    public boolean isOverlapping(TwoColumnRange<K> otherRange) {
        return lowerBound.compareTo(otherRange.upperBound) < 0 && upperBound.compareTo(otherRange.lowerBound) > 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Two {@link TwoColumnRange ranges} are always equal when their lower bounds are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TwoColumnRange<?> other = (TwoColumnRange<?>)obj;
        return lowerBound.equals(other.lowerBound);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lowerBound.hashCode();
        return result;
    }

    /**
     * @return Returns the lowerBound.
     */
    public K getLowerBound() {
        return lowerBound.boundaryValue;
    }

    /**
     * Returns whether the lower bound is inclusive or not.
     * 
     * @return <code>true</code> if the lower bound is inclusive otherwise <code>false</code>
     */
    public boolean isLowerInclusive() {
        return lowerBound.direction.isLowerInclulsive();
    }

    /**
     * @return Returns the upperBound.
     */
    public K getUpperBound() {
        return upperBound.boundaryValue;
    }

    /**
     * Returns whether the upper bound is inclusive or not.
     * 
     * @return <code>true</code> if the upper bound is inclusive otherwise <code>false</code>
     */
    public boolean isUpperInclusive() {
        return upperBound.direction.isUpperInclulsive();
    }

    @Override
    public String toString() {
        return "TwoColumnRange " + lowerBound.direction + lowerBound.boundaryValue + "," + upperBound.boundaryValue
                + upperBound.direction;
    }

    private static final class Bound<K extends Comparable<? super K>> implements Comparable<Bound<K>> {

        private static final Bound<?> NEGATIVE_INFINITY = new Bound<Comparable<Object>>(null, IntervalDirection.LEFT);

        private static final Bound<?> POSITIVE_INFINITY = new Bound<Comparable<Object>>(null, IntervalDirection.RIGHT);

        private final K boundaryValue;

        private final IntervalDirection direction;

        public Bound(K boundaryValue, IntervalDirection direction) {
            this.boundaryValue = boundaryValue;
            this.direction = direction;
        }

        /**
         * Returns the bound constant representing the negative infinity. The generic type cast is
         * safe because the boundaryValue is null.
         */
        @SuppressWarnings("unchecked")
        public static <K extends Comparable<? super K>> Bound<K> negativeInfinity() {
            return (Bound<K>)NEGATIVE_INFINITY;
        }

        /**
         * Returns the bound constant representing the positive infinity. The generic type cast is
         * safe because the boundaryValue is null.
         */
        @SuppressWarnings("unchecked")
        public static <K extends Comparable<? super K>> Bound<K> positiveInfinity() {
            return (Bound<K>)POSITIVE_INFINITY;
        }

        public int compareTo(Bound<K> otherBound) {
            if (boundaryValue == null) {
                return direction == IntervalDirection.LEFT ? -1 : 1;
            }
            if (otherBound.boundaryValue == null) {
                return otherBound.direction == IntervalDirection.RIGHT ? -1 : 1;
            }
            int compareLowerBound = boundaryValue.compareTo(otherBound.boundaryValue);
            if (direction == otherBound.direction || compareLowerBound != 0) {
                return compareLowerBound;
            } else {
                return direction == IntervalDirection.RIGHT ? -1 : 1;
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((boundaryValue == null) ? 0 : boundaryValue.hashCode());
            result = prime * result + ((direction == null) ? 0 : direction.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Bound<?> other = (Bound<?>)obj;
            if (boundaryValue == null) {
                if (other.boundaryValue != null) {
                    return false;
                }
            } else if (!boundaryValue.equals(other.boundaryValue)) {
                return false;
            }
            if (direction != other.direction) {
                return false;
            }
            return true;
        }

    }

    private static enum IntervalDirection {

        /**
         * Indicates that the interval bound is opened to the left side. For example: "]1..2]" both
         * bounds are opened on the left-hand side.
         */
        LEFT("]"),

        /**
         * Indicates that the interval bound is opened to the right side. For example: "[1..2[" both
         * bounds are opened on the right-hand side.
         */
        RIGHT("[");

        private final String toStringRepresentation;

        private IntervalDirection(String toStringRepresentation) {
            this.toStringRepresentation = toStringRepresentation;
        }

        public static IntervalDirection getLowerBoundDirection(boolean inclusive) {
            if (inclusive) {
                return RIGHT;
            } else {
                return LEFT;
            }
        }

        public static IntervalDirection getUpperBoundDirection(boolean inclusive) {
            if (inclusive) {
                return LEFT;
            } else {
                return RIGHT;
            }
        }

        public boolean isLowerInclulsive() {
            return this == RIGHT;
        }

        public boolean isUpperInclulsive() {
            return this == LEFT;
        }

        @Override
        public String toString() {
            return toStringRepresentation;
        }

    }

}