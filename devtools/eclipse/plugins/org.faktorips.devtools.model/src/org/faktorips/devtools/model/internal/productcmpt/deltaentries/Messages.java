/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.internal.productcmpt.deltaentries.messages"; //$NON-NLS-1$

    public static String DatatypeMismatchEntry_datatypeMissmatchDescription;
    public static String DeletedTemplateLinkEntry_removeInheritedTemplateLink;
    public static String DeletedTemplateLinkEntry_removeUndefinedTemplateLink;
    public static String HiddenAttributeMismatchEntry_desc;
    public static String InvalidGenerationsDeltaEntry_description;
    public static String LinkChangingOverTimeMismatchEntry_Description_GenToProdCmpt;
    public static String LinkChangingOverTimeMismatchEntry_Description_ProdCmptToGen;
    public static String LinkChangingOverTimeMismatchEntry_Description_RemoveOnly;
    public static String MissingPropertyValueEntry_ATTRIBUTE_VALUE;
    public static String MissingPropertyValueEntry_CONFIGURED_DEFAULT;
    public static String MissingPropertyValueEntry_CONFIGURED_VALUESET;
    public static String MissingPropertyValueEntry_FORMULA;
    public static String MissingPropertyValueEntry_TABLE_CONTENT_USAGE;
    public static String MissingPropertyValueEntry_VALIDATION_RULE_CONFIG;
    public static String MissingPropertyValueEntry_valueTransferedInformation;
    public static String MissingTemplateLinkEntry_missingTemplateLink;
    public static String MultilingualMismatchEntry_convertToInternatlStringValue;
    public static String MultilingualMismatchEntry_convertToStringValue;
    public static String PropertyTypeMismatchEntry_desc;
    public static String ValueMismatchEntry_convertMultiToSingleValue;
    public static String ValueMismatchEntry_convertSingleToMultiValue;
    public static String ValueSetMismatchEntry_desc;
    public static String ValueSetTemplateMismatchEntry_desc;
    public static String WrongRuntimeIdForLinkEntry_Description;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

}
