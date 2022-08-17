/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Datatype for <code>Integer</code>.
 * 
 * @author Jan Ortmann
 */
public class IntegerDatatype extends ValueClassNameDatatype implements NumericDatatype {

    public IntegerDatatype() {
        super(Integer.class.getSimpleName());
    }

    @Override
    public Object getValue(String s) {
        if (IpsStringUtils.isEmpty(s)) {
            return null;
        }
        return Integer.valueOf(s);
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
        Integer intA = (Integer)getValue(dividend);
        Integer intB = (Integer)getValue(divisor);

        if (intA == null) {
            throw new NumberFormatException("The dividend '" + dividend + "' can not be parsed to an Integer"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (intB == null) {
            throw new NumberFormatException("The divisor '" + divisor + "' can not be parsed to an Integer"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int a = intA.intValue();
        int b = intB.intValue();

        return b == 0 ? false : a % b == 0;
    }

    @Override
    public boolean hasDecimalPlaces() {
        return false;
    }

}
