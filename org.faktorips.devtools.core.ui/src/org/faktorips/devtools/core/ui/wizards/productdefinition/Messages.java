/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productdefinition.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String FolderAndPackagePage_check_openInEditor;
    public static String FolderAndPackagePage_label_package;
    public static String FolderAndPackagePage_label_rootFolder;
    public static String FolderAndPackagePage_page_title;
    public static String FolderAndPackagePage_title;
    public static String TypeAndTemplateSelectionComposite_noTemplate;
    public static String TypeSelectionComposite_msg_Filter;
    public static String TypeSelectionComposite_msgLabel_Filter;
    public static String TypeSelectionComposite_label_description;
    public static String TypeSelectionComposite_label_noDescriptionAvailable;
    public static String NewProductDefinitionValidator_msg_invalidPackageRoot;
    public static String NewProductDefinitionValidator_msg_invalidPackage;
    public static String NewProductDefinitionValidator_msg_srcFileExists;
    public static String NewGenerationWizard_title;
    public static String NewGenerationRunnable_taskName;
    public static String ChooseValidityDatePage_pageTitle;
    public static String ChooseValidityDatePage_msgPageInfoSingular;
    public static String ChooseValidityDatePage_msgPageInfoPlural;
    public static String ChooseValidityDatePage_msgValidFromInvalid;
    public static String ChooseValidityDatePage_labelValidFrom;
    public static String ChooseValidityDatePage_labelSkipExistingGenerations;

}
