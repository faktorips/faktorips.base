/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TestCaseDeltaDialog_Title;
    public static String TestCaseDeltaDialog_Message;
    public static String TestCaseDeltaDialog_Label_DifferencesTree;
    public static String TestCaseDeltaDialog_Button_Fix;
    public static String TestCaseDeltaDialog_Button_Ignore;
    public static String TestCaseDeltaType_MissingRootTestObject;
    public static String TestCaseDeltaType_MissingTestAttributeValue;
    public static String TestCaseDeltaType_MissingTestParam;
    public static String TestCaseDeltaType_MissingTestAttribute;
    public static String TestCaseDeltaLabelProvider_Undefined;
    public static String TestCaseDeltaType_DifferentSortOrder;

}
