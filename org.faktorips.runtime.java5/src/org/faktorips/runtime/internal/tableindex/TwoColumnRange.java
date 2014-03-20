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

    private final K lowerBound;

    private final boolean lowerInclusive;

    private final K upperBound;

    private final boolean upperInclusive;

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
            throw new NullPointerException();
        }
        if (upperBound == null) {
            throw new NullPointerException();
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerInclusive = lowerInclusive;
        this.upperInclusive = upperInclusive;
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
        int compareLowerBound = lowerBound.compareTo(other.lowerBound);
        if (lowerInclusive == other.lowerInclusive || compareLowerBound != 0) {
            return compareLowerBound;
        } else {
            // lowerInclusive != otherKey.lowerInclusive && compareLowerBound == 0
            return lowerInclusive ? -1 : 1;
        }
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
     * performance optimized! Thats why we do not simply use {@link #isContained(Comparable)}
     * 
     * @param otherKey The other key that's upper bound is compared to this one
     * @return <code>true</code> if this upper bound is below other's upper bound
     * 
     */
    public int compareToUpperBound(TwoColumnRange<K> otherKey) {
        int compareUpperBound = getUpperBound().compareTo(otherKey.getUpperBound());
        if (upperInclusive == otherKey.upperInclusive || compareUpperBound != 0) {
            return compareUpperBound;
        } else {
            // upperInclusive != otherKey.upperInclusive && compareUpperBound == 0
            return upperInclusive ? 1 : -1;
        }
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
        TwoColumnRange<?> other = (TwoColumnRange<?>)obj;
        if (lowerBound == null) {
            if (other.lowerBound != null) {
                return false;
            }
        } else if (!lowerBound.equals(other.lowerBound)) {
            return false;
        }
        return lowerInclusive == other.lowerInclusive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
        return result;
    }

    /**
     * @return Returns the lowerBound.
     */
    public K getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns whether the lower bound is inclusive or not.
     * 
     * @return <code>true</code> if the lower bound is inclusive otherwise <code>false</code>
     */
    public boolean isLowerInclusive() {
        return lowerInclusive;
    }

    /**
     * @return Returns the upperBound.
     */
    public K getUpperBound() {
        return upperBound;
    }

    /**
     * Returns whether the upper bound is inclusive or not.
     * 
     * @return <code>true</code> if the upper bound is inclusive otherwise <code>false</code>
     */
    public boolean isUpperInclusive() {
        return upperInclusive;
    }

    /**
     * Checks whether the value is within this range's bounds. Respects {@link #lowerInclusive} and
     * {@link #upperInclusive}.
     * 
     * @param value The value that should be checked if it is contained in this range.
     * @return <code>true</code> if the value is contained in this range
     */
    public boolean isContained(K value) {
        int lowerCompare = getLowerBound().compareTo(value);
        if (lowerInclusive ? lowerCompare > 0 : lowerCompare >= 0) {
            return false;
        }
        int upperCompare = getUpperBound().compareTo(value);
        if (upperInclusive ? upperCompare < 0 : upperCompare <= 0) {
            return false;
        }
        return true;
    }

}