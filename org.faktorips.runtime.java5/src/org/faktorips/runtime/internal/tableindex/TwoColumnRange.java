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

    public int compareTo(TwoColumnRange<K> other) {
        int compareLowerBound = lowerBound.compareTo(other.lowerBound);
        if (lowerInclusive == other.lowerInclusive || compareLowerBound != 0) {
            return compareLowerBound;
        } else {
            // lowerInclusive != otherKey.lowerInclusive && compareLowerBound == 0
            return lowerInclusive ? -1 : 1;
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
     * Returns <code>true</code> if the upper bound of this range is below the upper bound of the
     * specified other range. If both ranges defines the upper bound as inclusive or both exclusive
     * the method returns <code>true</code> in case of equal upper bounds. Otherwise it only returns
     * <code>true</code> if this upper bound is less than the other upper bound.
     * <p>
     * In {@link #compareTo(TwoColumnRange)} and {@link #equals(Object)} only the lower bound is
     * checked. Using this method we could check the upper bound is also matching.
     * 
     * @param otherKey The other key that's upper bound is compared to this one
     * @return <code>true</code> if this upper bound is below other's upper bound
     * 
     */
    public boolean isBelowUpperBoundOf(TwoColumnRange<K> otherKey) {
        int compareUpperBound = getUpperBound().compareTo(otherKey.getUpperBound());

        if (upperInclusive == otherKey.upperInclusive || compareUpperBound != 0) {
            return compareUpperBound <= 0;
        } else {
            // upperInclusive != otherKey.upperInclusive && compareUpperBound == 0
            return otherKey.upperInclusive;
        }
    }
}