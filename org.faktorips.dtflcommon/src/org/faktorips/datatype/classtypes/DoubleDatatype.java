/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * 
 * @author Jan Ortmann
 */
public class DoubleDatatype extends ValueClassDatatype implements NumericDatatype {

    public DoubleDatatype() {
        super(Double.class);
    }

    public DoubleDatatype(String name) {
        super(Double.class, name);
    }

    @Override
    public Object getValue(String s) {
        if (StringUtils.isEmpty(s)) {
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
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }

        BigDecimal a = new BigDecimal(dividend);
        BigDecimal b = new BigDecimal(divisor);

        try {
            a.divide(b, 0, BigDecimal.ROUND_UNNECESSARY);
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
