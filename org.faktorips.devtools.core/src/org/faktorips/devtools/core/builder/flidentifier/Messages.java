/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
