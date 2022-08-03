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

/**
 * A Range class where upper and lower bounds are {@link Double Doubles}.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class DoubleRange extends DefaultRange<Double> {

    private static final long serialVersionUID = 3093772484960108819L;

    private static final DoubleRange EMPTY = new DoubleRange();

    /**
     * Creates a new empty {@link DoubleRange}.
     * 
     * @deprecated since 22.6. Use {@link DoubleRange#empty()} instead.
     */
    @Deprecated
    public DoubleRange() {
        super();
    }

    /**
     * @deprecated since 22.6. Use {@link DoubleRange#valueOf(Double, Double, Double, boolean)}
     *                 instead.
     */
    @Deprecated
    public DoubleRange(Double lowerBound, Double upperBound, Double step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * @deprecated since 22.6. Use {@link DoubleRange#valueOf(Double, Double, boolean)} instead.
     */
    @Deprecated
    public DoubleRange(Double lowerBound, Double upperBound, boolean containsNull) {
        this(lowerBound, upperBound, null, containsNull);
    }

    /**
     * @deprecated since 22.6. Use {@link DoubleRange#valueOf(Double, Double)} instead.
     */
    @Deprecated
    public DoubleRange(Double lowerBound, Double upperBound) {
        this(lowerBound, upperBound, false);
    }

    /**
     * Creates an empty {@link DoubleRange}.
     */
    public static DoubleRange empty() {
        return EMPTY;
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value. The step is set to {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values. The step is set
     * to {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound) {
        return valueOf(lowerBound, upperBound, null, false);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value. The step is set to {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound, String step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound, Double step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value. The step is set to {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound, boolean containsNull) {
        return valueOf(lowerBound, upperBound, null, containsNull);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound, boolean containsNull) {
        return valueOf(lowerBound, upperBound, null, containsNull);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        Double min = lowerBound == null || lowerBound.isEmpty() ? null : Double.valueOf(lowerBound);
        Double max = upperBound == null || upperBound.isEmpty() ? null : Double.valueOf(upperBound);
        Double stepValue = step == null || step.isEmpty() ? null : Double.valueOf(step);
        DoubleRange range = new DoubleRange(min, max, stepValue, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound, Double step, boolean containsNull) {
        DoubleRange range = new DoubleRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Double value, Double bound) {
        if (getStep() == 0) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }
        double diff = Math.abs(bound - value);
        // Need to convert to BigDecimal to avoid floating point problems
        BigDecimal remaining = BigDecimal.valueOf(diff).remainder(BigDecimal.valueOf(getStep()));
        return remaining.equals(BigDecimal.valueOf(0, remaining.scale()));
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        double diff = Math.abs(getUpperBound() - getLowerBound());
        return 1 + Double.valueOf(diff / getStep()).intValue();
    }

    @Override
    protected Double getNextValue(Double currentValue) {
        return currentValue + getStep();
    }

    @Override
    protected Double getNullValue() {
        return null;
    }

}
