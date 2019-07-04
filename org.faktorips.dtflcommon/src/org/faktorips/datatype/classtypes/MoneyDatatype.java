/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.values.Money;

/**
 * Datatype for <code>Money</code>.
 * 
 * @author Jan Ortmann
 */
public class MoneyDatatype extends ValueClassNameDatatype implements NumericDatatype {

    public MoneyDatatype() {
        super(Money.class.getSimpleName());
    }

    @Override
    public Money getValue(String s) {
        return Money.valueOf(s);
    }

    @Override
    public boolean hasNullObject() {
        return true;
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
        return Money.valueOf(minuend).subtract(Money.valueOf(subtrahend)).toString();
    }

    @Override
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        Money a = Money.valueOf(dividend);
        Money b = Money.valueOf(divisor);
        try {
            a.getAmount().divide(b.getAmount(), 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasDecimalPlaces() {
        return true;
    }

}
