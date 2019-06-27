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

/**
 * A range implementation where the upper and lower bounds are of the type
 * <code>java.math.BigDecimal</code>.
 * 
 * @author Joerg Ortmann
 */
public class BigDecimalRange extends DefaultRange<BigDecimal> {

    private static final long serialVersionUID = -9040271817746215911L;

    /**
     * Creates a new BigDecimalRange with the provided lower bound and upper bound.
     */
    public BigDecimalRange(BigDecimal lowerBound, BigDecimal upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new BigDecimalRange with the provided lower bound upper bound and step.
     */
    private BigDecimalRange(BigDecimal lowerBound, BigDecimal upperBound, BigDecimal step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates and new BigDecimalRange with the provided lower and upper bounds.
     */
    public static final BigDecimalRange valueOf(String lower, String upper) {
        return new BigDecimalRange(bigDecimalOf(lower), bigDecimalOf(upper));
    }

    /**
     * Creates and new BigDecimalRange with the provided lower and upper bounds, the step increment
     * and an indicator saying if the null value is contained.
     */
    public static final BigDecimalRange valueOf(String lower, String upper, String step, boolean containsNull) {
        return new BigDecimalRange(bigDecimalOf(lower), bigDecimalOf(upper), bigDecimalOf(step), containsNull);
    }

    private static BigDecimal bigDecimalOf(String textToParse) {
        if (textToParse == null || textToParse.isEmpty()) {
            return null;
        } else {
            return new BigDecimal(textToParse);
        }
    }

    /**
     * Creates and new BigDecimalRange with the provided lower, upper bounds and step.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is open on this side
     * @param step the step increment of this range. The parameter being null indicates that the
     *            range is continuous
     */
    public static final BigDecimalRange valueOf(BigDecimal lower, BigDecimal upper, BigDecimal step) {
        return valueOf(lower, upper, step, false);
    }

    /**
     * Creates and new BigDecimalRange with the provided lower, upper bounds and step.
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
    public static final BigDecimalRange valueOf(BigDecimal lower,
            BigDecimal upper,
            BigDecimal step,
            boolean containsNull) {
        BigDecimalRange range = new BigDecimalRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(BigDecimal value, BigDecimal bound) {
        BigDecimal step = getStep();
        BigDecimal zero = BigDecimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }
        BigDecimal diff = bound.subtract(value).abs();
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
    protected BigDecimal getNextValue(BigDecimal currentValue) {
        return currentValue.add(getStep());
    }

    @Override
    protected BigDecimal getNullValue() {
        return null;
    }

}
