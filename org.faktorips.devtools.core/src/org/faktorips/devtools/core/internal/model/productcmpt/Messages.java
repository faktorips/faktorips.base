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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AttributeValue_AllowedValuesAre;
    public static String AttributeValue_attributeNotFound;
    public static String AttributeValue_ValueNotAllowed;

    public static String ConfigElement_msgTypeMismatch;
    public static String ConfigElement_policyCmptTypeNotFound;
    public static String ConfigElement_valueIsNotInTheValueSetDefinedInTheModel;
    public static String ConfigElement_valueSetIsNotASubset;
    public static String ConfigElement_msgAttrNotDefined;
    public static String ConfigElement_msgDatatypeMissing;
    public static String ConfigElement_msgUndknownDatatype;
    public static String ConfigElement_msgInvalidDatatype;
    public static String ConfigElement_msgValueNotParsable;
    public static String ConfigElement_msgValueNotInValueset;
    public static String ConfigElement_msgValueIsEmptyString;
    public static String ConfigElement_msgInvalidAttributeValueset;

    public static String Formula_msgExpressionMissing;
    public static String Formula_msgFormulaSignatureMissing;
    public static String Formula_msgWrongReturntype;

    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_AttributeNotFound;
    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DatatypeOfParameterNotFound;
    public static String FormulaTestInputValue_FormulaTestInputValue_ValidationMessage_DataypeNotFound;

    public static String ProductCmptGeneration_msgAttributeWithMissingConfigElement;
    public static String ProductCmptGeneration_msgNoGenerationInLinkedTargetForEffectiveDate;
    public static String ProductCmptGeneration_msgTemplateNotFound;
    public static String ProductCmptGeneration_msgNotEnoughRelations;
    public static String ProductCmptGeneration_msgTooManyRelations;
    public static String ProductCmptGeneration_msgDuplicateTarget;

    public static String TableAccessFunctionFlFunctionAdapter_msgNoTableAccess;
    public static String TableAccessFunctionFlFunctionAdapter_msgErrorDuringCodeGeneration;

    public static String ProductCmptRelation_msgNoRelationDefined;
    public static String ProductCmptRelation_msgMaxCardinalityIsLessThan1;
    public static String ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
    public static String ProductCmptRelation_msgMaxCardinalityExceedsModelMax;
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

    public static String DefaultRuntimeIdStrategy_msgRuntimeIdNotValid;

}
