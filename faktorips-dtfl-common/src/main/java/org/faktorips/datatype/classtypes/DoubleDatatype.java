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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 
 * @author Jan Ortmann
 */
public class DoubleDatatype extends ValueClassNameDatatype implements NumericDatatype {

    public DoubleDatatype() {
        super(Double.class.getSimpleName());
    }

    @Override
    public Object getValue(String s) {
        if (IpsStringUtils.isEmpty(s)) {
            return null;
        }
        return Double.valueOf(s);
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
        double result = ((Double)getValue(minuend)).doubleValue() - ((Double)getValue(subtrahend)).doubleValue();
        return Double.toString(result);
    }

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }

        BigDecimal a = new BigDecimal(dividend);
        BigDecimal b = new BigDecimal(divisor);

        try {
            a.divide(b, 0, RoundingMode.UNNECESSARY);
            return true;
        } catch (ArithmeticException e) {
            return false;
        }
    }

    @Override
    public boolean hasDecimalPlaces() {
        return true;
    }

}
