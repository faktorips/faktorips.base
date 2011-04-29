/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

    public static String AddIpsNatureDialog_basePackage_default;
    public static String AddIpsNatureDialog_basePackageName;
    public static String AddIpsNatureDialog_basePackageNameNotValid;
    public static String AddIpsNatureDialog_defaultRuntimeIdPrefix;
    public static String AddIpsNatureDialog_defaultSourceFolderName;
    public static String AddIpsNatureDialog_dialogMessage;
    public static String AddIpsNatureDialog_dialogTitle;
    public static String AddIpsNatureDialog_ErrorNoSourceFolderName;
    public static String AddIpsNatureDialog_fullProject;
    public static String AddIpsNatureDialog_modelProject;
    public static String AddIpsNatureDialog_productDefinitionProject;
    public static String AddIpsNatureDialog_PersistenceSupport;
    public static String AddIpsNatureDialog_ProjectType;
    public static String AddIpsNatureDialog_runtimeIdPrefix;
    public static String AddIpsNatureDialog_sourceFolderName;
    public static String AddIpsNatureDialog_TheSourceFolderMustBeADirectChildOfTheProject;
    public static String AddIpsNatureDialog_noJavaProject;
    public static String AddIpsNatureDialog_errorTitle;
    public static String AddIpsNatureDialog_NamingGroup;
    public static String AddIpsNatureDialog_SupportedLanguagesGroup;

    public static String SupportedLanguagesControl_columnLanguageCode;
    public static String SupportedLanguagesControl_columnLanguageName;

    public static String LocaleSelectionDialog_title;
    public static String LocaleSelectionDialog_message;

    public static String IpsPackageSortDefDialog_down;
    public static String IpsPackageSortDefDialog_headlineText;
    public static String IpsPackageSortDefDialog_restore;
    public static String IpsPackageSortDefDialog_up;
    public static String OpenIpsObjectSelectionDialog_Filter;
}
