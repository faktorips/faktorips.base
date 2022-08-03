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

import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable data object representing a range by defining its upper and lower bound. It is used
 * by the {@link TwoColumnRangeStructure} as key object for the internal map.
 * <p>
 * Infinite ranges can be defined by setting the upper and/or lower bound to <code>null</code>.
 * 
 * @see TwoColumnRangeStructure
 */
class TwoColumnRange<K extends Comparable<? super K>> implements Comparable<TwoColumnRange<K>>, Serializable {

    private static final long serialVersionUID = 42L;

    private final Bound<K> lowerBound;

    private final Bound<K> upperBound;

    /**
     * Defines a range including the values from (and including) the lower bound up to (and
     * including) the upper bound. Semantically equivalent to
     * <code>TwoColumnRange(lowerBound, upperBound, true, true)</code>.
     * <p>
     * Infinite ranges can be defined by passing in <code>null</code> as upper and/or lower bound.
     * <ul>
     * <li><code>new TwoColumnRange(null, 100)</code> defines a range from negative infinity to 100.
     * This range includes all values less than and equal to 100.</li>
     * <li>Calling <code>new TwoColumnRange(0, null)</code> in turn defines a range from 0 to
     * positive infinity. This range includes all values greater than and equal to 0.</li>
     * <li>Calling <code>new TwoColumnRange(null, null)</code> defines a range from negative to
     * positive infinity. This range includes all values.</li>
     * </ul>
     * 
     * @param lowerBound The lowerBound of this TwoColumnRange, <code>null</code> for negative
     *            infinity.
     * @param upperBound The upperBound of this TwoColumnRange, <code>null</code> for positive
     *            infinity.
     */
    TwoColumnRange(K lowerBound, K upperBound) {
        this(lowerBound, upperBound, true, true);
    }

    /**
     * Defines a range containing the values from the lower bound up to the upper bound. Whether
     * upper- and lower bounds themselves are included in the range is controlled by the flags
     * lowerInclusive and upperInclusive. <code>true</code> indicates a boundary value is included,
     * <code>false</code> indicates it is excluded and thus not contained in the range.
     * <p>
     * These flags are ignored for infinite bounds, infinity is always implicitly excluded. For more
     * information on infinite ranges see {@link #TwoColumnRange(Comparable, Comparable)}
     * 
     * @param lowerBound The lowerBound of this TwoColumnRange, <code>null</code> for negative
     *            infinity.
     * @param upperBound The upperBound of this TwoColumnRange, <code>null</code> for positive
     *            infinity.
     * @param lowerInclusive whether the lower bound is included in the range, ignored if lowerBound
     *            is <code>null</code>.
     * @param upperInclusive whether the upper bound is included in the range, ignored if upperBound
     *            is <code>null</code>.
     * @see #TwoColumnRange(Comparable, Comparable)
     */
    TwoColumnRange(K lowerBound, K upperBound, boolean lowerInclusive, boolean upperInclusive) {
        if (lowerBound == null) {
            this.lowerBound = Bound.<K> negativeInfinity();
        } else {
            this.lowerBound = new Bound<>(lowerBound, IntervalDirection.getLowerBoundDirection(lowerInclusive));
        }
        if (upperBound == null) {
            this.upperBound = Bound.<K> positiveInfinity();
        } else {
            this.upperBound = new Bound<>(upperBound, IntervalDirection.getUpperBoundDirection(upperInclusive));
        }
    }

    /**
     * Compares two {@link TwoColumnRange ranges} using their lower bounds.
     * <p>
     * Returns a negative integer, zero or a positive integer as the lower bound of this range is
     * less than, equal to or greater than the lower bound of the other range.
     * <p>
     * If both lower bounds are equal, compares whether the two ranges include or exclude the lower
     * bound:
     * <ul>
     * <li>Returns a negative integer if this range excludes the lower bound, but the other range
     * includes it.</li>
     * <li>Returns a positive integer if this range includes the lower bound, but the other range
     * excludes it.</li>
     * <li>Returns zero if both lower bounds are equal and both ranges define the lower bound as
     * exclusive or inclusive respectively.</li>
     * </ul>
     * 
     * @param other The other range whose lower bound is compared to this one
     * @return a negative integer, zero or a positive integer as the lower bound of this range is
     *             less than, equal to or greater than the lower bound of the other range.
     * 
     */
    @Override
    public int compareTo(TwoColumnRange<K> other) {
        return lowerBound.compareTo(other.lowerBound);
    }

