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

import java.math.BigInteger;

/**
 * A {@link Range} class where upper and lower bounds are {@link Long Longs}.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class LongRange extends DefaultRange<Long> {

    private static final long serialVersionUID = -785773839824461985L;

    /**
     * Creates a new empty {@link LongRange}.
     */
    public LongRange() {
        super();
    }

    public LongRange(Long lowerBound, Long upperBound) {
        super(lowerBound, upperBound, Long.valueOf(1));
    }

    private LongRange(Long lowerBound, Long upperBound, Long step, boolean containsNull) {
        super(lowerBound, upperBound, step, containsNull);
    }

    /**
     * Creates a {@link LongRange} based on the given strings. The strings are parsed with the
     * {@link Long#valueOf(String)} method. An empty string is interpreted as {@code null}.
     */
    public static LongRange valueOf(String lowerBound, String upperBound) {
        Long min = (lowerBound == null || lowerBound.isEmpty()) ? null : Long.valueOf(lowerBound);
        Long max = (upperBound == null || upperBound.isEmpty()) ? null : Long.valueOf(upperBound);
        return new LongRange(min, max);
    }

    /**
     * Creates a {@link LongRange} based on the given strings. The strings are parsed with the
     * {@link Long#valueOf(long)} method. An empty String is interpreted as {@code null}. If the
     * parameter {@code containsNull} is {@code true}, {@code null} is considered to be included
     * within this range.
     */
    public static LongRange valueOf(String lowerBound, String upperBound, String step, boolean containsNull) {
        Long min = (lowerBound == null || lowerBound.isEmpty()) ? null : Long.valueOf(lowerBound);
        Long max = (upperBound == null || upperBound.isEmpty()) ? null : Long.valueOf(upperBound);
        Long stepLong = (step == null || step.isEmpty()) ? null : Long.valueOf(step);
        return new LongRange(min, max, stepLong, containsNull);
    }

    public static LongRange valueOf(Long lowerBound, Long upperBound, Long step) {
        return valueOf(lowerBound, upperBound, step, false);
    }

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
        BigInteger diff = BigInteger.valueOf(Math.abs(bound - value));
        BigInteger[] divAndRemainder = diff.divideAndRemainder(BigInteger.valueOf(getStep().longValue()));
        return divAndRemainder[1].longValue() == 0;
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

}
