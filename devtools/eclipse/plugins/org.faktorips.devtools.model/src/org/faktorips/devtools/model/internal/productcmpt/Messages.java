/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AttributeValue_AllowedValuesAre;
    public static String AttributeValue_attributeNotFound;
    public static String AttributeValue_msg_validateValueHolder_hint;
    public static String AttributeValue_msg_validateValueHolder_multiValue;
    public static String AttributeValue_msg_validateValueHolder_singleValue;
    public static String AttributeValue_ValueEmptyOrNull;
    public static String AttributeValue_ValueNotAllowed;
    public static String AttributeValue_HiddenAttributeMismatch;
    public static String AttributeValue_MultiValueMustNotBeEmpty;

    public static String ConfiguredValueSet_error_msg_abstractValueSet;
    public static String ConfigElement_msgTypeMismatch;
    public static String ConfigElement_policyCmptTypeNotFound;
    public static String ConfigElement_valueSetIsNotASubset;
    public static String ConfigElement_stringTooLong;
    public static String ConfigElement_msgAttrNotDefined;
    public static String FormulaElement_msgDatatypeMissing;
    public static String ConfigElement_msgUnknownDatatype;
    public static String ConfigElement_msgInvalidDatatype;
    public static String ConfiguredDefault_msgValueNotParsable;
    public static String ConfiguredDefault_msgValueNotInValueset;
    public static String ConfiguredDefault_msgValueIsEmptyString;
    public static String ConfiguredValueSet_msgInvalidAttributeValueset;
    public static String ConfiguredDefault_caption;
    public static String ConfiguredValueSet_caption;
    public static String ConfiguredValueSet_error_msg_mandatoryAttribute;

    public static String Formula_msgExpressionMissing;
    public static String Formula_msgFormulaSignatureMissing;
    public static String Formula_msgWrongReturntype;

    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_AttributeNotFound;
    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DatatypeOfParameterNotFound;
    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DataypeNotFound;

    public static String ProductCmptGeneration_msgNoGenerationInLinkedTargetForEffectiveDate;
    public static String ProductCmptGeneration_msgEffectiveDateInLinkedTargetAfterEffectiveDate;
    public static String ProductCmptGeneration_msgTemplateNotFound;
    public static String ProductCmptGeneration_msgNotEnoughRelations;
    public static String ProductCmptGeneration_msgTooManyRelations;
    public static String ProductCmptGeneration_msgDuplicateTarget;

    public static String ProductCmptRelation_msgNoRelationDefined;
    public static String ProductCmptLink_msgChaningOverTimeMismatch_partOfComponent;
    public static String ProductCmptLink_msgChaningOverTimeMismatch_partOfGeneration;
    public static String ProductCmptLink_msgMaxCardinalityExceedsModelMax;
    public static String ProductCmptLink_msgMinCardinalityExceedsModelMin;
    public static String ProductCmptLink_msgMaxCardinalityExceedsModelMaxQualified;
    public static String ProductCmptRelation_msgInvalidTarget;

    public static String DeepCopyOperation_taskTitle;

    public static String AbstractProductCmptNamingStrategy_msgNoVersionSeparator;
    public static String AbstractProductCmptNamingStrategy_msgIllegalChar;
    public static String AbstractProductCmptNamingStrategy_emptyKindId;

    public static String DateBasedProductCmptNamingStrategy_msgWrongFormat;

    public static String ProductCmpt_msgInvalidTypeHierarchy;

    public static String FormulaTestInputValue_CoreException_WrongIdentifierForParameter;
    public static String FormulaTestInputValue_ValidationMessage_FormulaParameterNotFound;
    public static String FormulaTestInputValue_ValidationMessage_UnsupportedDatatype;

    public static String FormulaTestCase_CoreException_DatatypeNotFoundOrWrongConfigured;
    public static String FormulaTestCase_ValidationMessage_DuplicateFormulaTestCaseName;
    public static String FormulaTestCase_ValidationMessage_MismatchBetweenFormulaInputValuesAndIdentifierInFormula;

    public static String TableContentUsage_msgNoType;
    public static String TableContentUsage_msgUnknownStructureUsage;
    public static String TableContentUsage_msgUnknownTableContent;
    public static String TableContentUsage_msgInvalidTableContent;
    public static String TemplateValueSettings_msgExcludeNotAllowedInProductCmpt;
    public static String TemplateValueSettings_msgNoInheritableValueFound;
    public static String TemplateValueSettings_msgNoInheritableLinkFound;
    public static String TemplateValueSettings_msgNoDeletableLinkFound;

    public static String DefaultRuntimeIdStrategy_msgRuntimeIdNotValid;
    public static String MultiValueHolder_AtLeastOneInvalidValueMessageText;
    public static String MultiValueHolder_DuplicateValueMessageText;

    public static String AttributeValue_NotMultiLingual;
    public static String AttributeValue_MultiLingual;
    public static String ProductCmpt_Error_DifferencesToModel0;
    public static String ProductCmpt_Error_IdsNotUnique;
}
