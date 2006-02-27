package org.faktorips.devtools.core.model;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String EnumValueSet_msgValueNotInEnumeration;

	public static String EnumValueSet_msgNotAnEnumValueset;

	public static String EnumValueSet_msgValueNotParsable;

	public static String EnumValueSet_msgDuplicateValue;

	public static String Range_msgValueNotInRange;

	public static String Range_msgTypeOfValuesetNotMatching;

	public static String Range_msgNoStepDefinedInSubset;

	public static String Range_msgStepMismatch;

	public static String Range_msgLowerBoundViolation;

	public static String Range_msgUpperBoundViolation;

	public static String Range_msgValueNotParsable;

	public static String Range_msgValueNotComparable;

	public static String Range_msgUnknownDatatype;

	public static String Range_msgLowerboundGreaterUpperbound;

	public static String Range_msgPropertyValueNotParsable;
}
