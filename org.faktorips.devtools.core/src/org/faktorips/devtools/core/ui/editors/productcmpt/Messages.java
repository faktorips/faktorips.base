package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.productcmpt.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String PolicyAttributesSection_policyAttributes;

	public static String PolicyAttributesSection_defaultLabelPrefix;

	public static String PolicyAttributesSection_defaultLabelPostfix;

	public static String PropertiesPage_properties;

	public static String PropertiesPage_relations;

	public static String PropertiesPage_noRelationsDefined;

	public static String PolicyAttributeEditDialog_editLabel;

	public static String PolicyAttributeEditDialog_properties;

	public static String PolicyAttributeEditDialog_defaultValue;

	public static String RelationEditDialog_editRelation;

	public static String RelationEditDialog_properties;

	public static String RelationEditDialog_target;

	public static String RelationEditDialog_cardinalityMin;

	public static String RelationEditDialog_cardinalityMax;

	public static String ProductAttributesSection_productAttributes;

	public static String ProductAttributesSection_template;

	public static String ProductCmptEditor_msgNotContainingAttributes;

	public static String ProductCmptEditor_msgAttributesNotFound;

	public static String ProductCmptEditor_msgTypeMismatch;

	public static String ProductCmptEditor_msgValueAttributeMismatch;

	public static String ProductCmptEditor_msgNoRelationDefined;

	public static String ProductCmptEditor_msgFixIt;

	public static String ProductCmptEditor_productComponent;

	public static String FormulaEditDialog_editFormula;

	public static String FormulaEditDialog_Formula;

	public static String FormulaEditDialog_availableParameters;

	public static String FormulasSection_calculationFormulas;

	public static String PolicyAttributesSection_defaultsAndRanges;

	public static String PolicyAttributesSection_values;

	public static String PolicyAttributesSection_minimum;

	public static String PolicyAttributesSection_maximum;

	public static String PolicyAttributesSection_step;

	public static String ProductAttributesSection_attribute;

	public static String RelationsSection_cardinality;

	public static String PolicyAttributesSection_noDefaultsAndRangesDefined;

	public static String RelationsLabelProvider_undefined;

	public static String FormulasSection_noFormulasDefined;
}
