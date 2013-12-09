/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder.flidentifier;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.builder.flidentifier.messages"; //$NON-NLS-1$

    public static String AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved;
    public static String AbstractParameterIdentifierResolver_msgErrorDatatypeResolving;
    public static String AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute;
    public static String AbstractParameterIdentifierResolver_noAssociationTarget;
    public static String AbstractParameterIdentifierResolver_noIndexFor1to1Association0;
    public static String AbstractParameterIdentifierResolver_msgIdentifierNotAllowed;

    public static String AssociationParser_msgErrorAssociationQualifierOrIndex;
    public static String AssociationParser_msgErrorWhileFindAssociation;
    public static String AssociationParser_msgErrorAssociationQualifier;

    public static String EnumParser_msgErrorInvalidEnumValue;
    public static String IdentifierParser_msgErrorInvalidIdentifier;

    public static String QualifierAndIndexParser_errorMsg_errorWhileSearchingProductCmpt;

    public static String QualifierAndIndexParser_errorMsg_qualifierMustFollowAssociation;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}