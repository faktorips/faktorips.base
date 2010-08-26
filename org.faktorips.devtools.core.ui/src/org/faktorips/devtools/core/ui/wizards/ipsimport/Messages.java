/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public static String ImportPreviewPage_configurationGroupTitle;
    public static String ImportPreviewPage_livePreviewGroupTitle;
    public static String ImportPreviewPage_pageName;
    public static String ImportPreviewPage_pageTitle;
    public static String ImportPreviewPage_validationWarningInvalidFile;
    public static String ImportPreviewPage_warnFileInvalid;

}
