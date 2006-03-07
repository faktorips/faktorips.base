package org.faktorips.devtools.core.model;

/**
 * Valueset representing a range out of a discrete or continuous set of values.
 * 
 * @author Thorsten Guenther
 */
public interface IRangeValueSet extends IValueSet {

	/**
	 * Prefix for all message codes of this class.
	 */
	public final static String MSGCODE_PREFIX = "RANGE-"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that the lower bound of the subset is less than the lower
	 * bound of this value set. 
	 */
	public final static String MSGCODE_LBOUND_GREATER_UBOUND = MSGCODE_PREFIX
			+ "LBoundGreaterUBound"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that a step was only defined in this valueset, but not in the subset.
	 */
	public final static String MSGCODE_NO_STEP_DEFINED_IN_SUBSET = MSGCODE_PREFIX
			+ "NoStepDefinedInSubset"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that the steps of the both value sets are not equal. 
	 */
	public final static String MSGCODE_STEP_MISMATCH = MSGCODE_PREFIX
			+ "StepMismatch"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that the upper bound of the subset is greater than the
	 * upper bound of this value set. 
	 */
	public final static String MSGCODE_UPPER_BOUND_VIOLATION = MSGCODE_PREFIX
			+ "UpperBoundViolation"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that the lower bound of the subset is less than the lower
	 * bound of this value set. 
	 */
	public final static String MSGCODE_LOWER_BOUND_VIOLATION = MSGCODE_PREFIX
			+ "LowerBoundViolation"; //$NON-NLS-1$

	public final static String PROPERTY_UPPERBOUND = "upperBound"; //$NON-NLS-1$

	public final static String PROPERTY_LOWERBOUND = "lowerBound"; //$NON-NLS-1$

	public final static String PROPERTY_STEP = "step"; //$NON-NLS-1$

	/**
	 * Sets the lower bound. An empty string means that the range is unbouned.
	 * 
	 * @throws NullPointerException  if lowerBound is <code>null</code>.
	 */
	public void setLowerBound(String lowerBound);

	/**
	 * Sets the step. An empty string means that no step exists and all possible
	 * values in the range are valid.
	 * 
	 * @throws NullPointerException  if step is <code>null</code>.
	 */
	public void setStep(String step);

	/**
	 * Sets the upper bound. An empty string means that the range is unbounded.
	 * 
	 * @throws NullPointerException  if upperBound is <code>null</code>.
	 */
	public void setUpperBound(String upperBound);

	/**
	 * Returns the lower bound of the range
	 */
	public String getLowerBound();

	/**
	 * Returns the upper bound of the range
	 */
	public String getUpperBound();

	/**
	 * Returns the step of the range
	 */
	public String getStep();

}