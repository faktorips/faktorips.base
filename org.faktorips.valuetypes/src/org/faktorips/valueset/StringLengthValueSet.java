/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

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

    public boolean contains(String value) {
        if (value == null) {
            return containsNull();
        }
        return getMaximumLength() == null ? true : value.length() <= getMaximumLength();
    }

    public boolean containsNull() {
        return containsNull;
    }

    public Set<String> getValues(boolean excludeNull) {
        throw new IllegalStateException();
    }

    public boolean isDiscrete() {
        return false;
    }

    public boolean isEmpty() {
        return maximumLength == 0 && !containsNull;
    }

    public boolean isRange() {
        return false;
    }

    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return String.format("StringLengthValueSet (%1$s)", getMaximumLength());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (containsNull ? 1231 : 1237);
        result = prime * result + ((maximumLength == null) ? 0 : maximumLength.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof StringLengthValueSet
                && (this.containsNull() == ((StringLengthValueSet)o).containsNull())
                && (this.getMaximumLength().equals(((StringLengthValueSet)o).getMaximumLength())));
    }
}
