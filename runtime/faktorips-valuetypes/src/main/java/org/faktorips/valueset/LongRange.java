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
import java.util.Optional;

/**
 * A {@link Range} class where upper and lower bounds are {@link Long Longs}.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class LongRange extends DefaultRange<Long> {

    private static final long serialVersionUID = -785773839824461985L;

    private static final LongRange EMPTY = new LongRange();

    /**
     * Creates a new empty {@link LongRange}.
     * 
     * @deprecated since 22.6. Use {@link LongRange#empty()} instead.
     */
    @Deprecated
    public LongRange() {
        super();
    }

    /**
     * @deprecated since 22.6. Use {@link LongRange#valueOf(Long, Long)} instead.
     */
    @Deprecated
    public LongRange(Long lowerBound, Long upperBound) {
        super(lowerBound, upperBound, Long.valueOf(1));
    }

    /**
     * Creates a new {@link LongRange} with the provided lower bound, upper bound and step.
     */
    private LongRange(Long lowerBound, Long upperBound, Long step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates an empty {@link LongRange}.
     */
    public static LongRange empty() {
        return EMPTY;
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds parsed using the
     * {@link Long#valueOf(String)} method. An empty string is interpreted as {@code null}. The step
     * increment is 1 and this range doesn't contain {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static LongRange valueOf(String lowerBound, String upperBound) {
        return valueOf(lowerBound, upperBound, "1", false);
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds. The step increment
     * is 1 and this range doesn't contain {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     */
    public static LongRange valueOf(Long lowerBound, Long upperBound) {
        return valueOf(lowerBound, upperBound, 1L, false);
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds and step parsed
     * using the {@link Long#valueOf(String)} method. An empty string is interpreted as
     * {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static LongRange valueOf(String lowerBound, String upperBound, String step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds and step.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     */
    public static LongRange valueOf(Long lowerBound, Long upperBound, Long step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds, the step increment
     * and an indicator saying whether the {@code null} value is contained. The strings are parsed
     * with the {@link Long#valueOf(long)} method. An empty String is interpreted as {@code null}.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static LongRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        Long min = (lowerBound == null || lowerBound.isEmpty()) ? null : Long.valueOf(lowerBound);
        Long max = (upperBound == null || upperBound.isEmpty()) ? null : Long.valueOf(upperBound);
        Long stepLong = (step == null || step.isEmpty()) ? null : Long.valueOf(step);
        LongRange range = new LongRange(min, max, stepLong, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    /**
     * Creates a new {@link LongRange} with the provided lower and upper bounds, the step increment
     * and an indicator saying whether the {@code null} value is contained.
     * 
     * @param lowerBound the lower bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param upperBound the upper bound of the range. The parameter being {@code null} indicates
     *            that the range is open on this side
     * @param step the step increment of this range. The parameter being {@code null} indicates that
     *            the range is continuous
     * @param containsNull {@code true} indicates that the range contains {@code null}
     */
    public static LongRange valueOf(Long lowerBound, Long upperBound, Long step, boolean containsNull) {
        LongRange range = new LongRange(lowerBound, upperBound, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Long value, Long bound) {
        if (getStep() == null) {
            return true;
        }
        if (getStep().longValue() == 0L) {
            throw new IllegalArgumentException(
                    "The step size cannot be zero. Use null to indicate a continuous range.");
        }
        long diff = Math.abs(bound - value);
        return divisibleWithoutRest(diff, getStep());
    }

    @Override
    protected int sizeForDiscreteValuesExcludingNull() {
        BigInteger diff = BigInteger.valueOf(Math.abs(getUpperBound() - getLowerBound()));
        BigInteger[] divAndRemainder = diff.divideAndRemainder(BigInteger.valueOf((getStep()).longValue()));
        BigInteger returnValue = divAndRemainder[0].add(BigInteger.valueOf(1));

        if (returnValue.longValue() > Integer.MAX_VALUE) {
            throw new RuntimeException(
                    "The number of values contained within this range is to huge to be supported by this operation.");
        }

        return returnValue.intValue();
    }

    @Override
    protected Long getNextValue(Long currentValue) {
        return currentValue + getStep();
    }

    @Override
    protected Long getNullValue() {
        return null;
    }

    @Override
    public Optional<Class<Long>> getDatatype() {
        return Optional.of(Long.class);
    }

    @Override
    protected boolean divisibleWithoutRest(Long dividend, Long divisor) {
        BigInteger[] divAndRemainder = BigInteger.valueOf(dividend).divideAndRemainder(BigInteger.valueOf(divisor));
        return divAndRemainder[1].longValue() == 0;
    }

}
