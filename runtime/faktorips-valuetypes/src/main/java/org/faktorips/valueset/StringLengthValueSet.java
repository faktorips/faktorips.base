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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * ValueSet for Strings only. Limits the maximum length of values.
 */
public class StringLengthValueSet implements ValueSet<String> {

    private static final long serialVersionUID = 1L;

    private final boolean containsNull;

    private Integer maximumLength;

    /**
     * Constructs a new set with unlimited size (including {@code null}).
     */
    public StringLengthValueSet() {
        this(null, true);
    }

    public StringLengthValueSet(Integer maximumLength) {
        this(maximumLength, true);
    }

    public StringLengthValueSet(Integer maximumLength, boolean containsNull) {
        this.maximumLength = maximumLength;
        this.containsNull = containsNull;
    }

    public Integer getMaximumLength() {
        return maximumLength;
    }

    @Override
    public boolean contains(String value) {
        if (value == null || value.trim().isEmpty()) {
            return containsNull();
        }
        return getMaximumLength() == null ? true : value.length() <= getMaximumLength();
    }

    @Override
    public boolean containsNull() {
        return containsNull;
    }

    @Override
    public Set<String> getValues(boolean excludeNull) {
        if (isEmpty()) {
            return Collections.emptySet();
        }
        throw new IllegalStateException("This method cannot be called for value sets that are not discrete.");
    }

    @Override
    public boolean isDiscrete() {
        return isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return maximumLength != null && maximumLength == 0;
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
        return String.format("StringLengthValueSet (%1$s)", getMaximumLength());
    }

    @Override
    public int hashCode() {
        return Objects.hash(containsNull, maximumLength);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof StringLengthValueSet
                && (containsNull() == ((StringLengthValueSet)o).containsNull())
                && (getMaximumLength().equals(((StringLengthValueSet)o).getMaximumLength())));
    }

    /**
     * {@inheritDoc}
     * <p>
     * A {@link StringLengthValueSet} is considered restricted if the {@link #maximumLength} is not
     * {@code null}.
     */
    @Override
    public boolean isUnrestricted(boolean excludeNull) {
        if (maximumLength != null) {
            return false;
        }
        if (excludeNull) {
            return true;
        }
        return containsNull();
    }

    @Override
    public boolean isSubsetOf(ValueSet<String> otherValueSet) {
        if (otherValueSet instanceof UnrestrictedValueSet) {
            return otherValueSet.containsNull() || !containsNull();
        }
        if (otherValueSet instanceof StringLengthValueSet) {
            return (otherValueSet.containsNull() || !containsNull())
                    && isSameOrSmallerMaximumLengthAs(otherValueSet);
        }
        return false;
    }

    // CSOFF: BooleanExpressionComplexity
    private boolean isSameOrSmallerMaximumLengthAs(ValueSet<String> otherValueSet) {
        return (maximumLength != null
                && (((StringLengthValueSet)otherValueSet).maximumLength == null
                        || maximumLength <= ((StringLengthValueSet)otherValueSet).maximumLength))
                || (maximumLength == null && ((StringLengthValueSet)otherValueSet).maximumLength == null);
    }
    // CSON: BooleanExpressionComplexity

    @Override
    public Optional<Class<String>> getDatatype() {
        return Optional.of(String.class);
    }
}
