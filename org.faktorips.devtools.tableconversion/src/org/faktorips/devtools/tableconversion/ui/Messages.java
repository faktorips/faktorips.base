/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Roman Grutza
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.tableconversion.ui.messages"; //$NON-NLS-1$

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String CSVPropertyCompositeFactory_dateFormatHelp1;
    public static String CSVPropertyCompositeFactory_dateFormatLabel;
    public static String CSVPropertyCompositeFactory_errMsgDecimalGroupingLength;
    public static String CSVPropertyCompositeFactory_errMsgDecimalSeparatorAndGroupingCharsAreEqual;
    public static String CSVPropertyCompositeFactory_errMsgDecimalSeparatorLength;
    public static String CSVPropertyCompositeFactory_errMsgInvalidDateFormat;
    public static String CSVPropertyCompositeFactory_errMsgFieldDelimiterLength;
    public static String CSVPropertyCompositeFactory_fieldDelimiterLabel;
    public static String CSVPropertyCompositeFactory_labelDecimalGrouping;
    public static String CSVPropertyCompositeFactory_labelDecimalSeparator;
}
