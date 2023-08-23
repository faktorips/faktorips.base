/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a set of values. It can be a set of discrete values or a continuous range of values.
 * 
 * @author Peter Erzberger
 * @author Daniel Hohenberger conversion to Java5
 */
public interface ValueSet<T> extends Serializable {

    /**
     * Returns true if the provided value is in the set. A value is considered to be in the set if
     * the another value is in the set is found that is equal to it. Two values are considered to be
     * equal according to the equals() method semantic.
     */
    boolean contains(T value);

    /**
     * Returns true if this set contains discrete values. Also an empty {@link ValueSet} is
     * considered to be discrete.
     * 
     * If a {@link ValueSet} is discrete it is allowed to call {@link #getValues(boolean)}.
     * 
     * @return <code>true</code> if the value set contains discrete values or is empty
     * 
     * @see #getValues(boolean)
     */
    boolean isDiscrete();

    /**
     * Returns true if this set contains null.
     */
    boolean containsNull();

    /**
     * Returns <code>true</code> if the set is empty, otherwise <code>false</code>.
     */
    boolean isEmpty();

    /**
     * Returns <code>true</code> if the set is a range, otherwise <code>false</code>.
     */
    boolean isRange();

    /**
     * Returns the number of values in the set or <code>Integer.MAX_VALUE</code> if there is an
     * unlimited number of values in the set.
     */
    int size();

    /**
     * If this set contains discrete values this method returns all values. If null or a null
     * representation value (e.g. Decimal.NULL)is contained in the set the parameter excludeNull
     * determines if the return value contains it or not. If null is not part of the set the
     * parameter excludeNull is ignored.
     * 
     * @throws IllegalStateException if the method is called on a set that doesn't contain discrete
     *             values or has an unlimited number of values
     */
    Set<T> getValues(boolean excludeNull);

    /**
     * Determines whether this set restricts the number of its values.
     * <p>
     * For all value sets the following table shows how the parameter {@code excludeNull} and the
     * field {@code containsNull} of a {@link ValueSet} influences the return value of this method.
     * <table border="1">
     * <caption>&nbsp;</caption> <tbody>
     * <tr>
     * <td>excludeNull</td>
     * <td>containsNull</td>
     * <td>isUnrestricted</td>
     * </tr>
     * <tr>
     * <td>{@code true}</td>
     * <td>{@code true}</td>
     * <td>{@code true}</td>
     * </tr>
     * <tr>
     * <td>{@code true}</td>
     * <td>{@code false}</td>
     * <td>{@code true}</td>
     * </tr>
     * <tr>
     * <td>{@code false}</td>
     * <td>{@code true}</td>
     * <td>{@code true}</td>
     * </tr>
     * <tr>
     * <td>{@code false}</td>
     * <td>{@code false}</td>
     * <td>{@code false}</td>
     * </tr>
     * </tbody>
     * </table>
     * <p>
     * An {@link #isEmpty() empty} {@link ValueSet} is always considered to be restricted.
     *
     * @param excludeNull if {@code null} or a null representation value (e.g. {@code Decimal.NULL})
     *            should be considered
     * @return {@code true} if this set does not restrict the number of its values in any way.
     * @since 21.12
     */
    boolean isUnrestricted(boolean excludeNull);

    /**
     * Returns whether this {@link ValueSet} is a subset of (or the same as) the given other
     * {@link ValueSet}.
     * 
     * @since 24.1
     */
    boolean isSubsetOf(ValueSet<T> otherValueSet);

    Optional<Class<T>> getDatatype();

}
