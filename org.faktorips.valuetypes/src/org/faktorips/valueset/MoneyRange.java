/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.math.RoundingMode;

import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A range implementation where the upper and lower bounds are of the type {@link Money}.
 * 
 * @author Jan Ortmann
 * @see org.faktorips.values.Money
 */
public class MoneyRange extends DefaultRange<Money> {

    private static final long serialVersionUID = 4750295893441094927L;

    private static final MoneyRange EMPTY = new MoneyRange();

    /**
     * Creates a new empty {@link MoneyRange}.
     */
    public MoneyRange() {
        super();
    }

    /**
     * Creates a new {@link MoneyRange} with the provided lower bound and upper bound.
     */
    public MoneyRange(Money lowerBound, Money upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new {@link MoneyRange} with the provided lower bound, upper bound and step.
     */
    private MoneyRange(Money lowerBound, Money upperBound, Money step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates an empty {@link MoneyRange}.
     */
    public static MoneyRange empty() {
        return EMPTY;
    }

    /**
     * Creates a new {@link MoneyRange} with the provided lower and upper bounds parsed using the
     * {@link Money#valueOf(String)} method.
     */
    public static final MoneyRange valueOf(String lowerBound, String upperBound) {
        return new MoneyRange(Money.valueOf(lowerBound), Money.valueOf(upperBound));
    }

    /**
     * Creates a new {@link MoneyRange} with the provided lower and upper bounds, the step increment
     * and an indicator saying whether the {@code null} value is contained. The values are
     * determined by parsing the strings using the {@link Money#valueOf(String)} method.
     */
    public static final MoneyRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        return new MoneyRange(Money.valueOf(lowerBound), Money.valueOf(upperBound), Money.valueOf(step), containsNull);
    }

    public static final MoneyRange valueOf(Money lowerBound, Money upperBound, Money step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    public static final MoneyRange valueOf(Money lowerBound, Money upperBound, Money step, boolean containsNull) {
        MoneyRange range = new MoneyRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "Only exceptions are of interest, the return value is not needed")
    protected boolean checkIfValueCompliesToStepIncrement(Money value, Money bound) {
        Decimal step = getStep().getAmount();
        Decimal zero = Decimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }

        Decimal diff = bound.subtract(value).getAmount().abs();
        try {
            // throws an ArithmeticException if rounding is necessary. If the value is contained in
            // the range no rounding is necessary since this division must return an integer value
            diff.divide(getStep().getAmount(), 0, RoundingMode.UNNECESSARY);
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

        Decimal size = upperAmount.subtract(lowerAmount).abs().divide(stepAmount, 0, RoundingMode.UNNECESSARY)
                .add(1);
        if (size.longValue() > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "The number of values contained within this range is to huge to be supported by this operation.");
        }
        return size.intValue();
    }

    @Override
    protected Money getNullValue() {
        return Money.NULL;
    }

}
