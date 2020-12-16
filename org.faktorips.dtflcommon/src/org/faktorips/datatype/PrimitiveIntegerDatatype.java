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

/**
 * Datatype for the primitive <code>int</code>.
 */
public class PrimitiveIntegerDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    @Override
    public String getName() {
        return "int"; //$NON-NLS-1$
    }

    @Override
    public String getQualifiedName() {
        return "int"; //$NON-NLS-1$
    }

    @Override
    public ValueDatatype getWrapperType() {
        return Datatype.INTEGER;
    }

    @Override
    public String getDefaultValue() {
        return "0"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Integer.valueOf(value);
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        int result = ((Integer)getValue(minuend)).intValue() - ((Integer)getValue(subtrahend)).intValue();
        return Integer.toString(result);
    }

    @Override
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        int a = ((Integer)getValue(dividend)).intValue();
        int b = ((Integer)getValue(divisor)).intValue();
        return b == 0 ? false : a % b == 0;
    }

    @Override
    public boolean hasDecimalPlaces() {
        return false;
    }

}
