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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.values.NullObject;

/**
 * An {@link OrderedValueSet} that is sorted by natural order and therefore also is a {@link Range}.
 */
public class NaturalOrderedValueSet<E extends Comparable<E>> extends OrderedValueSet<E> implements Range<E> {

    private static final long serialVersionUID = 542101610772388291L;

    private transient E lowerBound;
    private transient E upperBound;

    /**
     * Creates a new instance of {@link NaturalOrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or the
     *            {@code null} representation value the parameter {@code containsNull} must be set
     *            to {@code true}
     * @param containsNull indicates whether the provided values contain {@code null} or the
     *            {@code null} representation value
     * @param nullValue the java {@code null} value or {@code null} representation value for the
     *            datatype of this value set
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    @SafeVarargs
    public NaturalOrderedValueSet(boolean containsNull, E nullValue, E... values) {
        super(containsNull, nullValue, values);
    }

    /**
     * Creates a new instance of {@link NaturalOrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or the
     *            {@code null} representation value the parameter {@code containsNull} must be set
     *            to {@code true}. If {@code values} is {@code null} the created set does not
     *            contain any values
     * @param containsNull indicates whether the provided values contain {@code null} or the
     *            {@code null} representation value
     * @param nullValue the java {@code null} value or {@code null} representation value for the
     *            datatype of this value set
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    public NaturalOrderedValueSet(Collection<E> values, boolean containsNull, E nullValue) {
        super(values, containsNull, nullValue);
    }

    /**
     * Creates a new instance of {@link NaturalOrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or a
     *            {@link NullObject} {@link #containsNull()} will return {@code true}. If
     *            {@code values} is {@code null} the created set does not contain any values.
     * 
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    public NaturalOrderedValueSet(Collection<E> values) {
        super(values);
    }

    /**
     * Creates a new instance of {@link NaturalOrderedValueSet}.
     * 
     * @param values the values of this set. If these values contain {@code null} or a
     *            {@link NullObject} {@link #containsNull()} will return {@code true}.
     * 
     * @throws IllegalArgumentException if the value collection contains duplicate entries
     */
    @SafeVarargs
    public NaturalOrderedValueSet(E... values) {
        super(Arrays.asList(values));
    }

    @Override
    protected SortedSet<E> createSetInternal() {
        return new TreeSet<>(Comparator.nullsLast(Comparator.naturalOrder()));
    }

    @Override
    public boolean isRange() {
        return true;
    }

    @Override
    public SortedSet<E> getValues() {
        return (SortedSet<E>)super.getValues();
    }

    @Override
    public SortedSet<E> getValues(boolean excludeNull) {
        return (SortedSet<E>)super.getValues(excludeNull);
    }

    @Override
    public E getLowerBound() {
        if (isEmpty()) {
            return null;
        }
        if (lowerBound == null) {
            lowerBound = getValues(true).first();
        }
        return lowerBound;
    }

    @Override
    public E getUpperBound() {
        if (isEmpty()) {
            return null;
        }
        if (upperBound == null) {
            upperBound = getValues(true).last();
        }
        return upperBound;
    }

    @Override
    public E getStep() {
        return null;
    }

}
