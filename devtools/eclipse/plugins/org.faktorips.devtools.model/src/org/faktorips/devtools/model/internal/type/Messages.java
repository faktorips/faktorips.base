/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.type.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String Association_msg_MaxCardinalityMustBeAtLeast1;
    public static String Association_msg_MaxCardinalityForDerivedUnionTooLow;
    public static String Association_msg_MinCardinalityGreaterThanMaxCardinality;
    public static String Association_msg_TargetRolePlural;
    public static String Association_msg_TargetRoleSingular;
    public static String Association_msg_TargetRoleSingularNotAValidJavaFieldName;
    public static String Association_msg_TargetRolePluralNotAValidJavaFieldName;
    public static String Association_msg_DerivedUnionDoesNotExist;
    public static String Association_msg_NotMarkedAsDerivedUnion;
    public static String Association_msg_TargetOfDerivedUnionDoesNotExist;
    public static String Association_msg_TargetNotSubclass;
    public static String Association_msgDerivedUnionNotSubset;
    public static String Association_msgSubsetOfDerivedUnionSameMaxCardinality;
    public static String Association_msg_ConstrainedAssociationSingularDoesNotExist;
    public static String Association_msg_ConstrainedAssociationPluralDoesNotExist;
    public static String Association_msg_ConstraintIsSubsetOfDerivedUnion;
    public static String Association_msg_ConstraintIsDerivedUnion;
    public static String Association_msg_ConstrainedIsSubsetOfDerivedUnion;
    public static String Association_msg_ConstrainedIsDerivedUnion;
    public static String Association_msg_ConstrainedTargetNoSuperclass;
    public static String Association_msg_ConstrainedInvalidMatchingAssociation;
    public static String Association_msg_MaxCardinalityForConstrainMustAllowMultipleItems;
    public static String Association_msg_MaxCardinalityForConstrainLowerThanSuperAssociation;
    public static String Association_msg_MinCardinalityForConstrainHigherThanSuperAssociation;
    public static String Association_msg_AssociationTypeNotEqualToSuperAssociation;

    public static String Attribute_msg_InvalidAttributeName;
    public static String Attribute_msg_DefaultNotParsable_UnknownDatatype;
    public static String Attribute_msg_DefaultValueIsEmptyString;
    public static String Attribute_msg_ValueTypeMismatch;
    public static String Attribute_msg_DefaultNotInValueset;
    public static String Attribute_msgNothingToOverwrite;
    public static String Attribute_ValueSet_not_SubValueSet_of_the_overridden_attribute;
    public static String Attribute_msg_Overwritten_modifier_different;
    public static String Attribute_msg_Overwritten_type_incompatible;
    public static String Attribute_msgOverwritten_ChangingOverTimeAttribute_different;

    public static String DuplicatePropertyNameValidator_msg;
    public static String DuplicatePropertyNameValidator_msg_DifferentElementsSameType;
    public static String DuplicatePropertyNameValidator_msg_DifferentElementsAndITypes;
    public static String DuplicatePropertyNameValidator_PluralAttribute;
    public static String DuplicatePropertyNameValidator_SingularAttribute;
    public static String DuplicatePropertyNameValidator_PluralAssociation;
    public static String DuplicatePropertyNameValidator_SingularAssociation;
    public static String DuplicatePropertyNameValidator_PluralMethod;
    public static String DuplicatePropertyNameValidator_SingularMethod;
    public static String DuplicatePropertyNameValidator_PluralElement;
    public static String DuplicatePropertyNameValidator_SingularElement;
    public static String DuplicatePropertyNameValidator_ProductCmptTypeItself;

    public static String AttributeAbstractDatatypeValidator_msg;
    public static String AttributeAbstractDatatypeValidator_hint;

    public static String TypeMethod_duplicateSignature;
    public static String TypeMethod_incompatbileReturnType;
    public static String TypeMethod_msg_abstractMethodError;
    public static String TypeMethod_msg_modifierOverriddenNotEqual;

    public static String Type_msg_AbstractMissmatch;
    public static String Type_msg_cycleInTypeHierarchy;
    public static String Type_msg_MustOverrideAbstractMethod;
    public static String Type_msg_MustImplementDerivedUnion;
    public static String Type_msg_MustImplementInverseDerivedUnion;
    public static String Type_msg_supertypeNotFound;
    public static String Type_msg_TypeHierarchyInconsistent;
    public static String Type_msgOtherTypeWithSameQNameInDependentProject;
    public static String Type_msgOtherTypeWithSameQNameInSameProject;

}
