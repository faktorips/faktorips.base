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

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.osgi.util.NLS;

/*
 * 
 * @author Alexander Weickmann
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.enums.messages"; //$NON-NLS-1$

    public static String EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes;

    public static String EnumAttributeValue_ValueNotParsable;
    public static String EnumAttributeValue_UniqueIdentifierValueEmpty;
    public static String EnumAttributeValue_UniqueIdentifierValueNotUnique;
    public static String EnumAttributeValue_UniqueIdentifierValueNotJavaConform;

    public static String EnumAttribute_NameMissing;
    public static String EnumAttribute_DuplicateName;
    public static String EnumAttribute_DatatypeMissing;
    public static String EnumAttribute_DatatypeDoesNotExist;
    public static String EnumAttribute_DuplicateIdentifier;
    public static String EnumAttribute_NoSuchAttributeInSupertypeHierarchy;
    public static String EnumAttribute_IdentifierNotOfDatatypeString;
    public static String EnumAttribute_LiteralNameButNotUniqueIdentifier;
    public static String EnumAttribute_Identifier;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
