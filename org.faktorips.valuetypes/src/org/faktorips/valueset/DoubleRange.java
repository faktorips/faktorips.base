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

    /**
     * Creates a new empty {@link DoubleRange}.
     */
    public DoubleRange() {
        super();
    }

    public DoubleRange(Double lowerBound, Double upperBound, Double step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    public DoubleRange(Double lowerBound, Double upperBound, boolean containsNull) {
        this(lowerBound, upperBound, null, containsNull);
    }

    public DoubleRange(Double lowerBound, Double upperBound) {
        this(lowerBound, upperBound, false);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value. The step is set to {@code null}.
     * 
     * @param containsNull defines whether {@code null} is part of the range or not
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound, boolean containsNull) {
        return valueOf(lowerBound, upperBound, null, containsNull);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated strings. The strings are parsed with the
     * {@link Double#valueOf(String)} method. An asterisk (*) is interpreted as the maximum/minimum
     * available {@link Double} value.
     * 
     * @param containsNull defines whether {@code null} is part of the range or not
     */
    public static DoubleRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        Double min = lowerBound == null || lowerBound.isEmpty() ? null : Double.valueOf(lowerBound);
        Double max = upperBound == null || upperBound.isEmpty() ? null : Double.valueOf(upperBound);
        Double stepValue = step == null || step.isEmpty() ? null : Double.valueOf(step);
        return new DoubleRange(min, max, stepValue, containsNull);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values.
     * 
     * @param containsNull defines whether {@code null} is part of the range or not
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound, boolean containsNull) {
        return new DoubleRange(lowerBound, upperBound, containsNull);
    }

    /**
     * Creates a {@link DoubleRange} based on the indicated {@link Double} values.
     * 
     * @param containsNull defines whether {@code null} is part of the range or not
     */
    public static DoubleRange valueOf(Double lowerBound, Double upperBound, Double step, boolean containsNull) {
        return new DoubleRange(lowerBound, upperBound, step, containsNull);
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
