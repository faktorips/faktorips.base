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
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the <code>org.faktorips.valueset.EnumValueSet</code> interface.
 * 
 * @author Peter Erzberger
 */
public class DefaultEnumValueSet<T> extends HashSet<T> implements Serializable, ValueSet<T> {

    private static final long serialVersionUID = -4073950730803261951L;

    private boolean containsNull;
    private T nullValue;

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true. This implementation of
     *            EnumValueSet cannot validate if the provided values really contain null. The
     *            caller is responsible for that
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set 
     */
    public DefaultEnumValueSet(boolean containsNull, T nullValue, T... values) {
        for (T t : values) {
            add(t);
        }
        initialize(containsNull, nullValue);
    }

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true. This implementation of
     *            EnumValueSet cannot validate if the provided values really contain null. The
     *            caller is responsible for that      
     */
    public DefaultEnumValueSet(T... values) {
        for (T t : values) {
            add(t);
        }
        initialize(false, null);
    }

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true. This implementation of
     *            EnumValueSet cannot validate if the provided values really contain null. The
     *            caller is responsible for that
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set
     */
    public DefaultEnumValueSet(Collection<T> values, boolean containsNull, T nullValue) {
        addAll(values);
        initialize(containsNull, nullValue);
    }

    private void initialize(boolean containsNull, T nullValue){
        this.containsNull = containsNull;
        this.nullValue = nullValue;
        if(containsNull)this.add(nullValue);
    }
    
    private Set<T> getValuesWithoutNull(){
        if(containsNull && super.contains(nullValue)){
            Set<T> set = new HashSet<T>(this);
            set.remove(nullValue);
            return Collections.unmodifiableSet(set);
        }
        return Collections.unmodifiableSet(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<T> getValues(boolean excludeNull) {
        return excludeNull?getValuesWithoutNull():Collections.unmodifiableSet(this);
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
    public boolean equals(Object obj){
        if (obj instanceof DefaultEnumValueSet) {
            DefaultEnumValueSet<? extends T> other = (DefaultEnumValueSet<? extends T>)obj;
            return super.equals(other) && 
                    containsNull == other.containsNull && 
                    nullValue == other.nullValue;
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
