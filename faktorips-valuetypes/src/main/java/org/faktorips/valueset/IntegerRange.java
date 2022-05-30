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

import java.math.BigInteger;

/**
 * A Range class where upper and lower bounds are {@link Integer Integers}.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class IntegerRange extends DefaultRange<Integer> {

    private static final long serialVersionUID = 8454353227761904051L;

    private static final IntegerRange EMPTY = new IntegerRange();

    /**
     * Creates a new empty {@link IntegerRange}.
     * 
     * @deprecated since 22.6. Use {@link IntegerRange#empty()} instead.
     */
    @Deprecated
    public IntegerRange() {
        super();
    }

    /**
     * Creates and new {@link IntegerRange} with the provided lower and upper bounds. The step
     * increment is 1 and this range doesn't contain {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * 
     * @deprecated since 22.6. Use {@link IntegerRange#valueOf(Integer, Integer)} instead.
     */
    @Deprecated
    public IntegerRange(Integer lowerBound, Integer upperBound) {
        super(lowerBound, upperBound, 1);
    }

    private IntegerRange(Integer lowerBound, Integer upperBound, Integer step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates an empty {@link IntegerRange}.
     */
    public static IntegerRange empty() {
        return EMPTY;
    }

    /**
     * Creates an {@link IntegerRange} with the provided lower and upper bounds. The step increment
     * is 1 and this range doesn't contain {@code null}. The strings are parsed with the
     * {@link Integer#valueOf(String)} method. An empty string is interpreted as {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     */
    public static IntegerRange valueOf(String lowerBound, String upperBound) {
        return valueOf(lowerBound, upperBound, "1", false);
    }

    /**
     * Creates and new {@link IntegerRange} with the provided lower and upper bounds. The step
     * increment is 1 and this range doesn't contain {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     */
    public static IntegerRange valueOf(Integer lowerBound, Integer upperBound) {
        return valueOf(lowerBound, upperBound, 1, false);
    }

    /**
     * Creates and new {@link IntegerRange} with the provided lower and upper bounds as well as the
     * step. The strings are parsed with the {@link Integer#valueOf(String)} method. An empty string
     * is interpreted as {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param step the step increment of this range.
     */
    public static IntegerRange valueOf(String lowerBound, String upperBound, String step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates and new {@link IntegerRange} with the provided lower and upper bounds as well as the
     * step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param step the step increment of this range.
     */
    public static IntegerRange valueOf(Integer lowerBound, Integer upperBound, int step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates an {@link IntegerRange} based on the given strings. The strings are parsed with the
     * {@link Integer#valueOf(String)} method. An empty string is interpreted as {@code null}. If
     * the parameter {@code containsNull} is {@code true}, {@code null} is considered to be included
     * within this range.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull if {@code true}, {@code null} is contained in the range
     */
    public static IntegerRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        Integer min = (lowerBound == null || lowerBound.isEmpty()) ? null : Integer.valueOf(lowerBound);
        Integer max = (upperBound == null || upperBound.isEmpty()) ? null : Integer.valueOf(upperBound);
        Integer stepInt = (step == null || step.isEmpty()) ? null : Integer.valueOf(step);
        IntegerRange range = new IntegerRange(min, max, stepInt, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates and new {@link IntegerRange} with the provided lower and upper bounds as well as the
     * step. If the parameter {@code containsNull} is {@code true}, {@code null} is considered to be
     * included within this range.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is unlimited on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull if {@code true}, {@code null} is contained in the range
     */
    public static IntegerRange valueOf(Integer lowerBound, Integer upperBound, Integer step, boolean containsNull) {
        IntegerRange range = new IntegerRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Integer value, Integer bound) {
        if (getStep() == 0) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }
        int diff = Math.abs(bound - value);
        int remaining = diff % getStep();
        return remaining == 0;
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        BigInteger diff = BigInteger.valueOf(Math.abs((long)getUpperBound() - (long)getLowerBound()));
        BigInteger[] divAndRemainder = diff.divideAndRemainder(BigInteger.valueOf((getStep()).longValue()));
        BigInteger returnValue = divAndRemainder[0].add(BigInteger.valueOf(1));

        if (returnValue.longValue() > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "The number of values contained within this range is to huge to be supported by this operation.");
        }

        return returnValue.intValue();
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
