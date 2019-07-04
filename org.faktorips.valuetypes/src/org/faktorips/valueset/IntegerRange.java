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

/**
 * A Range class where upper and lower bounds are Integers.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class IntegerRange extends DefaultRange<Integer> {

    private static final long serialVersionUID = 8454353227761904051L;

    /**
     * Creates and new IntegerRange with the provided lower and upper bounds. The step increment is
     * 1 and this range doesn't contain <code>null</code>
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     */
    public IntegerRange(Integer lower, Integer upper) {
        super(lower, upper, 1);
    }

    private IntegerRange(Integer lower, Integer upper, Integer step, boolean containsNull) {
        super(lower, upper, step, containsNull);
    }

    /**
     * Creates an IntegerRange based on the indicated Strings. The Strings are parsed with the
     * Integer.valueOf() method. An empty String is interpreted as <code>null</code>.
     */
    public static IntegerRange valueOf(String lower, String upper) {
        Integer min = (lower == null || lower.isEmpty()) ? null : Integer.valueOf(lower);
        Integer max = (upper == null || upper.isEmpty()) ? null : Integer.valueOf(upper);
        return new IntegerRange(min, max);

    }

    /**
     * Creates an IntegerRange based on the indicated Strings. The Strings are parsed with the
     * Integer.valueOf() method. An empty String is interpreted as <code>null</code>. If the
     * parameter containsNull is true <code>null</code> is considered to be included within this
     * range.
     */
    public static IntegerRange valueOf(String lower, String upper, String step, boolean containsNull) {
        Integer min = (lower == null || lower.isEmpty()) ? null : Integer.valueOf(lower);
        Integer max = (upper == null || upper.isEmpty()) ? null : Integer.valueOf(upper);
        Integer stepInt = (step == null || step.isEmpty()) ? null : Integer.valueOf(step);
        return new IntegerRange(min, max, stepInt, containsNull);
    }

    /**
     * Creates and new IntegerRange with the provided lower, upper bounds and step.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param step the step increment of this range. The parameter being null indicates that the
     *            range is continuous
     * @param containsNull if true than <code>null</code> is contained in the range
     */
    public static IntegerRange valueOf(Integer lower, Integer upper, Integer step, boolean containsNull) {
        IntegerRange range = new IntegerRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates and new IntegerRange with the provided lower, upper bounds.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     */
    public static IntegerRange valueOf(Integer lower, Integer upper) {
        return new IntegerRange(lower, upper);
    }

    /**
     * Creates and new IntegerRange with the provided lower, upper bounds and step.
     * 
     * @param lower the lower bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param upper the upper bound of the range. The parameter being null indicates that the range
     *            is unlimited on this side
     * @param step the step increment of this range.
     */
    public static IntegerRange valueOf(Integer lower, Integer upper, int step) {
        return valueOf(lower, upper, Integer.valueOf(step), false);
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {

        if (getStep() == 0) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }
        int diff = Math.abs(bound - value);
        int remaining = diff % getStep();
        return remaining == 0;
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        int lowerInt = getLowerBound();
        int upperInt = getUpperBound();
        int diff = Math.abs(upperInt - lowerInt);
        return diff / getStep() + 1;
    }

    @Override
    protected Integer getNextValue(Integer currentValue) {
        return currentValue + getStep();
    }

    @Override
    protected Integer getNullValue() {
        return null;
    }

}
