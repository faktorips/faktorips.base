/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 **************************************************************************************************/
package org.faktorips.valueset.java5;

import java.io.Serializable;
import java.util.Collection;

import org.faktorips.valueset.EnumValueSet;

/**
 * 
 * @author Daniel Hohenberger
 */
public class DefaultEnumValueSet implements EnumValueSet, Serializable {

    private static final long serialVersionUID = -7383945006925352479L;

    private final org.faktorips.valueset.DefaultEnumValueSet defaultEnumValueSet;

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
     * @throws IllegalArgumentException if the values array contains duplicate entries
     */
    public DefaultEnumValueSet(Object[] values, boolean containsNull, Object nullValue) {
        this.defaultEnumValueSet = new org.faktorips.valueset.DefaultEnumValueSet(values, containsNull, nullValue);
    }

    /**
     * Creates a new instance of DefaultEnumValueSet.
     * 
     * @param containsNull indicates if the provided values contain null or the null representation
     *            value
     * @param nullValue the java null value or null representation value for the datatype of this
     *            enumeration value set
     * 
     * @param values the values of this set. If these values contain null or the null representation
     *            value the parameter containsNull must be set to true. This implementation of
     *            EnumValueSet cannot validate if the provided values really contain null. The
     *            caller is responsible for that
     * @throws IllegalArgumentException if the values array contains duplicate entries
     */
    public DefaultEnumValueSet(boolean containsNull, Object nullValue, Object... values) {
        this.defaultEnumValueSet = new org.faktorips.valueset.DefaultEnumValueSet(values, containsNull, nullValue);
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
     * @throws IllegalArgumentException if the values collection contains duplicate entries
     */
    @SuppressWarnings("unchecked")
    public DefaultEnumValueSet(Collection values, boolean containsNull, Object nullValue) {
        this.defaultEnumValueSet = new org.faktorips.valueset.DefaultEnumValueSet(values, containsNull, nullValue);
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getValues(boolean excludeNull) {
        return defaultEnumValueSet.getValues(excludeNull);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDiscrete() {
        return defaultEnumValueSet.isDiscrete();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj instanceof DefaultEnumValueSet) {
            DefaultEnumValueSet other = (DefaultEnumValueSet)obj;
            return defaultEnumValueSet.equals(other.defaultEnumValueSet);
        }
        return defaultEnumValueSet.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return defaultEnumValueSet.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return defaultEnumValueSet.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Object value) {
        return defaultEnumValueSet.contains(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsNull() {
        return defaultEnumValueSet.containsNull();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return defaultEnumValueSet.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return defaultEnumValueSet.size();
    }

}
