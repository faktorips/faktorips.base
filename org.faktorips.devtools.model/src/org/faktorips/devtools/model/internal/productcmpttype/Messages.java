/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    public static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.productcmpttype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductCmptType_DuplicateFormulaName;
    public static String ProductCmptType_iconFileCannotBeResolved;
    public static String ProductCmptType_InconsistentTypeHierarchies;
    public static String ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;
    public static String ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden;
    public static String ProductCmptType_msgProductCmptTypeAbstractWhenPolicyCmptTypeAbstract;
    public static String ProductCmptType_multiplePropertyNames;
    public static String ProductCmptType_MustInheritFromASupertype;
    public static String ProductCmptType_notMarkedAsConfigurable;
    public static String ProductCmptType_PolicyCmptTypeDoesNotExist;
    public static String ProductCmptType_policyCmptTypeDoesNotSpecifyThisType;
    public static String ProductCmptType_policyCmptTypeNotValid;
    public static String ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;
    public static String ProductCmptType_caption;
    public static String ProductCmptType_pluralCaption;
    public static String ProductCmptType_error_supertypeNotMarkedAsLayerSupertype;
    public static String ProductCmptType_error_settingChangingOverTimeDiffersFromSettingInSupertype;

    public static String ProductCmptTypeAssociation_error_MatchingAssociationDoesNotReferenceThis;
    public static String ProductCmptTypeAssociation_error_MatchingAssociationDuplicateName;
    public static String ProductCmptTypeAssociation_error_MatchingAssociationInvalid;
    public static String ProductCmptTypeAssociation_error_matchingAssociationNotFound;
    public static String ProductCmptTypeAssociation_errorMsg_constrained_changeOverTime_missmatch;
    public static String ProductCmptTypeAssociation_Msg_DeriveUnionChangingOverTimeMismatch_SubetChanging;
    public static String ProductCmptTypeAssociation_Msg_DeriveUnionChangingOverTimeMismatch_SubetStatic;

    public static String ProductCmptTypeMethod_FormulaNameIsMissing;
    public static String ProductCmptTypeMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
    public static String ProductCmptTypeMethod_FormulaSignatureMustntBeAbstract;
    public static String ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy;
    public static String ProductCmptTypeMethod_msgOptionalNotAllowedBecauseNotOptionalInSupertypeHierarchy;
    public static String ProductCmptTypeMethod_msgChangingOverTimeNotAllowedBecauseNotChangingOverTimeInSupertypeHierarchy;
    public static String ProductCmptTypeMethod_msgNotChangingOverTimeNotAllowedBecauseChangingOverTimeInSupertypeHierarchy;

    public static String TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;
    public static String TableStructureUsage_msgInvalidRoleName;
    public static String TableStructureUsage_msgRoleNameAlreadyInSupertype;
    public static String TableStructureUsage_msgTableStructureNotExists;
    public static String TableStructureUsage_msg_Singular;
    public static String TableStructureUsage_msg_Plural;

    public static String ProductCmptTypeMethod_Formula_msg_Plural;
    public static String ProductCmptTypeMethod_Formula_msg_Singular;
    public static String ProductCmptCategory_msgNameIsEmpty;
    public static String ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy;
    public static String ProductCmptCategory_msgInheritedButNotFoundInSupertypeHierarchy;
    public static String ProductCmptCategory_msgInheritedButNoSupertype;
    public static String ProductCmptCategory_DuplicateDefaultsForFormulaSignatureDefinitions;
    public static String ProductCmptCategory_DuplicateDefaultsForValidationRules;
    public static String ProductCmptCategory_DuplicateDefaultsForTableStructureUsages;
    public static String ProductCmptCategory_DuplicateDefaultsForPolicyCmptTypeAttributes;
    public static String ProductCmptCategory_DuplicateDefaultsForProductCmptTypeAttributes;
    public static String ProductCmptCategory_NoDefaultForFormulaSignatureDefinitions;
    public static String ProductCmptCategory_NoDefaultForValidationRules;
    public static String ProductCmptCategory_NoDefaultForTableStructureUsages;
    public static String ProductCmptCategory_NoDefaultForPolicyCmptTypeAttributes;
    public static String ProductCmptCategory_NoDefaultForProductCmptTypeAttributes;

    public static String ProductCmptPropertyExternalReference_msgReferencedPropertyCouldNotBeFound;

    public static String ProductCmptPropertyValidator_msgTypeDoesNotAcceptChangingOverTime;

    public static String ProductCmptTypeAttribute_msg_invalidValueSet;
    public static String ProductCmptTypeAttribute_msgOverwritten_singleValueMultipleValuesDifference;
    public static String ProductCmptTypeAttribute_msgOverwritten_multilingual_different;
    public static String ProductCmptTypeAttribute_msgDefaultValueNotInValueSetWhileHidden;

}
