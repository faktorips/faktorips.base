/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.testcase.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestRuleViolationType_NotViolated;
    public static String TestRuleViolationType_Violated;
    public static String TestRuleViolationType_Unknown;
    public static String TestRuleViolationType_TextNotViolated;
    public static String TestRuleViolationType_TextViolated;

}
