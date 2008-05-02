/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.valueset.java5;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of the <code>org.faktorips.valueset.java5.ValueSet</code> interface for ordered
 * values.
 * 
 * @author Daniel Hohenberger
 */
public class OrderedValueSet<E> extends TreeSet<E> implements Serializable, ValueSet<E> {

    private static final long serialVersionUID = 2484960572251702320L;
    
    private boolean containsNull;
    private E nullValue;

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
     */
    public OrderedValueSet(boolean containsNull, E nullValue, E... values) {
        for (E e : values) {
            add(e);
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true.
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set
     */
    public OrderedValueSet(Collection<E> values, boolean containsNull, E nullValue) {
        addAll(values);
        initialize(containsNull, nullValue);
    }

    private void initialize(boolean containsNull, E nullValue) {
        this.containsNull = containsNull;
        this.nullValue = nullValue;
        if (containsNull && !super.contains(nullValue)) {
            add(nullValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<E> getValuesWithoutNull() {
        if (containsNull) {
            Set<E> set = (Set<E>)super.clone();
            set.remove(nullValue);
            return Collections.unmodifiableSet(set);
        }
        return Collections.unmodifiableSet(this);
    }

    /**
     * {@inheritDoc}
     */
    public Set<E> getValues(boolean excludeNull) {
        return excludeNull ? getValuesWithoutNull() : Collections.unmodifiableSet(this);
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
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj instanceof OrderedValueSet) {
            OrderedValueSet<? extends E> other = (OrderedValueSet<? extends E>)obj;
            return super.equals(other) && containsNull == other.containsNull && containsNull ? ((null == nullValue && null == other.nullValue) || (nullValue
                    .equals(other.nullValue)))
                    : true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsNull() {
        return containsNull;
    }

}
