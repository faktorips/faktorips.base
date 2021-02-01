/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String NewTableContentsValidator_msg_noProject;
    public static String NewTableContentsValidator_msg_noStructure;
    public static String NewTableContentsValidator_msgInvalidStructure;
    public static String NewTableContentsValidator_msgNoAdditionalContentsAllowed;
    public static String NewTableContentsWizard_title;
    public static String TableContentsPage_title;
    public static String TableContentsPage_label_name;
    public static String TableContentsPage_label_project;
    public static String TableContentsPage_labelStructure;
    public static String TableContentsPage_pageTitle;

}
