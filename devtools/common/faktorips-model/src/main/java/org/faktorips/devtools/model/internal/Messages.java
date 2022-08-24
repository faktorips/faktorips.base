/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String CustomValidationsMap_MsgCannotAddValidation_classesDoNotMatch;
    public static String DefaultVersionProvider_readableVersionFormat;
    public static String TableContentsEnumDatatypeAdapter_1;
    public static String TableContentsEnumDatatypeAdapter_3;

    public static String ValidationUtils_msgObjectDoesNotExist;
    public static String ValidationUtils_msgDatatypeDoesNotExist;
    public static String ValidationUtils_msgVoidNotAllowed;
    public static String ValidationUtils_msgPropertyMissing;
    public static String ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND;
    public static String ValidationUtils_VALUEDATATYPE_INVALID;
    public static String ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE;
    public static String ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE_MONEY;
    public static String ValueSetNullIncompatibleValidator_Msg_NullNotAllowed;

}
