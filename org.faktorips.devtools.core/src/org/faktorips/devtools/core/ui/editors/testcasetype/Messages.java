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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.osgi.util.NLS;

/**
 * @author Joerg Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcasetype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String TestCaseTypeEditor_PageName;
    public static String TestCaseTypeEditor_SectionTitle_Structure;
    public static String TestCaseTypeEditor_SectionTitle_Details;
    public static String TestCaseTypeEditor_EditorTitle;
    public static String TestCaseTypeLabelProvider_Undefined;
    public static String TestCaseTypeSection_Button_AddAttribute;
    public static String TestCaseTypeSection_Button_RemoveAttribute;
    public static String TestCaseTypeSection_Button_MoveAttributeUp;
    public static String TestCaseTypeSection_Button_MoveAttributeDown;
    public static String TestCaseTypeSection_Action_ShowAll_ToolTip;
    public static String TestCaseTypeSection_Error_UnexpectedObjectClass;
    public static String TestCaseTypeSection_Dialog_SelectAttributeAdd_Message;
    public static String TestCaseTypeSection_EditFieldLabel_Datatype;
    public static String TestCaseTypeSection_EditFieldLabel_MinInstances;
    public static String TestCaseTypeSection_EditFieldLabel_MaxInstances;
    public static String TestCaseTypeSection_EditFieldLabel_RequiresProduct;
    public static String TestCaseTypeSection_EditFieldLabel_Name;
    public static String TestCaseTypeSection_EditFieldLabel_TestParameterType;
    public static String TestCaseTypeSection_Error_WrongTestAttributeIndex;
    public static String TestCaseTypeSection_Button_Remove;
    public static String TestCaseTypeSection_Dialog_SelectAttribute_Title;
    public static String RelationRefControl_Title;
    public static String RelationRefControl_Description;
    public static String NewChildParameterWizard_Title;
    public static String NewRootParameterWizard_Title;
    public static String NewChildParamWizardPage_Title;
    public static String NewChildParamWizardPage_Description;
    public static String NewChildParamWizardPage_Label_Relation;
    public static String NewChildParamWizardPage_Label_Target;
    public static String NewChildParamWizardPage_Error_RelationDoesNotExists;
    public static String NewTestParamDetailWizardPage_Title;
    public static String NewTestParamDetailWizardPage_Description;
    public static String NewRootParamWizardPage_Label_Datatype;
    public static String NewRootParamWizardPage_Error_DatatypeDoesNotExists;
    public static String TestCaseTypeSection_Button_NewRootParameter;
    public static String TestCaseTypeSection_AttributeTable_ColumnTitleAttributeName;
    public static String TestCaseTypeSection_AttributeTable_ColumnTitleAttribute;
    public static String TestCaseTypeSection_AttributeTable_ColumnTitleAttributeType;
    public static String TestCaseTypeSection_ErrorDialog_AddParameterPcTypeIsMissing;
    public static String TestCaseTypeSection_ErrorDialog_AddParameterTitle;
    public static String TestCaseTypeSection_ErrorDialog_AttributeChangingNotAllowedBecausePolicyCmptTypeNotExists;
    public static String TestCaseTypeSection_ErrorDialog_AttributeChangingTitle;
    public static String RelationRefControl_Button_Browse;
    public static String TestCaseTypeTreeRootElement_RootElement_Text;
    public static String NewChildParamWizardPage_ValidationError_InvalidTestParameterName;
    public static String NewRootParamWizardPage_ValidationError_InvalidTestParameterName;
    public static String TestCaseTypeSection_Button_Up;
    public static String TestCaseTypeSection_Button_Down;
    public static String NewRootParamFirstWizardPage_Title;
    public static String NewRootParamFirstWizardPage_Decription;
    public static String NewRootParamFirstWizardPage_RadioButton_TestPolicyCmptTypeParameter;
    public static String NewRootParamFirstWizardPage_RadioButton_TestValueParameter;
    public static String NewRootParamFirstWizardPage_RadioButton_TestRuleParameter;
    public static String NewRootParamFirstWizardPage_GroupLabel;
    public static String NewRootParamWizardPage_Title_TestPolicyCmptParam;
    public static String NewRootParamWizardPage_Description_TestPolicyCmptParam;
    public static String NewRootParamWizardPage_Title_TestValueParam;
    public static String NewRootParamWizardPage_Description_TestValueParam;
    public static String NewRootParamWizardPage_Title_TestRuleParam;
    public static String NewRootParamWizardPage_Description_TestRuleParam;
    
}
