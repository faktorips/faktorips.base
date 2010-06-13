/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
     * Creates and new BigDecimalRange with the provided lower and upper bounds.
     */
    public final static BigDecimalRange valueOf(String lower, String upper) {
        return new BigDecimalRange(new BigDecimal(lower), new BigDecimal(upper));
    }

    /**
     * Creates and new BigDecimalRange with the provided lower and upper bounds, the step increment
     * and an indicator saying if the null value is contained.
     */
    public final static BigDecimalRange valueOf(String lower, String upper, String step, boolean containsNull) {
        return new BigDecimalRange(new BigDecimal(lower), new BigDecimal(upper), new BigDecimal(step), containsNull);
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
    public final static BigDecimalRange valueOf(BigDecimal lower, BigDecimal upper, BigDecimal step) {
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
    public final static BigDecimalRange valueOf(BigDecimal lower,
            BigDecimal upper,
            BigDecimal step,
            boolean containsNull) {
        BigDecimalRange range = new BigDecimalRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

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
