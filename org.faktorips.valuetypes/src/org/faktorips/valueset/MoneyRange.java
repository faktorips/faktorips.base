/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.math.BigDecimal;

import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * A range implementation where the upper and lower bounds are of the type
 * <code>org.faktorips.datatype.Money</code>.
 * 
 * @author Jan Ortmann
 * @see org.faktorips.values.Money
 */
public class MoneyRange extends DefaultRange<Money> {

    private static final long serialVersionUID = 4750295893441094927L;

    /**
     * Creates a new MoneyRange with the provided lower bound and upper bound.
     */
    public MoneyRange(Money lowerBound, Money upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new MoneyRange with the provided lower bound, upper bound and step.
     */
    private MoneyRange(Money lowerBound, Money upperBound, Money step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates a new MoneyRange with the provided lower and upper bounds parsed using the
     * Money.valueOf(String s) method.
     */
    public static final MoneyRange valueOf(String lower, String upper) {
        return new MoneyRange(Money.valueOf(lower), Money.valueOf(upper));
    }

    /**
     * Creates a new MoneyRange with the provided lower and upper bounds, the step increment and an
     * indicator saying if the null value is contained. The values are determined by parsing the
     * strings using the Money.valueOf(String s) method.
     */
    public static final MoneyRange valueOf(String lower, String upper, String step, boolean containsNull) {
        return new MoneyRange(Money.valueOf(lower), Money.valueOf(upper), Money.valueOf(step), containsNull);
    }

    public static final MoneyRange valueOf(Money lower, Money upper, Money step) {
        return valueOf(lower, upper, step, false);
    }

    public static final MoneyRange valueOf(Money lower, Money upper, Money step, boolean containsNull) {
        MoneyRange range = new MoneyRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Money value, Money bound) {
        Decimal step = getStep().getAmount();
        Decimal zero = Decimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }

        Decimal diff = bound.subtract(value).getAmount().abs();
        try {
            // throws an ArithmeticException if rounding is necessary. If the value is contained in
            // the range no rounding is necessary since this division must return an integer value
            diff.divide(getStep().getAmount(), 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    @Override
    protected Money getNextValue(Money currentValue) {
        return currentValue.add(getStep());
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        Decimal upperAmount = getUpperBound().getAmount();
        Decimal lowerAmount = getLowerBound().getAmount();
        Decimal stepAmount = getStep().getAmount();

        return upperAmount.subtract(lowerAmount).abs().divide(stepAmount, 0, BigDecimal.ROUND_UNNECESSARY).intValue() + 1;
    }

    @Override
    protected Money getNullValue() {
        return Money.NULL;
    }

}
