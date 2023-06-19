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

import java.util.Objects;
import java.util.Set;

import org.faktorips.values.NullObject;

/**
 * Special case of value set that represents an unrestricted value set. This is a value set that
 * does not restrict the values allowed by a datatype (Integer, String, etc.).
 *
 * @author Jan Ortmann
 */
public class UnrestrictedValueSet<T> implements ValueSet<T> {

    private static final long serialVersionUID = 1L;

    private final boolean containsNull;

    /**
     * Constructs a new set, which acts as if it contains every possible value (including
     * {@code null}).
     */
    public UnrestrictedValueSet() {
        this(true);
    }

    /**
     * Constructs a new set, which acts as if it contains every possible value (potentially
     * including {@code null}, depending on the given parameter).
     *
     * @param containsNull whether {@code null} is included in this set
     */
    public UnrestrictedValueSet(boolean containsNull) {
        this.containsNull = containsNull;
    }

    /**
     * Returns {@code true} if the given value is not {@code null} or a {@link NullObject}. The
     * return value for {@code null} is dependent on the {@code containsNull} constructor parameter.
     */
    @Override
    public boolean contains(T value) {
        if (value == null || value instanceof NullObject) {
            return containsNull;
        } else {
            return true;
        }
    }

    @Override
    public boolean containsNull() {
        return containsNull;
    }

    @Override
    public Set<T> getValues(boolean excludeNull) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isDiscrete() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "UnrestrictedValueSet";
    }

    @Override
    public int hashCode() {
        return Objects.hash(containsNull);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof UnrestrictedValueSet
                && (this.containsNull() == ((UnrestrictedValueSet<?>)o).containsNull()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnrestricted(boolean excludeNull) {
        if (excludeNull) {
            return true;
        }
        return containsNull();
    }
}
