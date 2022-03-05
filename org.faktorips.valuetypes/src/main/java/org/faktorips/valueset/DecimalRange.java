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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A range implementation where the upper and lower bounds are of the type {@link Decimal}.
 * 
 * @author Jan Ortmann, Peter Erzberger
 * @author Daniel Hohenberger conversion to Java5
 */
public class DecimalRange extends DefaultRange<Decimal> {

    private static final long serialVersionUID = 5007646029371664759L;

    private static final DecimalRange EMPTY = new DecimalRange();

    /**
     * Creates a new empty {@link DecimalRange}.
     *
     * @deprecated since 22.6. Use {@link DecimalRange#empty()} instead.
     */
    @Deprecated
    public DecimalRange() {
        super();
    }

    /**
     * Creates a new {@link DecimalRange} with the provided lower and upper bound.
     * 
     * @deprecated since 22.6. Use {@link DecimalRange#valueOf(Decimal, Decimal)} instead.
     */
    @Deprecated
    public DecimalRange(Decimal lowerBound, Decimal upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Creates a new {@link DecimalRange} with the provided lower bound, upper bound and step.
     */
    private DecimalRange(Decimal lowerBound, Decimal upperBound, Decimal step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates an empty {@link DecimalRange}.
     */
    public static DecimalRange empty() {
        return EMPTY;
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static final DecimalRange valueOf(String lowerBound, String upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds.
     *
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static final DecimalRange valueOf(Decimal lowerBound, Decimal upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static final DecimalRange valueOf(String lowerBound, String upperBound, String step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static final DecimalRange valueOf(Decimal lowerBound, Decimal upperBound, Decimal step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds, the step
     * increment and an indicator saying if the {@link Decimal#NULL} value is contained.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull true indicates that the range contains the null representation value
     *            {@link Decimal#NULL}
     */
    public static final DecimalRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        DecimalRange range = new DecimalRange(Decimal.valueOf(lowerBound), Decimal.valueOf(upperBound),
                Decimal.valueOf(step),
                containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates and new {@link DecimalRange} with the provided lower and upper bounds, the step
     * increment and an indicator saying if the {@link Decimal#NULL} value is contained.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull true indicates that the range contains the null representation value
     *            {@link Decimal#NULL}
     */
    public static final DecimalRange valueOf(Decimal lowerBound,
            Decimal upperBound,
            Decimal step,
            boolean containsNull) {
        DecimalRange range = new DecimalRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "Only exceptions are of interest, the return value is not needed")
    protected boolean checkIfValueCompliesToStepIncrement(Decimal value, Decimal bound) {
        Decimal step = getStep();
        Decimal zero = Decimal.valueOf(0, step.scale());
        if (zero.equals(step)) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }
        Decimal diff = bound.subtract(value).abs();
        try {
            // throws an ArithmeticException if rounding is necessary. If the value is contained in
            // the range no rounding is necessary since this division must return an integer value
            diff.divide(getStep(), 0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        Decimal size = getUpperBound().subtract(getLowerBound()).abs()
                .divide(getStep(), 0, RoundingMode.UNNECESSARY).add(Decimal.valueOf(1));
        if (size.longValue() > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "The number of values contained within this range is to huge to be supported by this operation.");
        }
        return size.intValue();
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
