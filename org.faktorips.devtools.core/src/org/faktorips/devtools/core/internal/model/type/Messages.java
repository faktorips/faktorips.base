/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.type.messages"; //$NON-NLS-1$

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
    public static String Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
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

    public static String Attribute_msg_InvalidAttributeName;
    public static String Attribute_msg_DefaultNotParsable_UnknownDatatype;
    public static String Attribute_msg_DefaultValueIsEmptyString;
    public static String Attribute_msg_ValueTypeMismatch;
    public static String Attribute_msg_DefaultNotInValueset;
    public static String Attribute_msgNothingToOverwrite;
    public static String Attribute_ValueSet_not_SubValueSet_of_the_overridden_attribute;
    public static String Attribute_msg_Overwritten_datatype_different;
    public static String Attribute_msg_Overwritten_modifier_different;
    public static String DuplicatePropertyNameValidator_msg;

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
