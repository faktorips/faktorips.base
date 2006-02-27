package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.pctype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String Relation_msgErrorMaxCardinalityMalformed;

	public static String Relation_msgMaxCardinalityMustBeAtLeast1;

	public static String Relation_msgMaxCardinalityForContainerRelationTooLow;

	public static String Relation_msgMinCardinalityGreaterThanMaxCardinality;

	public static String Relation_msgContainerRelNotInSupertype;

	public static String Relation_27;

	public static String Relation_msgNotMarkedAsContainerRel;

	public static String Relation_msgNoTarget;

	public static String Relation_msgTargetNotSubclass;

	public static String Relation_msgContainerRelNotReverseRel;

	public static String Relation_msgSamePluralRolename;

	public static String Relation_msgSameSingularRoleName;

	public static String Relation_msgRelationNotInTarget;

	public static String Relation_26;

	public static String Relation_msgReverseRelationNotSpecified;

	public static String Relation_msgReverseRelOfContainerRelMustBeContainerRelToo;

	public static String Relation_msgReverseCompositionMissmatch;

	public static String Relation_msgReverseAssociationMissmatch;

	public static String Method_msgInvalidMethodname;

	public static String Method_msgTypeEmpty;

	public static String Method_18;

	public static String Method_abstractMethodError;

	public static String Method_21;

	public static String Method_msgNameEmpty;

	public static String Method_msgInvalidParameterName;

	public static String Method_msgDatatypeEmpty;

	public static String Method_msgDatatypeNotFound;

	public static String Method_30;

	public static String ValidationRule_msgFunctionNotExists;

	public static String ValidationRule_msgIgnored;

	public static String ValidationRule_msgUndefinedAttribute;

	public static String ValidationRule_msgDuplicateEntries;

	public static String Attribute_msgInvalidAttributeName;

	public static String Attribute_msgDefaultNotParsable_UnknownDatatype;

	public static String Attribute_12;

	public static String Attribute_msgValueNotParsable_InvalidDatatype;

	public static String Attribute_15;

	public static String Attribute_msgValueTypeMismatch;

	public static String Attribute_18;

	public static String Attribute_19;

	public static String Attribute_msgDefaultNotInValueset;

	public static String Attribute_23;

	public static String Attribute_msgNoInputparams;

	public static String Attribute_msgNoParamsNeccessary;

	public static String Attribute_msgEmptyName;

	public static String Attribute_msgInvalidParamName;

	public static String Attribute_msgDatatypeEmpty;

	public static String Attribute_msgDatatypeNotFound;

	public static String Attribute_36;

	public static String PolicyCmptType_msgSupertypeNotFound;

	public static String PolicyCmptType_10;

	public static String PolicyCmptType_msgNameMissing;

	public static String PolicyCmptType_msgAbstractMissmatch;

	public static String PolicyCmptType_msgMustOverrideAbstractMethod;

	public static String PolicyCmptType_17;
}
