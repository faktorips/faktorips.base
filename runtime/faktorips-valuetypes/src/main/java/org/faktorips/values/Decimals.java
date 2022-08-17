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

import java.util.Arrays;
import java.util.Comparator;

import org.faktorips.annotation.UtilityClass;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Utility class for handling {@link Decimal} values and working with their {@link NullObject}
 * {@link Decimal#NULL}.
 */
@UtilityClass
public final class Decimals {

    private Decimals() {
        // Utility class that should not be instantiated
    }

    /**
     * Returns {@code true} if the given Decimal is {@code null} or {@code Decimal.NULL}.
     * 
     * @param d a {@link Decimal} that may be {@code null}
     * @return {@code true} if the given Decimal is {@code null} or {@code Decimal.NULL}
     */
    public static boolean isNull(@CheckForNull Decimal d) {
        return d == null || d.isNull();
    }

    /**
     * Returns {@code true} if any of the given Decimals is {@code null} or {@code Decimal.NULL}.
     * 
     * @param decimals an Array of {@link Decimal}
     * @return {@code true} if any of the given Decimals is {@code null} or {@code Decimal.NULL}
     */
    public static boolean isAnyNull(Decimal... decimals) {
        return Arrays.stream(decimals).anyMatch(Decimals::isNull);
    }

    /**
     * Returns {@code true} if the given Decimal is neither {@code null} nor {@code Decimal.NULL}.
     * Convenience method for {@code !isNull(d)}.
     * 
     * @param d a {@link Decimal} that may be {@code null}
     * @return {@code true} if the given Decimal is neither {@code null} nor {@code Decimal.NULL}
     */
    public static boolean isNotNull(@CheckForNull Decimal d) {
        return !isNull(d);
    }

    /**
     * Returns {@code true} if d1 is less than d2. Returns {@code false} if d1
     * {@linkplain #isNull(Decimal) is null}. Returns {@code true} if d2
     * {@linkplain #isNull(Decimal) is null}.
     * 
     * @param d1 a {@link Decimal} that may be {@code null}
     * @param d2 a {@link Decimal} that may be {@code null}
     * @return {@code true} if d1 is less than d2
     * @see #nullsLastComparator()
     */
    public static boolean lessThanIncludingNull(@CheckForNull Decimal d1, @CheckForNull Decimal d2) {
        return nullsLastComparator().compare(d1, d2) < 0;
    }

    /**
     * Returns the given Decimal or {@code Decimal.NULL} if the given Decimal is {@code null},
     * enforcing the {@link NullObject} pattern.
     * 
     * @param d a {@link Decimal} that may be {@code null}
     * @return the given Decimal or {@code Decimal.NULL} if the given Decimal is {@code null}
     */
    public static Decimal orNull(@CheckForNull Decimal d) {
        if (d == null) {
            return Decimal.NULL;
        }
        return d;
    }

    /**
     * Returns the minimum value ignoring {@code null}/{@link Decimal#NULL} values.
     * 
     * @param values The values to calculate the minimum value.
     * @return The minimum value or {@code Decimal.NULL} if there are no non-null values.
     */
    public static Decimal minIgnoreNull(Decimal... values) {
        return Arrays.stream(values).filter(Decimals::isNotNull).reduce(Decimal::min).orElse(Decimal.NULL);
    }

    /**
     * Returns the maximum value ignoring {@code null}/{@link Decimal#NULL} values.
     * 
     * @param values The values to calculate the maximum value.
     * @return The maximum value or {@code Decimal.NULL} if there are no non-null values.
     */
    public static Decimal maxIgnoreNull(Decimal... values) {
        return Arrays.stream(values).filter(Decimals::isNotNull).reduce(Decimal::max).orElse(Decimal.NULL);
    }

    /**
     * Returns a {@link Decimal#NULL}-friendly comparator that considers {@link Decimal#NULL} to be
     * less than non-null. When both are {@link Decimal#NULL}, they are considered equal. If both
     * are non-null, they are compared according to their natural order.
     * 
     * @return a comparator that considers {@link Decimal#NULL} to be less than non-null.
     * @since 22.6
     */
    public static Comparator<Decimal> nullsFirstComparator() {
        return NullObjectComparator.nullsFirst();
    }

    /**
     * Returns a {@link Decimal#NULL}-friendly comparator that considers {@link Decimal#NULL} to be
     * more than non-null. When both are {@link Decimal#NULL}, they are considered equal. If both
     * are non-null, they are compared according to their natural order.
     * 
     * @return a comparator that considers {@link Decimal#NULL} to be more than non-null.
     * @since 22.6
     */
    public static Comparator<Decimal> nullsLastComparator() {
        return NullObjectComparator.nullsLast();
    }

}
