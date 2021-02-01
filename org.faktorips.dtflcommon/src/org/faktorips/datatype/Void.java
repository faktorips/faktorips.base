/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import java.util.Objects;

/**
 * The datatype void representing <code>java.lang.Void</code>.
 * 
 * @author Jan Ortmann
 */
public class Void extends AbstractDatatype implements ValueDatatype {

    @Override
    public String getName() {
        return "void"; //$NON-NLS-1$
    }

    @Override
    public String getQualifiedName() {
        return "void"; //$NON-NLS-1$
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public String getDefaultValue() {
        throw new UnsupportedOperationException("Can't get a default value for Datatype void."); //$NON-NLS-1$
    }

    @Override
    public boolean isParsable(String value) {
        return false;
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) {
        throw new UnsupportedOperationException("The datatype " + getQualifiedName() //$NON-NLS-1$
                + " does not support comparison for values"); //$NON-NLS-1$
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return Objects.equals(valueA, valueB);
    }

    @Override
    public Object getValue(String value) {
        throw new RuntimeException("Operation getValue not supported for void"); //$NON-NLS-1$
    }

}
