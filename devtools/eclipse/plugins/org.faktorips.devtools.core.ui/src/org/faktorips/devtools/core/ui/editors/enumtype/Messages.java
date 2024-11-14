/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.enumtype.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumTypeEditor_title;

    public static String EnumTypeGeneralInfoSection_title;
    public static String EnumTypeGeneralInfoSection_linkSuperclass;
    public static String EnumTypeGeneralInfoSection_labelAbstract;
    public static String EnumTypeGeneralInfoSection_labelExtensible;
    public static String EnumTypeGeneralInfoSection_labelEnumContentPackageFragment;
    public static String EnumTypeGeneralInfoSection_IdentifierBoundary;
    public static String EnumTypeGeneralInfoSection_IdentifierBoundaryTooltipText;

    public static String EnumTypeStructurePage_title;
    public static String EnumTypeStructurePage_andLiteral;

    public static String EnumTypeValuesPage_title;

    public static String EnumAttributesSection_title;
    public static String EnumAttributessection_buttonInherit;

    public static String EnumAttributeEditDialog_title;
    public static String EnumAttributeEditDialog_generalTitle;
    public static String EnumAttributeEditDialog_labelName;
    public static String EnumAttributeEditDialog_labelDatatype;
    public static String EnumAttributeEditDialog_labelUseAsLiteralName;
    public static String EnumAttributeEditDialog_labelUnique;
    public static String EnumAttributeEditDialog_labelMultilingual;
    public static String EnumAttributeEditDialog_hintMultilingual;
    public static String EnumAttributeEditDialog_labelIdentifier;
    public static String EnumAttributeEditDialog_labelDisplayName;
    public static String EnumAttributeEditDialog_labelIsInherited;
    public static String EnumAttributeEditDialog_labelMandatory;
    public static String EnumAttributeEditDialog_labelDefaultValueProviderAttribute;
    public static String EnumAttributeEditDialog_EnumAttribute_EnumDatatypeExtensibleShowHint;

    public static String EnumAttributeEditDialog_mismatchMultilingual;
    public static String EnumAttributeEditDialog_mismatchNoMultilingual;

    public static String InheritAttributesDialog_title;
    public static String InheritAttributesDialog_labelNoAttributes;
    public static String InheritAttributesDialog_labelSelectAttribute;

}