    /**
     * Compares two {@link TwoColumnRange ranges} using their upper bounds.
     * <p>
     * Returns a negative integer, zero or a positive integer as the upper bound of this range is
     * less than, equal to or greater than the upper bound of the other range.
     * <p>
     * If both upper bounds are equal, compares whether the two ranges include or exclude the upper
     * bound:
     * <ul>
     * <li>Returns a negative integer if this range excludes the upper bound, but the other range
     * includes it.</li>
     * <li>Returns a positive integer if this range includes the upper bound, but the other range
     * excludes it.</li>
     * <li>Returns zero if both upper bounds are equal and both ranges define the upper bound as
     * exclusive or inclusive respectively.</li>
     * </ul>
     * 
     * @param otherKey The other range whose upper bound is compared to this one
     * @return a negative integer, zero or a positive integer as the upper bound of this range is
     *             less than, equal to or greater than the upper bound of the other range.
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
     * <li>[0..10] and [5..20] overlap</li>
     * <li>[3..5] and [5..8] overlap, both contain 5.</li>
     * <li>[3..5[ and [5..8] do not overlap, however, as [3..5[ does not include 5.</li>
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
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        TwoColumnRange<?> other = (TwoColumnRange<?>)obj;
        return lowerBound.equals(other.lowerBound);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + lowerBound.hashCode();
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

    /**
     * Represents a bound of a range. Holds the boundary value and the direction to which the brace
     * is opened.
     */
    private static final class Bound<K extends Comparable<? super K>> implements Comparable<Bound<K>>, Serializable {

        private static final long serialVersionUID = 1L;

        private static final Bound<?> NEGATIVE_INFINITY = new Bound<Comparable<Object>>(null, IntervalDirection.LEFT);

        private static final Bound<?> POSITIVE_INFINITY = new Bound<Comparable<Object>>(null, IntervalDirection.RIGHT);

        private final K boundaryValue;

        private final IntervalDirection direction;

        /**
         * 
         * @param boundaryValue the value that is the upper or lower bound of the range
         * @param direction the direction to which the brace is opened. Right for "[", left for "]".
         */
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

        @Override
        public int compareTo(Bound<K> otherBound) {
            if (isInfinity() || otherBound.isInfinity()) {
                return compareInfinity(otherBound);
            }
            int compareLowerBound = boundaryValue.compareTo(otherBound.boundaryValue);
            if (direction == otherBound.direction || compareLowerBound != 0) {
                return compareLowerBound;
            } else {
                return direction == IntervalDirection.RIGHT ? -1 : 1;
            }
        }

        private int compareInfinity(Bound<K> otherBound) {
            if (isInfinity() && otherBound.isInfinity()) {
                if (direction == otherBound.direction) {
                    return 0;
                } else {
                    return direction == IntervalDirection.LEFT ? -1 : 1;
                }
            } else if (isInfinity()) {
                return direction == IntervalDirection.LEFT ? -1 : 1;
            } else {
                return otherBound.direction == IntervalDirection.RIGHT ? -1 : 1;
            }
        }

        private boolean isInfinity() {
            return boundaryValue == null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(boundaryValue, direction);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            Bound<?> other = (Bound<?>)obj;
            return Objects.equals(boundaryValue, other.boundaryValue)
                    && (direction == other.direction);
        }

    }

    /**
     * The direction to which the interval-brace is opened.
     */
    private enum IntervalDirection {

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

        IntervalDirection(String toStringRepresentation) {
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
