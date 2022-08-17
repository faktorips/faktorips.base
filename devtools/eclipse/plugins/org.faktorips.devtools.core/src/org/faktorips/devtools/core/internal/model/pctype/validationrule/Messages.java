/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.pctype.validationrule.messages"; //$NON-NLS-1$
    public static String ValidationRuleCsvImporter_error_duplicatedKey;
    public static String ValidationRuleCsvImporter_status_multipleUsedMessageCodes;
    public static String ValidationRuleCsvImporter_warning_multipleUsedMessageCodes;
    public static String ValidationRuleMessagesPropertiesImporter_error_loadingPropertyFile;
    public static String ValidationRuleMessagesPropertiesImporter_status_illegalMessage;
    public static String ValidationRuleMessagesPropertiesImporter_status_importingMessages;
    public static String ValidationRuleMessagesPropertiesImporter_status_missingMessage;
    public static String ValidationRuleMessagesPropertiesImporter_status_problemsDuringImport;
    public static String ValidationRuleMessagesPropertiesImporter_warning_invalidMessageKey;
    public static String ValidationRuleMessagesPropertiesImporter_warning_ruleNotFound;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
