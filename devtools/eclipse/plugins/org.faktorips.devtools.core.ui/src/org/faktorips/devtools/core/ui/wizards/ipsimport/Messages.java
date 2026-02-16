/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsimport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String SelectFileAndImportMethodPage_title;
    public static String SelectFileAndImportMethodPage_labelName;
    public static String SelectFileAndImportMethodPage_labelFirstRowContainsColumnHeader;
    public static String SelectFileAndImportMethodPage_labelImportExistingReplace;
    public static String SelectFileAndImportMethodPage_labelImportExistingAppend;
    public static String SelectFileAndImportMethodPage_msgEmptyFilename;
    public static String SelectFileAndImportMethodPage_msgMissingImportMethod;
    public static String SelectFileAndImportMethodPage_msgMissingImportExistingMethod;
    public static String SelectFileAndImportMethodPage_msgMissingFileFormat;
    public static String SelectFileAndImportMethodPage_msgFileDoesNotExist;
    public static String SelectFileAndImportMethodPage_msgFilenameIsDirectory;
    public static String SelectFileAndImportMethodPage_labelFileFormat;
    public static String SelectFileAndImportMethodPage_labelNullRepresentation;
    public static String SelectFileAndImportMethodPage_labelEnumAsNameAndId;
    public static String ImportPreviewPage_configurationGroupTitle;
    public static String ImportPreviewPage_livePreviewGroupTitle;
    public static String ImportPreviewPage_pageName;
    public static String ImportPreviewPage_pageTitle;
    public static String ImportPreviewPage_validationWarningInvalidFile;
    public static String ImportPreviewPage_warnFileInvalid;

}
