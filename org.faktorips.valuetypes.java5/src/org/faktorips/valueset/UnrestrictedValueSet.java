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

import java.util.Set;

/**
 * Special case of value set that represents an unrestricted value set. This is a value set that
 * does not restrict the values allowed by a datatype (Integer, String, etc.).
 * 
 * @author Jan Ortmann
 */
public class UnrestrictedValueSet<T> implements ValueSet<T> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public boolean contains(Object value) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsNull() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Set<? extends T> getValues(boolean excludeNull) {
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDiscrete() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "UnrestrictedValueSet";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UnrestrictedValueSet<?>;
    }

}
