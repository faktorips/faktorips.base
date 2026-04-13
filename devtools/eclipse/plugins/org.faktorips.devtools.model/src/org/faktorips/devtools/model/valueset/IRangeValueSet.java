/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

/**
 * Value set representing a range over a discrete or continuous set of values.
 *
 * @author Thorsten Guenther
 */
public interface IRangeValueSet extends IValueSet {

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "RANGE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    String MSGCODE_LBOUND_GREATER_UBOUND = MSGCODE_PREFIX + "LBoundGreaterUBound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of the attribute this range is based
     * on is not a numeric data type (ranges are only possible for numeric data types).
     */
    String MSGCODE_NOT_NUMERIC_DATATYPE = MSGCODE_PREFIX + "notNumericDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the range defined by the lower and upper bound is
     * not dividable without remainder using the step.
     */
    String MSGCODE_STEP_RANGE_MISMATCH = MSGCODE_PREFIX + "stepRangeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower and upper bounds are equal but at least
     * one bound is open, resulting in an empty range.
     */
    String MSGCODE_LBOUND_EQUAL_UBOUND_OPEN = MSGCODE_PREFIX + "LBoundEqualUBoundOpen"; //$NON-NLS-1$

    String PROPERTY_UPPERBOUND = "upperBound"; //$NON-NLS-1$

    String PROPERTY_LOWERBOUND = "lowerBound"; //$NON-NLS-1$

    String PROPERTY_STEP = "step"; //$NON-NLS-1$

    String PROPERTY_EMPTY = "empty"; //$NON-NLS-1$

    String PROPERTY_LOWERBOUND_OPEN = "lowerBoundOpen"; //$NON-NLS-1$

    String PROPERTY_UPPERBOUND_OPEN = "upperBoundOpen"; //$NON-NLS-1$

    String RANGE_VALUESET_START = "["; //$NON-NLS-1$

    String RANGE_VALUESET_END = "]"; //$NON-NLS-1$

    String RANGE_VALUESET_START_OPEN = "("; //$NON-NLS-1$

    String RANGE_VALUESET_END_OPEN = ")"; //$NON-NLS-1$

    String RANGE_VALUESET_POINTS = " ... "; //$NON-NLS-1$

    String RANGE_STEP_SEPERATOR = " / "; //$NON-NLS-1$

    /**
     * Sets the lower bound. An empty string means that the range is unbounded.
     *
     * @throws NullPointerException if {@code lowerBound} is {@code null}
     */
    void setLowerBound(String lowerBound);

    /**
     * Sets the step. An empty string means that no step exists and all possible values in the range
     * are valid.
     *
     * @throws NullPointerException if {@code step} is {@code null}
     */
    void setStep(String step);

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     *
     * @throws NullPointerException if {@code upperBound} is {@code null}
     */
    void setUpperBound(String upperBound);

    /**
     * Returns the lower bound of the range.
     */
    String getLowerBound();

    /**
     * Returns the upper bound of the range.
     */
    String getUpperBound();

    /**
     * Returns the step of the range.
     */
    String getStep();

    /**
     * Returns whether the lower bound is open (exclusive). An open lower bound means the value at
     * the lower bound itself is not included in the range. For example, in the range (0..10], 0 is
     * excluded but 10 is included.
     */
    boolean isLowerBoundOpen();

    /**
     * Returns whether the upper bound is open (exclusive). An open upper bound means the value at
     * the upper bound itself is not included in the range. For example, in the range [0..10), 0 is
     * included but 10 is excluded.
     */
    boolean isUpperBoundOpen();

    /**
     * Sets whether the upper bound is open (exclusive) or closed (inclusive).
     */
    void setUpperBoundOpen(boolean upperBoundOpen);

    /**
     * Sets whether the lower bound is open (exclusive) or closed (inclusive).
     */
    void setLowerBoundOpen(boolean lowerBoundOpen);

    /**
     * Sets the range to empty. Will reset all bounds and step if set to {@code true}.
     *
     * @since 20.6
     */
    void setEmpty(boolean empty);

}
