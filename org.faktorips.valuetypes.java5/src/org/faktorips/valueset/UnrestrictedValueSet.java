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

import java.util.Set;

/**
 * Special case of value set that represents an unrestricted value set. This is a value set that
 * does not restrict the values allowed by a datatype (Integer, String, etc.).
 * 
 * @author Jan Ortmann
 */
public class UnrestrictedValueSet<T> implements ValueSet<T> {

    private static final long serialVersionUID = 1L;

    public boolean contains(Object value) {
        return true;
    }

    public boolean containsNull() {
        return true;
    }

    public Set<T> getValues(boolean excludeNull) {
        throw new IllegalStateException();
    }

    public boolean isDiscrete() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

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
