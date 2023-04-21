/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    public static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.pctype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String PersistentAssociationInfo_joinColumnName;
    public static String PersistentAssociationInfo_joinTableName;
    public static String PersistentAssociationInfo_msgIsInvalid;
    public static String PersistentAssociationInfo_msgJoinTableNameExceedsMaximumLength;
    public static String PersistentAssociationInfo_msgLazyFetchNotSupported;
    public static String PersistentAssociationInfo_msgMaxLengthExceeds;
    public static String PersistentAssociationInfo_msgMustBeEmpty;
    public static String PersistentAssociationInfo_msgMustNotBeEmpty;
    public static String PersistentAssociationInfo_msgOwningSideManyToManyMarkedOnBothSides;
    public static String PersistentAssociationInfo_msgOwningSideManyToManyNotAllowed;
    public static String PersistentAssociationInfo_msgOwningSideMissing;
    public static String PersistentAssociationInfo_msgTransientMismatch;
    public static String PersistentAssociationInfo_sourceColumnName;
    public static String PersistentAssociationInfo_tagetColumnName;
    public static String PersistentAssociationInfo_msgChildToParentCascadeType;

    public static String PersistentAttributeInfo_msgColumnNameLengthExceedsMaximumLength;
    public static String PersistentAttributeInfo_msgColumnNameMustBeEmpty;
    public static String PersistentAttributeInfo_msgColumnNameMustNotContainWhitespaceCharacters;
    public static String PersistentAttributeInfo_msgColumnPrecisionExceedsTheLimit;
    public static String PersistentAttributeInfo_msgColumnScaleExceedsTheLimit;
    public static String PersistentAttributeInfo_msgColumnSizeExceedsTheLimit;
    public static String PersistentAttributeInfo_msgEmptyColumnName;
    public static String PersistentAttributeInfo_msgColumnSizeNotRestrictedInModel;
    public static String PersistentAttributeInfo_msgModelExceedsColumnSize;
    public static String PersistentAttributeInfo_msgColumnNullableDoesNotMatchModel;
    public static String PersistentInfo_msgIndexNameIsInvalid;

    public static String PersistentTypeInfo_msgDiscriminatorAlreadyDefined;
    public static String PersistentTypeInfo_msgDiscriminatorColumnNameIsInvalid;
    public static String PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedBecauseMappedSuperclass;
    public static String PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedNotRootEntity;
    public static String PersistentTypeInfo_msgDiscriminatorDefinitionNotAllowedTypeNotDefDiscrColumn;
    public static String PersistentTypeInfo_msgDiscriminatorMustBeDefinedInTheRootEntity;
    public static String PersistentTypeInfo_msgDiscriminatorValueMustBeEmpty;
    public static String PersistentTypeInfo_msgDiscriminatorValueMustBeEmptyBecauseMappedSuperclass;
    public static String PersistentTypeInfo_msgDiscriminatorValueMustNotBeEmpty;
    public static String PersistentTypeInfo_msgDiscriminatorValueNotConform;
    public static String PersistentTypeInfo_msgDuplicateColumnName;
    public static String PersistentTypeInfo_msgFoundDuplicateColumnNameIn;
    public static String PersistentTypeInfo_msgInvalidInheritanceStratedyCombination;
    public static String PersistentTypeInfo_msgTableNameExceedsMaximumLength;
    public static String PersistentTypeInfo_msgTableNameInvalid;
    public static String PersistentTypeInfo_msgTableNameMustBeEmptyBecauseNameDefinedInSupertypeShouldBeUsed;
    public static String PersistentTypeInfo_msgTableNameMustBeEmptyMappedSuperclass;
    public static String PersistentTypeInfo_msgTableNameMustBeEmptyNotRootEntityAndInhStrategyIs;
    public static String PersistentTypeInfo_msgTableNameOfRootEntityMustBeUsed;
    public static String PersistentTypeInfo_msgUseTableDefInSupertypIsNotAllowed;
    public static String PersistentTypeInfo_msgDiscriminatorValueTooLong;

    public static String PolicyCmptType_msg_ProductCmptTypeNameMissing;
    public static String PolicyCmptType_msgSubtypeConfigurableWhenSupertypeConfigurable;
    public static String PolicyCmptType_productCmptType;
    public static String PolicyCmptType_TheTypeDoesNotConfigureThisType;
    public static String PolicyCmptType_msgDuplicateRuleName;
    public static String PolicyCmptType_msgInverseDerivedUnionNotSepcified;
    public static String PolicyCmptType_msgRuleMethodNameConflict;
    public static String PolicyCmptType_caption;
    public static String PolicyCmptType_msgDifferentGenerateValidatorClassSetting;

    public static String PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmpty;
    public static String PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmptyIfDerivedUnionHasInverse;
    public static String PolicyCmptTypeAssociation_Association_msg_InverseOfMasterToDetailMustBeADetailToMaster;
    public static String PolicyCmptTypeAssociation_error_MatchingAssociationInvalid;
    public static String PolicyCmptTypeAssociation_error_MatchingAssociationInvalidSourceForConfiguredType;
    public static String PolicyCmptTypeAssociation_error_MatchingAssociationInvalidSourceForNotConfiguredType;
    public static String PolicyCmptTypeAssociation_error_matchingAssociatonNotFound;
    public static String PolicyCmptTypeAssociation_errorMsg_constrainedPropertyQualifiedMismatch;
    public static String PolicyCmptTypeAssociation_InverseOfDetailToMasterMustBeAMasterToDetail;
    public static String PolicyCmptTypeAssociation_sharedAssociation_invalidAssociationHost;
    public static String PolicyCmptTypeAssociation_sharedAssociation_noAssociationHost;

    public static String PolicyCmptTypeAttribute_msg_AbstractCantBeProductRelevant;
    public static String PolicyCmptTypeAttribute_msg_IllegalValueSetType;
    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureDoesNotExists;
    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureHasADifferentDatatype;
    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing;
    public static String PolicyCmptTypeAttribute_msg_ConstantCantBeAbstract;
    public static String PolicyCmptTypeAttribute_msg_defaultValueExtensibleEnumType;
    public static String PolicyCmptTypeAttribute_TypeOfOverwrittenAttributeCantBeChanged;
    public static String PolicyCmptTypeAttribute_OverwrittenAttributeDisabledGenericValidation;

    public static String Association_msg_InverseAssociationInconsistentWithDerivedUnion;
    public static String Association_msg_AssociationNotFoundInTarget;
    public static String Association_msg_InverseAssociationMismatch;
    public static String Association_msg_InverseAssociationMustBeMarkedAsDerivedUnionToo;
    public static String Association_msg_InverseAssociationMustBeOfTypeAssociation;
    public static String Association_msg_DetailToMasterAssociationMustHaveMaxCardinality1;

    public static String ValidationRule_ConstantAttributesCantBeValidated;
    public static String ValidationRule_msg_InvalidMarkerId;
    public static String ValidationRule_msgUndefinedAttribute;
    public static String ValidationRule_msgDuplicateEntries;
    public static String ValidationRule_msgValueSetRule;
    public static String ValidationRule_msgNoNewlineAllowed;
    public static String ValidationRule_msgCodeShouldBeProvided;
    public static String ValidationRuleMessageText_warning_invalidParameter;

    public static String Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
    public static String Attribute_proposalForRuleName;
    public static String Attribute_proposalForMsgCode;
    public static String Attribute_msg_Overwritten_datatype_different;
    public static String MarkerEnumUtil_invalidMarkerEnum;

}
