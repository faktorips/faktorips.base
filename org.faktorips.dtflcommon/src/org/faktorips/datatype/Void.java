/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.apache.commons.lang.ObjectUtils;

/**
 * The datatype void representing <code>java.lang.Void</code>.
 * 
 * @author Jan Ortmann
 */
public class Void extends AbstractDatatype implements ValueDatatype {

    public String getName() {
        return "void"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "void"; //$NON-NLS-1$
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isImmutable() {
        return true;
    }

    public boolean isMutable() {
        return false;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public ValueDatatype getWrapperType() {
        return null;
    }

    public String getJavaClassName() {
        return "void"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        throw new UnsupportedOperationException("Can't get a default value for Datatype void."); //$NON-NLS-1$
    }

    public boolean isParsable(String value) {
        return false;
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    public boolean isNull(String value) {
        return value == null;
    }

    public boolean supportsCompare() {
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("The datatype " + getQualifiedName() //$NON-NLS-1$
                + " does not support comparison for values"); //$NON-NLS-1$
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return ObjectUtils.equals(valueA, valueB);
    }

    public Object getValue(String value) {
        throw new RuntimeException("Operation getValue not supported for void"); //$NON-NLS-1$
    }

}
