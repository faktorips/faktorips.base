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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.pctype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String PctEditor_title;
	public static String RuleEditDialog_title;
	public static String RuleEditDialog_messageTitle;
	public static String RuleEditDialog_functionTitle;
	public static String RuleEditDialog_attrTitle;
	public static String RuleEditDialog_labelName;
	public static String RuleEditDialog_messageGroupTitle;
	public static String RuleEditDialog_labelCode;
	public static String RuleEditDialog_labelSeverity;
	public static String RuleEditDialog_labelText;
	public static String RuleEditDialog_labelApplyInAllBusinessFunctions;
	public static String RuleEditDialog_labelSpecifiedInSrc;
	public static String BehaviourPage_title;
	public static String OverrideMethodDialog_title;
	public static String OverrideMethodDialog_msgEmpty;
	public static String GeneralInfoSection_title;
	public static String GeneralInfoSection_linkSuperclass;
	public static String GeneralInfoSection_labelAbstractClass;
	public static String GeneralInfoSection_labelProduct;
	public static String GeneralInfoSection_labelType;
	public static String MethodsSection_title;
	public static String MethodsSection_button;
	public static String ValidatedAttributesControl_description;
	public static String ValidatedAttributesControl_label;
	public static String AttributesSection_title;
	public static String AttributesSection_deleteMessage;
	public static String AttributesSection_deleteTitle;
	public static String AttributeEditDialog_title;
	public static String AttributeEditDialog_generalTitle;
	public static String AttributeEditDialog_valuesetTitle;
	public static String AttributeEditDialog_calcParamsTitle;
	public static String AttributeEditDialog_validationRuleTitle;
	public static String AttributeEditDialog_labelDatatype;
	public static String AttributeEditDialog_labelModifier;
	public static String AttributeEditDialog_labelAttrType;
	public static String AttributeEditDialog_labelProdRelevant;
	public static String AttributeEditDialog_labelDefaultValue;
	public static String AttributeEditDialog_labelParams;
	public static String AttributeEditDialog_labelActivateValidationRule;
	public static String AttributeEditDialog_tooltipActivateValidationRule;
	public static String AttributeEditDialog_ruleTitle;
	public static String AttributeEditDialog_labelName;
	public static String AttributeEditDialog_suggestedNamePrefix;
	public static String AttributeEditDialog_messageTitle;
	public static String AttributeEditDialog_labelCode;
	public static String AttributeEditDialog_labelSeverity;
	public static String AttributeEditDialog_labelText;
	public static String AttributeEditDialog_descriptionContent;
	public static String StructurePage_title;
	public static String RuleFunctionsControl_title;
	public static String RuleFunctionsControl_titleColum1;
	public static String RuleFunctionsControl_titleColumn2;
	public static String RulesSection_title;
	public static String RulesSection_msgMissingAttribute;
	public static String RulesSection_titleMissingAttribute;
	public static String MethodEditDialog_title;
	public static String MethodEditDialog_signatureTitle;
	public static String MethodEditDialog_implementationTitle;
	public static String MethodEditDialog_labelAccesModifier;
	public static String MethodEditDialog_labelAbstract;
	public static String MethodEditDialog_labelType;
	public static String MethodEditDialog_labelName;
	public static String MethodEditDialog_labelParameters;
	public static String RelationsSection_title;
	public static String RelationEditDialog_title;
	public static String RelationEditDialog_propertiesTitle;
	public static String RelationEditDialog_productSideTitle;
	public static String RelationEditDialog_labelType;
	public static String RelationEditDialog_labelReadOnlyContainer;
	public static String RelationEditDialog_labelTarget;
	public static String RelationEditDialog_labelTargetRoleSingular;
	public static String RelationEditDialog_labelTargetRolePlural;
	public static String RelationEditDialog_labelMinCardinality;
	public static String RelationEditDialog_labelMaxCardinality;
	public static String RelationEditDialog_labelProdRelevant;
	public static String RelationEditDialog_labelReverseRel;
	public static String RelationEditDialog_labelContainerRel;
}
