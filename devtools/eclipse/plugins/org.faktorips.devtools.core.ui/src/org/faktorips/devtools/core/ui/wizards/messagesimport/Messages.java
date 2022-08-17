/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.messagesimport.messages"; //$NON-NLS-1$
    public static String MessagesImportPMO_EmptyTargetname;
    public static String MessagesImportPMO_EmptyFilename;
    public static String MessagesImportPMO_FilenameIsDirectory;
    public static String MessagesImportPMO_FileDoesNotExist;
    public static String MessagesImportPage_Label_SourceGroup;
    public static String MessagesImportPage_Label_TargetGroup;
    public static String MessagesImportPage_labelLocale;
    public static String MessagesImportPage_labelTarget;
    public static String MessagesImportPage_labelImportFile;
    public static String MessagesImportPage_pageTitle;
    public static String MessagesImportWizard_pageName;
    public static String MessagesImportWizard_windowTitle;
    public static String MessagesImportPMO_EmptyLocale;
    public static String MessagesImportPMO_Msg_noIdColumnIndex;
    public static String MessagesImportPMO_Msg_NoTextColumnIndex;
    public static String MessagesImportPMO_noColumnDelimiter;
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
    public static String MessagesImportWizard_checkboxEnableWarnings;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do not instatiate
    }
}
