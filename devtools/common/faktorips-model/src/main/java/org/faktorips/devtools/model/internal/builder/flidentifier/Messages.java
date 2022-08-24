/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.builder.flidentifier.messages"; //$NON-NLS-1$

    public static String AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved;
    public static String AbstractParameterIdentifierResolver_msgErrorDatatypeResolving;
    public static String AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute;
    public static String AbstractParameterIdentifierResolver_noAssociationTarget;
    public static String AbstractParameterIdentifierResolver_noIndexFor1to1Association0;
    public static String AbstractParameterIdentifierResolver_msgIdentifierNotAllowed;

    public static String AssociationParser_msgErrorAssociationQualifierOrIndex;
    public static String AssociationParser_msgErrorAssociationQualifier;
    public static String AssociationParser_ListDatatypeDescriptionPrefix;

    public static String EnumParser_msgErrorInvalidEnumValue;
    public static String EnumParser_description;

    public static String IdentifierParser_msgErrorInvalidIdentifier;

    public static String QualifierAndIndexParser_errorMsg_errorWhileSearchingProductCmpt;

    public static String QualifierAndIndexParser_descriptionIndex;
    public static String QualifierAndIndexParser_descriptionQualifier;
    public static String QualifierAndIndexParser_descriptionQualifierUndefined;

    public static String ParameterParser_description;

    public static String AttributeParser_defaultOfName;

    static {
        // initialize resource bundle
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
