/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TableContentsEnumDatatypeAdapter_1;
    public static String TableContentsEnumDatatypeAdapter_3;

    public static String ValidationUtils_msgObjectDoesNotExist;
    public static String ValidationUtils_msgDatatypeDoesNotExist;
    public static String ValidationUtils_msgVoidNotAllowed;
    public static String ValidationUtils_msgPropertyMissing;
    public static String ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND;
    public static String ValidationUtils_VALUEDATATYPE_INVALID;
    public static String ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE;

}
