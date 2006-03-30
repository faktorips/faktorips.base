/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

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

	public static String ProductCmptEditor_msg_GenerationMissmatch;
	
	public static String ProductCmptEditor_title_GenerationMissmatch;

	public static String RelationsContentProvider_msg_UnknownElementClass;

	public static String GenerationsSection_titleShowGeneration;

	public static String GenerationsSection_msgShowGeneration;

	public static String ProductAttributesSection_labelGenerationValidTo;

	public static String ProductAttributesSection_valueGenerationValidToUnlimited;

	public static String ProductAttributesSection_true;

	public static String ProductAttributesSection_false;

	public static String RulesPage_title;

	public static String RulesSection_title;

	public static String GenerationsSection_displayPostfix;

	public static String ProductAttributesSection_noProductCmptType;

	public static String CardinalityPanel_labelOptional;

	public static String CardinalityPanel_labelMandatory;

	public static String CardinalityPanel_labelOther;

	public static String MissingResourcePage_msgFileOutOfSync;

	public static String ProductCmptEditor_msgFileOutOfSync;

	public static String SetTemplateDialog_titleNewTemplate;

	public static String SetTemplateDialog_labelNewTemplate;

	public static String SetTemplateDialog_msgTemplateDoesNotExist;

	public static String ProductCmptEditor_msgTemplateNotFound;

	public static String ProductCmptEditor_titleEmpty;

	public static String GenerationEditDialog_titleChangeValidFromDate;

	public static String GenerationEditDialog_pagetitleValidFromDate;

	public static String GenerationEditDialog_labelValidFrom;

	public static String GenerationEditDialog_msgInvalidFormat;

	public static String GenerationEditDialog_msgDateToEarly;

	public static String GenerationEditDialog_msgDateToLate;

}
