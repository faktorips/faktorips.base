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

import java.io.Serializable;

/**
 * An immutable data object defining a range between two columns. It is used by the
 * {@link TwoColumnRangeStructure} as key object for the internal map.
 * 
 * @see TwoColumnRangeStructure
 */
class TwoColumnRange<K extends Comparable<K>> implements Comparable<TwoColumnRange<K>>, Serializable {

    private static final long serialVersionUID = 42L;
    private final K lowerBound;
    private final K upperBound;

    /**
     * @param lowerBound The lowerBound of this TwoColumnRange.
     * @param upperBound The upperBound of this TwoColumnRange.
     */
    TwoColumnRange(K lowerBound, K upperBound) {
        super();
        if (lowerBound == null) {
            throw new NullPointerException();
        }
        if (upperBound == null) {
            throw new NullPointerException();
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int compareTo(TwoColumnRange<K> other) {
        return lowerBound.compareTo(other.lowerBound);
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
        return true;
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
     * @return Returns the upperBound.
     */
    public K getUpperBound() {
        return upperBound;
    }

    /**
     * Returns <code>true</code> if the upper bound of the specified otherKey is lower or equals to
     * the upper bound of this {@link TwoColumnRange}.
     * <p>
     * In {@link #compareTo(TwoColumnRange)} and {@link #equals(Object)} we only check the lower
     * bound. To get the correct key that matches the lower bound we already used
     * {@link #compareTo(TwoColumnRange)}. Using this method we could check the upper bound is also
     * matching.
     * 
     * @param otherKey The other key that's upper bound is compared to this one
     * @return <code>true</code> if the other upper bound is lower or equal to this upper bound
     * 
     */
    public boolean isLowerOrEqualUpperBound(TwoColumnRange<K> otherKey) {
        return getUpperBound().compareTo(otherKey.getUpperBound()) <= 0;
    }
}