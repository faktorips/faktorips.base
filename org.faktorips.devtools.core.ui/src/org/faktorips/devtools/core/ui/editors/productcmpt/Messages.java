/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    public static String DefaultsAndRangesEditDialog_additionalValuesDefinedInModel;

    public static String DefaultsAndRangesEditDialog_valueDefinedInProductCmpt;

    public static String DefaultsAndRangesEditDialog_valueNotContainedInValueSet;

    public static String DefaultsAndRangesSection_minMaxStepLabel;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ConfirmBuildProject;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorDurringBuild;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_ErrorsInProject;

    public static String FormulaTestInputValuesControl_PreconditionDialogExecuteFormula_Title;

    public static String GenerationSelectionDialog_checkboxCanEditRecentGenerations;

    public static String GenerationSelectionDialog_infoGenerationsCouldntChangeInfo;

    public static String GenerationSelectionDialog_infoGenerationsCouldntChange;

    public static String GenerationSelectionDialog_infoNoChangesInCurrentWorkingMode;

    public static String GenerationSelectionDialog_labelShowReadOnlyGeneration;

    public static String GenerationSelectionDialog_title;

    public static String GenerationsSection_validFrom;

    public static String GenerationsSection_validFromInPast;

    public static String ProductCmptCompareItem_AllValues;

    public static String ProductCmptCompareItem_Attribute;

    public static String ProductCmptCompareItem_Formula;

    public static String ProductCmptCompareItem_Relation;

    public static String ProductCmptCompareItem_RelationCardinalityOther_maximum;

    public static String ProductCmptCompareItem_RelationCardinalityOther_minimum;

    public static String ProductCmptCompareItem_SourceFile;

    public static String ProductCmptCompareItem_unlimited;

    public static String ProductCmptCompareItem_ValueSet;

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

    public static String ProductAttributesSection_template;

    public static String ProductCmptEditor_productComponent;

    public static String FormulaEditDialog_editFormula;

    public static String FormulaEditDialog_Formula;

    public static String FormulaEditDialog_availableParameters;

    public static String FormulasSection_calculationFormulas;

    public static String PolicyAttributesSection_defaultsAndRanges;

    public static String PolicyAttributesSection_valueSet;

    public static String PolicyAttributesSection_minimum;

    public static String PolicyAttributesSection_maximum;

    public static String PolicyAttributesSection_step;

    public static String ProductAttributesSection_attribute;

    public static String RelationsSection_cardinality;

    public static String PolicyAttributesSection_noDefaultsAndRangesDefined;

    public static String RelationsLabelProvider_undefined;

    public static String FormulasSection_noFormulasDefined;

    public static String ProductCmptEditor_title_GenerationMissmatch;

    public static String GenerationsSection_titleShowGeneration;

    public static String GenerationsSection_msgShowGeneration;

    public static String GenerationsSection_buttonKeepEffectiveDate;

    public static String GenerationsSection_buttonChangeEffectiveDate;

    public static String GenerationsSection_buttonCancel;

    public static String ProductAttributesSection_valueGenerationValidToUnlimited;

    public static String RulesSection_title;

    public static String CardinalityPanel_labelOptional;

    public static String CardinalityPanel_labelMandatory;

    public static String CardinalityPanel_labelOther;

    public static String MissingResourcePage_msgFileOutOfSync;

    public static String ProductCmptEditor_msgFileOutOfSync;

    public static String SetTemplateDialog_titleNewTemplate;

    public static String SetTemplateDialog_labelNewTemplate;

    public static String SetTemplateDialog_msgTemplateDoesNotExist;

    public static String ProductCmptEditor_msgTemplateNotFound;

    public static String GenerationEditDialog_titleChangeValidFromDate;

    public static String GenerationEditDialog_pagetitleValidFromDate;

    public static String GenerationEditDialog_labelValidFrom;

    public static String GenerationEditDialog_msgInvalidFormat;

    public static String GenerationEditDialog_msgDateToEarly;

    public static String GenerationEditDialog_msgDateToLate;

    public static String GenerationPropertiesPage_openModelDescView;

    public static String GenerationSelectionDialog_labelCreate;

    public static String GenerationSelectionDialog_labelSwitch;

    public static String GenerationSelectionDialog_description;

    public static String ProductAttributesSection_labelRuntimeId;

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

    public static String ProductAttributesSection_msgInvalidDate;

    public static String RelationsSection_ContextMenu_Properties;

    public static String RelationsSection_ContextMenu_OpenInNewEditor;

}
