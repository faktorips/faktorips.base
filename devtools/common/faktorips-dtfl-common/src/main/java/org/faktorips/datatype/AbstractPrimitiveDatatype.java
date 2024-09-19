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

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Abstract base class for datatypes representing a Java primtive like boolean.
 *
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatype extends AbstractDatatype implements ValueDatatype {

    public AbstractPrimitiveDatatype() {
        super();
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public String valueToString(Object value) {
        return "" + value; //$NON-NLS-1$
    }

    /**
     * If the value is <code>null</code> or an empty string, <code>false</code> is returned.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        if (IpsStringUtils.isEmpty(value)) {
            return false;
        }
        try {
            getValue(value);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasNullObject() {
        return true;
    }

    @Override
    public abstract Object getValue(String value);

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return Objects.equals(getValue(valueA), getValue(valueB));
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(String valueA, String valueB) {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("Datatype " + getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison of values"); //$NON-NLS-1$
        }
        return ((Comparable<Object>)getValue(valueA)).compareTo(getValue(valueB));
    }

    @Override
    public String getNullObjectId() {
        return getDefaultValue();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

}
