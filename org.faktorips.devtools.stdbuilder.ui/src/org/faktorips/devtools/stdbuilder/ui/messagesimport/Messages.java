/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.stdbuilder.ui.messagesimport.messages"; //$NON-NLS-1$
    public static String MessagesImportPMO_EmptyTargetname;
    public static String MessagesImportPMO_EmptyFilename;
    public static String MessagesImportPMO_FilenameIsDirectory;
    public static String MessagesImportPMO_FileDoesNotExist;
    public static String MessagesImportPage_labelLocale;
    public static String MessagesImportPage_labelTarget;
    public static String MessagesImportPage_labelTranslations;
    public static String MessagesImportPage_pageTitle;
    public static String MessagesImportWizard_pageName;
    public static String MessagesImportWizard_windowTitle;
    public static String MessagesImportPMO_EmptyLocale;
    public static String MessagesImportWizard_labelFormat;
    public static String MessagesImportWizard_labelFormatProperties;
    public static String MessagesImportWizard_labelFormatCSV;
    public static String MessagesImportWizard_labelFormatSettings;
    public static String MessagesImportWizard_labelFormatSettingsDelimiter;
    public static String MessagesImportWizard_labelFormatSettingsIdentifier;
    public static String MessagesImportWizard_labelFormatSettingsColumn;
    public static String MessagesImportWizard_labelIdentification;
    public static String MessagesImportWizard_labelIdentificationName;
    public static String MessagesImportWizard_labelIdentificationCode;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do not instatiate
    }
}
