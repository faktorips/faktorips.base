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
 * A Range class where upper and lower bounds are Doubles.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class DoubleRange extends DefaultRange<Double> {

    private static final long serialVersionUID = 3093772484960108819L;

    public DoubleRange(Double lower, Double upper, Double step, boolean containsNull) {
        super(lower, upper, step, containsNull);
    }

    public DoubleRange(Double lower, Double upper, boolean containsNull) {
        this(lower, upper, null, containsNull);
    }

    public DoubleRange(Double lower, Double upper) {
        this(lower, upper, false);
    }

    /**
     * Creates an DoubleRange based on the indicated Strings. The Strings are parsed with the
     * Double.valueOf() method. An asterisk (*) is interpreted as the maximum/minimum available
     * Double value. The step is set to <code>null</code>
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(String lower, String upper, boolean containsNull) {
        return valueOf(lower, upper, null, containsNull);
    }

    /**
     * Creates an DoubleRange based on the indicated Strings. The Strings are parsed with the
     * Double.valueOf() method. An asterisk (*) is interpreted as the maximum/minimum available
     * Double value.
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(String lower, String upper, String stepString, boolean containsNull) {
        Double min = lower == null || lower.isEmpty() ? null : Double.valueOf(lower);
        Double max = upper == null || upper.isEmpty() ? null : Double.valueOf(upper);
        Double step = stepString == null || stepString.isEmpty() ? null : Double.valueOf(stepString);
        return new DoubleRange(min, max, step, containsNull);
    }

    /**
     * Creates an DoubleRange based on the indicated Double values.
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(Double lower, Double upper, boolean containsNull) {
        return new DoubleRange(lower, upper, containsNull);
    }

    /**
     * Creates an DoubleRange based on the indicated Double values.
     * 
     * @param containsNull defines if null is part of the range or not
     */
    public static DoubleRange valueOf(Double lower, Double upper, Double step, boolean containsNull) {
        return new DoubleRange(lower, upper, step, containsNull);
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Double value, Double bound) {
        if (getStep() == 0) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
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
