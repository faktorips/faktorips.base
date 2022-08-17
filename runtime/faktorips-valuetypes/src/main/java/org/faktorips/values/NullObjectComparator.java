/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.values;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * {@link NullObject}-friendly comparator.
 *
 * @since 22.6
 */
// adapted from java.util.Comparators.NullComparator<T>
public class NullObjectComparator<N extends NullObjectSupport> implements Comparator<N>, Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean nullFirst;
    // if null, non-null Ns are considered equal
    private final Comparator<N> real;

    @SuppressWarnings("unchecked")
    NullObjectComparator(boolean nullFirst, Comparator<? super N> real) {
        this.nullFirst = nullFirst;
        this.real = (Comparator<N>)real;
    }

    /**
     * Returns a {@link NullObject}-friendly comparator that considers {@link NullObject
     * NullObjects} to be less than non-null. When both compared objects are {@link NullObject
     * NullObjects}, they are considered equal. If both are non-null, they are compared according to
     * their natural order.
     * 
     * @return a comparator that considers {@link NullObject NullObjects} to be less than non-null.
     */
    @SuppressWarnings("unchecked")
    public static <C extends NullObjectSupport & Comparable<C>> Comparator<C> nullsFirst() {
        return new NullObjectComparator<>(true, (Comparator<C>)Comparator.naturalOrder());
    }

    /**
     * Returns a {@link NullObject}-friendly comparator that considers {@link NullObject
     * NullObjects} to be greater than non-null. When both compared objects are {@link NullObject
     * NullObjects}, they are considered equal. If both are non-null, they are compared according to
     * their natural order.
     * 
     * @return a comparator that considers {@link NullObject NullObjects} to be greater than
     *             non-null.
     */
    @SuppressWarnings("unchecked")
    public static <C extends NullObjectSupport & Comparable<C>> NullObjectComparator<C> nullsLast() {
        return new NullObjectComparator<>(false, (Comparator<C>)Comparator.naturalOrder());
    }

    @Override
    public int compare(N a, N b) {
        if (ObjectUtil.isNull(a)) {
            return ObjectUtil.isNull(b) ? 0 : (nullFirst ? -1 : 1);
        } else if (ObjectUtil.isNull(b)) {
            return nullFirst ? 1 : -1;
        } else {
            return (real == null) ? 0 : real.compare(a, b);
        }
    }

    @Override
    public Comparator<N> thenComparing(Comparator<? super N> other) {
        Objects.requireNonNull(other);
        return new NullObjectComparator<>(nullFirst, real == null ? other : real.thenComparing(other));
    }

    @Override
    public Comparator<N> reversed() {
        return new NullObjectComparator<>(!nullFirst, real == null ? null : real.reversed());
    }
}
