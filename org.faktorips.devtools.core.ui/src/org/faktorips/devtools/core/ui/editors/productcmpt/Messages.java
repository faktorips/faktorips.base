/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AttributeValueEditComposite_attributeNotChangingOverTimeDescription;

    public static String AttributeValueEditComposite_MenuItem_openTemplate;

    public static String AttributeValueEditComposite_MenuItem_showPropertyUsage;

    public static String AttributeValueEditComposite_MenuItem_showTemplatePropertyUsage;

    public static String AttributeRelevanceControl_Mandatory;

    public static String AttributeRelevanceControl_Optional;

    public static String AttributeRelevanceControl_Irrelevant;

    public static String DefaultsAndRangesEditDialog_additionalValuesDefinedInModel;

    public static String DefaultsAndRangesEditDialog_valueDefinedInProductCmpt;

    public static String DefaultsAndRangesEditDialog_valueNotContainedInValueSet;

    public static String ComponentPropertiesSection_title;

    public static String ComponentPropertiesSection_labelValidFrom;

    public static String ComponentPropertiesSection_TemplateName;

    public static String ConfigElementEditComposite_valueSet;

    public static String ConfigElementEditComposite_defaultValue;

    public static String ConfigElementEditComposite_AttributeRelevance;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ConfirmBuildProject;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorDurringBuild;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorsInProject;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_Title;

    public static String GenerationPropertiesPage_pageTitle;

    public static String GenerationSelectionDialog_checkboxCanEditRecentGenerations;

    public static String GenerationSelectionDialog_infoGenerationsCouldntChangeInfo;

    public static String GenerationSelectionDialog_infoGenerationsCouldntChange;

    public static String GenerationSelectionDialog_infoNoChangesInCurrentWorkingMode;

    public static String GenerationSelectionDialog_labelShowReadOnlyGeneration;

    public static String GenerationSelectionDialog_title;

    public static String PropertiesPage_relations;

    public static String PropertiesPage_noRelationsDefined;

    public static String PolicyAttributeEditDialog_editLabel;

    public static String PolicyAttributeEditDialog_properties;

    public static String RelationEditDialog_editRelation;

    public static String RelationEditDialog_properties;

    public static String RelationEditDialog_target;

    public static String RelationEditDialog_cardinalityMin;

    public static String RelationEditDialog_cardinalityMax;

    public static String ProductAttributesSection_type;

    public static String ProductCmptEditor_productCmptTitle;

    public static String ProductCmptEditor_productComponent;

    public static String FormulaEditDialog_editFormula;

    public static String FormulaEditDialog_Formula;

    public static String FormulaEditDialog_availableParameters;

    public static String RelationsSection_cardinality;

    public static String RelationsLabelProvider_undefined;

    public static String ProductCmptEditor_templateTitle;

    public static String ProductCmptEditor_title_GenerationMissmatch;

    public static String CardinalityPanel_LabelDefaultCardinality;

    public static String CardinalityPanel_labelOptional;

    public static String CardinalityPanel_labelMandatory;

    public static String CardinalityPanel_labelOther;

    public static String CardinalityPanel_MenuItem_showUsage;

    public static String CardinalityPanel_MenuItem_showTemplateLinkUsage;

    public static String ExpressionProposalProvider_defaultValue;

    public static String MissingResourcePage_msgFileOutOfSync;

    public static String ProductCmptEditor_msgFileOutOfSync;

    public static String SetTemplateDialog_titleNewTemplate;

    public static String SetTemplateDialog_labelNewTemplate;

    public static String SetTemplateDialog_msgTemplateDoesNotExist;

    public static String ProductCmptEditor_msgTypeNotFound;

    public static String GenerationEditDialog_titleChangeValidFromDate;

    public static String GenerationEditDialog_pagetitleValidFromDate;

    public static String GenerationEditDialog_labelValidFrom;

    public static String GenerationEditDialog_msgDateToEarly;

    public static String GenerationEditDialog_msgDateToLate;

    public static String GenerationPropertiesPage_valueGenerationValidToUnlimited;

    public static String GenerationPropertiesPage_openModelDescView;

    public static String GenerationPropertiesPage_fallbackSectionTitle;

    public static String GenerationPropertiesPage_msg_warning_notLatestGeneration;

    public static String GenerationPropertiesPage_hideInheritedValues;

    public static String GenerationPropertiesPage_showAllValues;

    public static String GenerationSelectionDialog_labelCreate;

    public static String GenerationSelectionDialog_labelSwitch;

    public static String GenerationSelectionDialog_description;

    public static String ProductAttributesSection_labelRuntimeId;

    public static String ProductAttributesSection_ButtonLabel_GenerateRuntimeId;

    public static String ProductAttributesSection_labelValidTo;

    public static String FormulaTestInputValuesControl_ButtonLabel_Store;

    public static String FormulaTestInputValuesControl_ButtonLabel_Clear;

    public static String FormulaTestInputValuesControl_DefaultFormulaTestCaseName;

    public static String FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Title;

    public static String FormulaTestInputValuesControl_InfoDialogSuccessfullyStored_Text;

    public static String FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Parameter;

    public static String FormulaTestInputValuesControl_TableFormulaTestInputValues_Column_Value;

    public static String FormulaTestInputValuesControl_Label_Result;

    public static String FormulaEditDialog_GroupLabel_FormulaTestInput;

    public static String FormulaEditDialog_TabText_FormulaTestCases;

    public static String FormulaTestCaseControl_GroupLabel_TestCases;

    public static String FormulaTestCaseControl_Button_ExecuteAll;

    public static String FormulaTestCaseControl_Button_Delete;

    public static String FormulaTestCaseControl_Button_Update;

    public static String FormulaTestCaseControl_Button_Up;

    public static String FormulaTestCaseControl_Button_Down;

    public static String FormulaTestCaseControl_GroupLabel_TestInput;

    public static String FormulaTestCaseControl_TableTestCases_Column_Name;

    public static String FormulaTestCaseControl_TableTestCases_ColumnExpectedResult;

    public static String FormulaTestCaseControl_TableTestCases_Column_ActualResult;

    public static String FormulaTestCaseControl_TestFailureMessage_ExpectedButWas;

    public static String FormulaTestCaseControl_TestError_NoExpectedResultGiven;

    public static String FormulaTestInputValuesControl_Error_ParseExceptionWhenExecutingFormula;

    public static String FormulaTestCaseControl_InformationDialogUpdateInputValues_Title;

    public static String FormulaTestCaseControl_InformationDialogUpdateInputValues_NewValueParams;

    public static String FormulaTestCaseControl_InformationDialogUpdateInputValues_DeletedValueParams;

    public static String FormulaTestCaseControl_InformationDialogUpdateInputValues_TextTop;

    public static String FormulaTestCaseControl_Button_New;

    public static String FormulaTestInputValuesControl_Error_ExecutingFormula;

    public static String FormulaTestCaseControl_ToolTip_BtnUpdate;

    public static String ProductCmptPropertiesPage_pageTitle;

    public static String FormulaTestInputValuesControl_ButtonLabel_Calculate;

    public static String FormulaTestInputValuesControl_Result_ObjectIsNotValid;

    public static String LinkEditDialog_cardinalityDefault;

    public static String LinksSection_filterEmptyAssociations;

    public static String LinksSection_Tooltip_PolicyAssociation;

    public static String LinksSection_Tooltip_ProductAssociation;

    public static String ValidationRuleConfigEditComposite_activated;

    public static String ValidationRuleConfigEditComposite_deactivated;

    public static String ValidationRuleSection_DefaultTitle;

    public static String ProductCmptPropertySection_NoContentToDisplay;

    public static String TemplateLinkPmo_Status_Delete;

    public static String TemplateLinkPmo_Status_Inherited;

    public static String TemplateLinkPmo_Status_NewlyDefined;

    public static String TemplateLinkPmo_Status_Override;

    public static String TemplateLinkPmo_Status_OverrideEqual;

    public static String TemplateValuePmo_Status_Inherited;

    public static String TemplateValuePmo_Status_NewlyDefined;

    public static String TemplateValuePmo_Status_NewlyDefined_withoutParentTemplate;

    public static String TemplateValuePmo_Status_Override;

    public static String TemplateValuePmo_Status_OverrideEqual;

    public static String TemplateValuePmo_Status_Undefined;

    public static String TemplateValuePmo_Status_Undefined_WithoutParentTemplate;

}
