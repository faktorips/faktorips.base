/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import java.math.BigInteger;

/**
 * A Range class where upper and lower bounds are Longs.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 */
public class LongRange extends DefaultRange<Long> {

    private static final long serialVersionUID = -785773839824461985L;

    public LongRange(Long lower, Long upper) {
        super(lower, upper, new Long(1));
    }

    private LongRange(Long lower, Long upper, Long step, boolean containsNull) {
        super(lower, upper, step, containsNull);
    }

    /**
     * Creates a LongRange based on the indicated Strings. The Strings are parsed with the
     * Long.valueOf() method. An empty String is interpreted as <code>null</code>.
     */
    public static LongRange valueOf(String lower, String upper) {
        Long min = (lower == null || lower.isEmpty()) ? null : Long.valueOf(lower);
        Long max = (upper == null || upper.isEmpty()) ? null : Long.valueOf(upper);
        return new LongRange(min, max);
    }

    /**
     * Creates a LongRange based on the indicated Strings. The Strings are parsed with the
     * Long.valueOf() method. An empty String is interpreted as <code>null</code>. If the parameter
     * containsNull is true <code>null</code> is considered to be included within this range.
     */
    public static LongRange valueOf(String lower, String upper, String step, boolean containsNull) {
        Long min = (lower == null || lower.isEmpty()) ? null : Long.valueOf(lower);
        Long max = (upper == null || upper.isEmpty()) ? null : Long.valueOf(upper);
        Long stepLong = (step == null || step.isEmpty()) ? null : Long.valueOf(step);
        return new LongRange(min, max, stepLong, containsNull);
    }

    public static LongRange valueOf(Long lower, Long upper, Long step) {
        return valueOf(lower, upper, step, false);
    }

    public static LongRange valueOf(Long lower, Long upper, Long step, boolean containsNull) {
        LongRange range = new LongRange(lower, upper, step, containsNull);
        range.checkIfStepFitsIntoBounds();
        return range;
    }

    @Override
    protected boolean checkIfValueCompliesToStepIncrement(Long value, Long bound) {
        if (getStep().longValue() == 0L) {
            throw new IllegalArgumentException("The step size cannot be zero. Use null to indicate a continuous range.");
        }
        BigInteger diff = BigInteger.valueOf(Math.abs(getUpperBound() - getLowerBound()));
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
                    "The number of values contained within this range are to huge to be supported by this operation.");
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
