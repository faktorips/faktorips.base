/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    public boolean isRange() {
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
