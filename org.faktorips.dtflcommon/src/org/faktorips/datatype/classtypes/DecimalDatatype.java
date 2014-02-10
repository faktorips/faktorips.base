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

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;
import org.faktorips.values.Decimal;

/**
 * Datatype for <code>Decimal</code>.
 * 
 * @author Jan Ortmann
 */
public class DecimalDatatype extends ValueClassDatatype implements NumericDatatype {

    public DecimalDatatype() {
        super(Decimal.class);
    }

    public DecimalDatatype(String name) {
        super(Decimal.class, name);
    }

    @Override
    public Object getValue(String s) {
        return Decimal.valueOf(s);
    }

    @Override
    public boolean hasNullObject() {
        return true;
    }

    public boolean supportsCompare() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        return ((Decimal)getValue(minuend)).subtract((Decimal)getValue(subtrahend)).toString();
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        Decimal a = Decimal.valueOf(dividend);
        Decimal b = Decimal.valueOf(divisor);
        try {
            a.divide(b, 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    public boolean hasDecimalPlaces() {
        return true;
    }

}
