/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import java.io.Serializable;

public class TwoColumnKey<K extends Comparable<K>> implements Comparable<TwoColumnKey<K>>, Serializable {

    private static final long serialVersionUID = 42L;
    private final K lowerBound;
    private final K upperBound;

    /**
     * @param lowerBound The lowerBound of this TwoColumnKey.
     * @param upperBound The upperBound of this TwoColumnKey.
     */
    public TwoColumnKey(K lowerBound, K upperBound) {
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

    public int compareTo(TwoColumnKey<K> other) {
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
        TwoColumnKey<?> other = (TwoColumnKey<?>)obj;
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
     * A two-column-key defines a range or mathematically speaking an interval by upper and lower
     * bound. This method tests whether this key defines a sub-range of the argument key. If this
     * key is a sub range, all values defined by this key are also contained in the argument key.
     * 
     * @returns <code>true</code> if both ranges have identical upper and lower bounds. Returns
     *          <code>true</code> if this key is a true sub-range of the argument key. Returns
     *          <code>false</code>, however, if both ranges only partially overlap or have no common
     *          elements at all.
     */
    public boolean isSubRangeOf(TwoColumnKey<K> otherKey) {
        return getLowerBound().compareTo(otherKey.getLowerBound()) >= 0
                && getUpperBound().compareTo(otherKey.getUpperBound()) <= 0;
    }
}