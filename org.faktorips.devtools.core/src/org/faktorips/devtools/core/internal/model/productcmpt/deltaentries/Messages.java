/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String LinkChangingOverTimeMismatchEntry_Description_GenToProdCmpt;

    public static String LinkChangingOverTimeMismatchEntry_Description_ProdCmptToGen;

    public static String LinkChangingOverTimeMismatchEntry_Description_RemoveOnly;

    public static String MissingPropertyValueEntry_valueTransferedInformation;

    public static String PropertyTypeMismatchEntry_desc;

    public static String ValueMismatchEntry_convertMultiToSingleValue;

    public static String ValueMismatchEntry_convertSingleToMultiValue;

    public static String ValueSetMismatchEntry_desc;

    public static String MultilingualMismatchEntry_convertToStringValue;

    public static String MultilingualMismatchEntry_convertToInternatlStringValue;

}
