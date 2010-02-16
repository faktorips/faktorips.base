/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.valueset;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of the <code>org.faktorips.valueset.java5.ValueSet</code> interface for ordered
 * values.
 * 
 * @author Daniel Hohenberger
 */
public class OrderedValueSet<E> implements Serializable, ValueSet<E> {

    private static final long serialVersionUID = 1735375680693336950L;

    private boolean containsNull;
    private E nullValue;
    private final LinkedHashSet<E> set = new LinkedHashSet<E>();

    private int hashCode;

    /**
     * Creates a new instance of <code>OrderedEnumValueSet</code>.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true.
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set
     * @throws IllegalArgumentException if the values array contains duplicate entries
     */
    public OrderedValueSet(boolean containsNull, E nullValue, E... values) {
        if (values != null) {
            for (E e : values) {
                if (set.contains(e)) {
                    throw new IllegalArgumentException("The provided values array contains duplicate entries.");
                }
                set.add(e);
            }
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true. if <code>values</code> is
     *            <code>null</code> the created set does not contain any values.
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set
     * @throws IllegalArgumentException if the values Collection contains duplicate entries
     */
    public OrderedValueSet(Collection<E> values, boolean containsNull, E nullValue) {
        if (values != null) {
            for (E e : values) {
                if (set.contains(e)) {
                    throw new IllegalArgumentException("The provided values Collection contains duplicate entries.");
                }
                set.add(e);
            }
        }
        initialize(containsNull, nullValue);
    }

    private void initialize(boolean containsNull, E nullValue) {
        this.containsNull = containsNull;
        this.nullValue = nullValue;
        if (containsNull && !set.contains(nullValue)) {
            set.add(nullValue);
        }
        calculateHashCode();
    }

    private void calculateHashCode() {
        int result = 17;
        for (E item : set) {
            if (item != null) {
                result = result * 37 + item.hashCode();
            }
        }
        hashCode = result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    @SuppressWarnings("unchecked")
    private Set<E> getValuesWithoutNull() {
        if (containsNull) {
            Set<E> set2 = (Set<E>)set.clone();
            set2.remove(nullValue);
            return Collections.unmodifiableSet(set2);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * {@inheritDoc}
     */
    public Set<E> getValues(boolean excludeNull) {
        return excludeNull ? getValuesWithoutNull() : Collections.unmodifiableSet(set);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDiscrete() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof OrderedValueSet) {
            OrderedValueSet<? extends E> other = (OrderedValueSet<? extends E>)obj;
            return set.equals(other.set)
                    && containsNull == other.containsNull
                    && (containsNull ? ((null == nullValue && null == other.nullValue) || (nullValue
                            .equals(other.nullValue))) : true);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsNull() {
        return containsNull;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (Iterator<E> it = set.iterator(); it.hasNext();) {
            E item = it.next();
            buf.append(item);
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(']');
        return buf.toString();
    }

    public boolean contains(Object value) {
        return set.contains(value);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public int size() {
        return set.size();
    }

}
