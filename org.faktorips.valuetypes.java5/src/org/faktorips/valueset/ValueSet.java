/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.valueset;

import java.io.Serializable;
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
    public boolean contains(Object value);

    /**
     * Returns true if this set contains discrete values.
     */
    public boolean isDiscrete();

    /**
     * Returns true if this set contains null.
     */
    public boolean containsNull();

    /**
     * Returns <code>true</code> if the set is empty, otherwise <code>false</code>.
     */
    public boolean isEmpty();

    /**
     * Returns the number of values in the set or <code>Integer.MAX_VALUE</code> if there is an
     * unlimited number of values in the set.
     */
    public int size();

    /**
     * If this set contains discrete values this method returns all values. If null or a null
     * representation value (e.g. Decimal.NULL)is contained in the set the parameter excludeNull
     * determines if the return value contains it or not. If null is not part of the set the
     * parameter excludeNull is ignored.
     * 
     * @throws IllegalStateException if the method is called on a set that doesn't contain discrete
     *             values or has an unlimited number of values
     */
    public Set<? extends T> getValues(boolean excludeNull);

}
