/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.osgi.util.NLS;

/*
 * 
 * @author Alexander Weickmann
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.enumtype.messages"; //$NON-NLS-1$

    public static String EnumTypeEditor_title;

    public static String EnumTypeGeneralInfoSection_title;
    public static String EnumTypeGeneralInfoSection_linkSuperclass;
    public static String EnumTypeGeneralInfoSection_labelAbstract;
    public static String EnumTypeGeneralInfoSection_labelContainingValues;
    public static String EnumTypeGeneralInfoSection_labelEnumContentPackageFragment;

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
    public static String EnumAttributeEditDialog_labelIdentifier;
    public static String EnumAttributeEditDialog_labelDisplayName;
    public static String EnumAttributeEditDialog_labelIsInherited;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
