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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A {@link Range} implementation where the upper and lower bounds are of the type
 * {@link BigDecimal}.
 * 
 * @author Joerg Ortmann
 */
public class BigDecimalRange extends DefaultRange<BigDecimal> {

    private static final long serialVersionUID = -9040271817746215911L;

    private static final BigDecimalRange EMPTY = new BigDecimalRange();

    /**
     * Creates a new empty {@link BigDecimalRange}.
     * 
     * @deprecated since 22.6. Use {@link BigDecimalRange#empty()} instead.
     */
    @Deprecated
    public BigDecimalRange() {
        super();
    }

    /**
     * Creates a new {@link BigDecimalRange} with the provided lower and upper bound.
     * 
     * @deprecated since 22.6. Use {@link BigDecimalRange#valueOf(BigDecimal, BigDecimal)} instead.
     */
    @Deprecated
    public BigDecimalRange(BigDecimal lowerBound, BigDecimal upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new {@link BigDecimalRange} with the provided lower bound, upper bound and step.
     */
    private BigDecimalRange(BigDecimal lowerBound, BigDecimal upperBound, BigDecimal step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates an empty {@link BigDecimalRange}.
     */
    public static BigDecimalRange empty() {
        return EMPTY;
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static final BigDecimalRange valueOf(String lowerBound, String upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static final BigDecimalRange valueOf(BigDecimal lowerBound, BigDecimal upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static final BigDecimalRange valueOf(String lowerBound, String upperBound, String step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static final BigDecimalRange valueOf(BigDecimal lowerBound, BigDecimal upperBound, BigDecimal step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds, the step
     * increment and an indicator saying whether the <code>null</code> value is contained.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static final BigDecimalRange valueOf(String lowerBound,
            String upperBound,
            String step,
            boolean containsNull) {
        BigDecimalRange range = new BigDecimalRange(bigDecimalOf(lowerBound), bigDecimalOf(upperBound),
                bigDecimalOf(step),
                containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates and new {@link BigDecimalRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static final BigDecimalRange valueOf(BigDecimal lowerBound,
            BigDecimal upperBound,
            BigDecimal step,
            boolean containsNull) {
        BigDecimalRange range = new BigDecimalRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    private static BigDecimal bigDecimalOf(String textToParse) {
        if (textToParse == null || textToParse.isEmpty()) {
            return null;
        } else {
            return new BigDecimal(textToParse);
        }
    }

    @Override
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Only exceptions are of interest, the return value is not needed")
    protected boolean checkIfValueCompliesToStepIncrement(BigDecimal value, BigDecimal bound) {
        BigDecimal step = getStep();
        BigDecimal zero = BigDecimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }
        BigDecimal diff = bound.subtract(value).abs();
        return divisibleWithoutRest(diff, getStep());
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        if (getStep() == null) {
            if (getLowerBound() == null || getUpperBound() == null) {
                return Integer.MAX_VALUE;
            }
            return getLowerBound().compareTo(getUpperBound()) == 0 ? 1 : Integer.MAX_VALUE;
        }
        BigDecimal size = getUpperBound().subtract(getLowerBound()).abs()
                .divide(getStep(), 0, RoundingMode.UNNECESSARY).add(BigDecimal.ONE);
        if (size.longValue() > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "The number of values contained within this range is to huge to be supported by this operation.");
        }
        return size.intValue();
    }

    @Override
    protected BigDecimal getNextValue(BigDecimal currentValue) {
        return currentValue.add(getStep());
    }

    @Override
    protected BigDecimal getNullValue() {
        return null;
    }

    @Override
    public Optional<Class<BigDecimal>> getDatatype() {
        return Optional.of(BigDecimal.class);
    }

    @Override
    protected boolean divisibleWithoutRest(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal[] divAndRemainder = dividend.divideAndRemainder(divisor);
        return divAndRemainder[1].compareTo(BigDecimal.ZERO) == 0;
    }

}
