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

/**
 * A range implementation where the upper and lower bounds are of the type
 * <code>org.faktorips.values.java5.Decimal</code>.
 * 
 * @author Jan Ortmann, Peter Erzberger
 * @author Daniel Hohenberger conversion to Java5
 */
public class DecimalRange extends DefaultRange<Decimal> {

    private static final long serialVersionUID = 5007646029371664759L;

    /**
     * Creates a new DecimalRange with the provided lower bound and upper bound.
     */
    public DecimalRange(Decimal lowerBound, Decimal upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new DecimalRange with the provided lower bound upper bound and step.
     */
    private DecimalRange(Decimal lowerBound, Decimal upperBound, Decimal step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates and new DecimalRange with the provided lower and upper bounds.
     */
    public static final DecimalRange valueOf(String lower, String upper) {
        return new DecimalRange(Decimal.valueOf(lower), Decimal.valueOf(upper));
    }

    /**
     * Creates and new DecimalRange with the provided lower and upper bounds, the step increment and
     * an indicator saying if the null value is contained.
     */
    public static final DecimalRange valueOf(String lower, String upper, String step, boolean containsNull) {
        return new DecimalRange(Decimal.valueOf(lower), Decimal.valueOf(upper), Decimal.valueOf(step), containsNull);
    }

    /**
     * Creates and new DecimalRange with the provided lower, upper bounds and step.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param step the step increment of this range. The parameter being null indicates that the
     *            range is continuous
     */
    public static final DecimalRange valueOf(Decimal lower, Decimal upper, Decimal step) {
        return valueOf(lower, upper, step, false);
    }

    /**
     * Creates and new DecimalRange with the provided lower, upper bounds and step.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param step the step increment of this range. The parameter being null indicates that the
     *            range is continuous
     * @param containsNull true indicates that the range contains null or the null representation
     *            value of the datatype of this range
     */
    public static final DecimalRange valueOf(Decimal lower, Decimal upper, Decimal step, boolean containsNull) {
        DecimalRange range = new DecimalRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Decimal value, Decimal bound) {

        Decimal step = getStep();
        Decimal zero = Decimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }
        Decimal diff = bound.subtract(value).abs();
        try {
            // throws an ArithmeticException if rounding is necessary. If the value is contained in
            // the range no rounding is necessary since this division must return an integer value
            diff.divide(getStep(), 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        return getUpperBound().subtract(getLowerBound()).abs().divide(getStep(), 0, BigDecimal.ROUND_UNNECESSARY)
                .intValue() + 1;
    }

    @Override
    protected Decimal getNextValue(Decimal currentValue) {
        return currentValue.add(getStep());
    }

    @Override
    protected Decimal getNullValue() {
        return Decimal.NULL;
    }

}
