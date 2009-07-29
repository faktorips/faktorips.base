/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.osgi.util.NLS;

/**
 * @author Roman Grutza
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsimport.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
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
    public static String SelectFileAndImportMethodPage_labelFileFormat;
    public static String SelectFileAndImportMethodPage_labelNullRepresentation;
    public static String ImportPreviewPage_configurationGroupTitle;
    public static String ImportPreviewPage_livePreviewGroupTitle;
    public static String ImportPreviewPage_pageName;
    public static String ImportPreviewPage_pageTtile;
    public static String ImportPreviewPage_validationWarningInvalidFile;
}
