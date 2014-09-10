/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
     * Validation message code to indicate that the data type of the attribute this range is based
     * on is not a numeric data type (ranges are only possible for numeric data types).
     */
    public static final String MSGCODE_NOT_NUMERIC_DATATYPE = MSGCODE_PREFIX + "notNumericDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the range defined by the lower and upper bound is
     * not dividable without remainder using the step.
     */
    public static final String MSGCODE_STEP_RANGE_MISMATCH = MSGCODE_PREFIX + "stepRangeMissmatch"; //$NON-NLS-1$

    public static final String PROPERTY_UPPERBOUND = "upperBound"; //$NON-NLS-1$

    public static final String PROPERTY_LOWERBOUND = "lowerBound"; //$NON-NLS-1$

    public static final String PROPERTY_STEP = "step"; //$NON-NLS-1$

    public static final String RANGE_VALUESET_START = "["; //$NON-NLS-1$

    public static final String RANGE_VALUESET_END = "]"; //$NON-NLS-1$

    public static final String RANGE_VALUESET_POINTS = " ... "; //$NON-NLS-1$

    public static final String RANGE_STEP_SEPERATOR = " / "; //$NON-NLS-1$

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

}
