/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.enums.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes;

    public static String EnumAttributeValue_ValueNotParsable;
    public static String EnumAttributeValue_UniqueIdentifierValueEmpty;
    public static String EnumAttributeValue_UniqueIdentifierValueNotUnique;

    public static String EnumLiteralNameAttributeValue_ValueIsNotAValidJavaIdentifier;

    public static String EnumAttribute_NameMissing;
    public static String EnumAttribute_NameNotAValidFieldName;
    public static String EnumAttribute_DuplicateName;
    public static String EnumAttribute_DuplicateNameInSupertypeHierarchy;
    public static String EnumAttribute_DatatypeMissing;
    public static String EnumAttribute_DatatypeDoesNotExist;
    public static String EnumAttribute_DatatypeIsVoid;
    public static String EnumAttribute_DatatypeIsAbstract;
    public static String EnumAttribute_DatatypeIsContainingEnumTypeOrSubclass;
    public static String EnumAttribute_DuplicateLiteralName;
    public static String EnumAttribute_NoSuchAttributeInSupertypeHierarchy;
    public static String EnumAttribute_InheritedButNoSupertype;
    public static String EnumAttribute_InheritedMultiLingualMismatch;
    public static String EnumAttribute_MultilingualCannotBeIdentifier;
    public static String EnumAttribute_LiteralNameNotOfDatatypeString;
    public static String EnumAttribute_LiteralNameButNotUniqueIdentifier;
    public static String EnumAttribute_DuplicateUsedAsNameInFaktorIpsUi;
    public static String EnumAttribute_DuplicateUsedAsIdInFaktorIpsUi;
    public static String EnumAttribute_EnumDatatypeDoesNotContainValuesButParentEnumTypeDoes;
    public static String EnumAttribute_PropertyDisplayName_Identifier;
    public static String EnumAttribute_PropertyDisplayName_LiteralName;
    public static String EnumAttribute_PropertyDisplayNameDisplayName;
    public static String EnumAttribute_MandatoryValueNotSet;

    public static String EnumLiteralNameAttribute_DefaultValueProviderAttributeDoesNotExist;
    public static String EnumLiteralNameAttribute_DefaultValueProviderAttributeNotOfDatatypeString;
    public static String EnumLiteralNameAttribute_NotNeeded;

    public static String EnumType_NotInheritedAttributesInSupertypeHierarchySingular;
    public static String EnumType_NotInheritedAttributesInSupertypeHierarchyPlural;
    public static String EnumType_NoLiteralNameAttribute;
    public static String EnumType_NoUsedAsIdInFaktorIpsUiAttribute;
    public static String EnumType_NoUsedAsNameInFaktorIpsUiAttribute;
    public static String EnumType_EnumValuesObsolete;
    public static String EnumType_MultipleLiteralNameAttributes;
    public static String EnumType_EnumContentAlreadyUsedByAnotherEnumType;

    public static String EnumContent_ReferencedEnumAttributesCountInvalid;
    public static String EnumContent_ReferencedEnumAttributeNamesInvalid;
    public static String EnumContent_ReferencedEnumAttributesOrderingInvalid;

    public static String EnumAttributeValue_Msg_IdNotAllowedByIdentifierBoundary_valueOfEnumType;
    public static String EnumAttributeValue_Msg_IdNotAllowedByIdentifierBoundary_valueOfEnumContent;

    public static String EnumAttributeValue_MultiLingual;
    public static String EnumAttributeValue_NotMultiLingual;
}
