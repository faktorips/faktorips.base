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

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>long</code>.
 */
public class PrimitiveLongDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    public String getName() {
        return "long"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "long"; //$NON-NLS-1$
    }

    public ValueDatatype getWrapperType() {
        return Datatype.LONG;
    }

    public String getJavaClassName() {
        return "long"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        return "0"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Long.valueOf(value);
    }

    public boolean supportsCompare() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        long result = ((Long)getValue(minuend)).longValue() - ((Long)getValue(subtrahend)).longValue();
        return Long.toString(result);
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        long a = ((Long)getValue(dividend)).longValue();
        long b = ((Long)getValue(divisor)).longValue();
        return b == 0 ? false : a % b == 0;
    }

    public boolean hasDecimalPlaces() {
        return false;
    }

}
