/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.enums.messages"; //$NON-NLS-1$

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

    public static String EnumAttribute_NameMissing;
    public static String EnumAttribute_NameNotAValidFieldName;
    public static String EnumAttribute_DuplicateName;
    public static String EnumAttribute_DuplicateNameInSupertypeHierarchy;
    public static String EnumAttribute_DatatypeMissing;
    public static String EnumAttribute_DatatypeDoesNotExist;
    public static String EnumAttribute_DatatypeIsPrimitive;
    public static String EnumAttribute_DatatypeIsVoid;
    public static String EnumAttribute_DatatypeIsAbstract;
    public static String EnumAttribute_DatatypeIsContainingEnumTypeOrSubclass;
    public static String EnumAttribute_DuplicateLiteralName;
    public static String EnumAttribute_NoSuchAttributeInSupertypeHierarchy;
    public static String EnumAttribute_InheritedButNoSupertype;
    public static String EnumAttribute_LiteralNameNotOfDatatypeString;
    public static String EnumAttribute_LiteralNameButNotUniqueIdentifier;
    public static String EnumAttribute_DuplicateUsedAsNameInFaktorIpsUi;
    public static String EnumAttribute_DuplicateUsedAsIdInFaktorIpsUi;
    public static String EnumAttribute_EnumDatatypeDoesNotContainValuesButParentEnumTypeDoes;
    public static String EnumAttribute_PropertyDisplayName_Identifier;
    public static String EnumAttribute_PropertyDisplayName_LiteralName;
    public static String EnumAttribute_PropertyDisplayNameDisplayName;

    public static String EnumLiteralNameAttribute_DefaultValueProviderAttributeDoesNotExist;
    public static String EnumLiteralNameAttribute_DefaultValueProviderAttributeNotOfDatatypeString;
    public static String EnumLiteralNameAttribute_DefaultValueProviderAttributeNotUnique;
    public static String EnumLiteralNameAttribute_NotNeeded;

    public static String EnumType_NotInheritedAttributesInSupertypeHierarchySingular;
    public static String EnumType_NotInheritedAttributesInSupertypeHierarchyPlural;
    public static String EnumType_NoLiteralNameAttribute;
    public static String EnumType_NoUsedAsIdInFaktorIpsUiAttribute;
    public static String EnumType_NoUsedAsNameInFaktorIpsUiAttribute;
    public static String EnumType_EnumValuesObsolete;
    public static String EnumType_MultipleLiteralNameAttributes;

    public static String EnumContent_ReferencedEnumAttributesCountInvalid;
    public static String EnumContent_ReferencedEnumAttributeNamesInvalid;
    public static String EnumContent_ReferencedEnumAttributesOrderingInvalid;

}
