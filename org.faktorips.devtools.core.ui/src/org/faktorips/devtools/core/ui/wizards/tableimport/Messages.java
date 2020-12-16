/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tableimport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TableImport_title;
    public static String TableImportWizard_operationName;
    public static String TableImportWizard_tableImportControlBody;
    public static String TableImportWizard_tableImportControlTitle;
    public static String SelectTableContentsPage_title;
    public static String SelectTableContentsPage_labelContents;
    public static String SelectTableContentsPage_msgMissingContent;
    public static String SelectTableContentsPage_labelProject;
    public static String SelectTableContentsPage_msgProjectEmpty;
    public static String SelectTableContentsPage_msgNonExistingProject;
    public static String SelectTableContentsPage_msgContentsEmpty;
    public static String SelectTableContentsPage_msgStructureNotValid;
    public static String SelectFileAndImportMethodPage_labelImportExisting;
    public static String SelectFileAndImportMethodPage_labelImportNew;
    public static String TableContentsPage_title;
    public static String TableContentsPage_labelStructure;
    public static String TableContentsPage_msgMissingStructure;
    public static String TableContentsPage_tableStructureHasntGotAnyColumns;
    public static String TableContentsPage_msgStructureEmpty;

}
