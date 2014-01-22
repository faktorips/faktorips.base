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

package org.faktorips.devtools.core.model.valueset;

/**
 * Value set representing a range over a discrete or continuous set of values.
 * 
 * @author Thorsten Guenther
 */
public interface IRangeValueSet extends IValueSet {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "RANGE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    public static final String MSGCODE_LBOUND_GREATER_UBOUND = MSGCODE_PREFIX + "LBoundGreaterUBound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a step was only defined in this value set, but not
     * in the subset.
     */
    public static final String MSGCODE_NO_STEP_DEFINED_IN_SUBSET = MSGCODE_PREFIX + "NoStepDefinedInSubset"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the upper bound of the subset is greater than the
     * upper bound of this value set.
     */
    public static final String MSGCODE_UPPER_BOUND_VIOLATION = MSGCODE_PREFIX + "UpperBoundViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    public static final String MSGCODE_LOWER_BOUND_VIOLATION = MSGCODE_PREFIX + "LowerBoundViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of the attribute this range is based
     * on is not a numeric data type (ranges are only possible for numeric data types).
     */
    public static final String MSGCODE_NOT_NUMERIC_DATATYPE = MSGCODE_PREFIX + "notNumericDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the range defined by the lower and upper bound is
     * not dividable without remainder using the step.
     */
    public static final String MSGCODE_STEP_RANGE_MISMATCH = MSGCODE_PREFIX + "stepRangeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the step of this range is not dividable without
     * remainder by the step of another range.
     */
    public static final String MSGCODE_STEP_MISMATCH = MSGCODE_PREFIX + "stepMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of an other range is not dividable
     * without remainder by the step of this range.
     */
    public static final String MSGCODE_LOWERBOUND_MISMATCH = MSGCODE_PREFIX + "lowerBoundMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the upper bound of an other range is not dividable
     * without remainder by the step of this range.
     */
    public static final String MSGCODE_UPPERBOUND_MISMATCH = MSGCODE_PREFIX + "upperBoundMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value is not dividable without remainder by the
     * step of this range.
     */
    public static final String MSGCODE_STEP_VIOLATION = MSGCODE_PREFIX + "stepViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the step is not parsable.
     */
    public static final String MSGCODE_STEP_NOT_PARSABLE = MSGCODE_PREFIX + "StepNotParsable"; //$NON-NLS-1$

    public static final String PROPERTY_UPPERBOUND = "upperBound"; //$NON-NLS-1$

    public static final String PROPERTY_LOWERBOUND = "lowerBound"; //$NON-NLS-1$

    public static final String PROPERTY_STEP = "step"; //$NON-NLS-1$

    public static final String RANGE_VALUESET_START = "["; //$NON-NLS-1$

    public static final String RANGE_VALUESET_END = "]"; //$NON-NLS-1$

    public static final String RANGE_VALUESET_POINTS = "..."; //$NON-NLS-1$

    public static final String RANGE_VALUESET_SEPERATOR_WITH_WHITESPACE = "/"; //$NON-NLS-1$

    /**
     * Sets the lower bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException If <tt>lowerBound</tt> is <code>null</code>.
     */
    public void setLowerBound(String lowerBound);

    /**
     * Sets the step. An empty string means that no step exists and all possible values in the range
     * are valid.
     * 
     * @throws NullPointerException If <tt>step</tt> is <code>null</code>.
     */
    public void setStep(String step);

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException If <tt>upperBound</tt> is <code>null</code>.
     */
    public void setUpperBound(String upperBound);

    /**
     * Returns the lower bound of the range.
     */
    public String getLowerBound();

    /**
     * Returns the upper bound of the range.
     */
    public String getUpperBound();

    /**
     * Returns the step of the range.
     */
    public String getStep();

    /**
     * Adds or removes the null-value from the range.
     * 
     * @param containsNull <code>true</code> to add the null-value to this range or
     *            <code>false</code> to remove it.
     */
    public void setContainsNull(boolean containsNull);

}
