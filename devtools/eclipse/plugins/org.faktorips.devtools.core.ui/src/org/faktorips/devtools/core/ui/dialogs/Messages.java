/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.dialogs.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AddIpsNatureDialog_basePackageName;
    public static String AddIpsNatureDialog_basePackageNameNotValid;
    public static String AddIpsNatureDialog_dialogMessage;
    public static String AddIpsNatureDialog_dialogTitle;
    public static String AddIpsNatureDialog_ErrorDialogText;
    public static String AddIpsNatureDialog_ErrorDialogTitle;
    public static String AddIpsNatureDialog_ErrorNoSourceFolderName;
    public static String AddIpsNatureDialog_fullProject;
    public static String AddIpsNatureDialog_modelProject;
    public static String AddIpsNatureDialog_productDefinitionProject;
    public static String AddIpsNatureDialog_PersistenceSupport;
    public static String AddIpsNatureDialog_GroovySupport;
    public static String AddIpsNatureDialog_ProjectType;
    public static String AddIpsNatureDialog_runtimeIdPrefix;
    public static String AddIpsNatureDialog_sourceFolderName;
    public static String AddIpsNatureDialog_noJavaProject;
    public static String AddIpsNatureDialog_errorTitle;
    public static String AddIpsNatureDialog_NamingGroup;
    public static String AddIpsNatureDialog_SupportedLanguagesGroup;
    public static String AddIpsNatureDialog_JaxbSupport;

    public static String InternationalValueDialog_languageColumnTitle;
    public static String InternationalValueDialog_valueColumnTitle;
    public static String InternationalValueDialog_titleText;
    public static String InternationalValueDialog_descriptionText;

    public static String SupportedLanguagesControl_columnLanguageCode;
    public static String SupportedLanguagesControl_columnLanguageName;

    public static String LocaleSelectionDialog_title;
    public static String LocaleSelectionDialog_message;

    public static String IpsPackageSortDefDialog_down;
    public static String IpsPackageSortDefDialog_headlineText;
    public static String IpsPackageSortDefDialog_restore;
    public static String IpsPackageSortDefDialog_up;
    public static String MultiValueDialog_TableDescription;
    public static String MultiValueDialog_TitleText;
    public static String OpenIpsObjectSelectionDialog_Filter;
}
