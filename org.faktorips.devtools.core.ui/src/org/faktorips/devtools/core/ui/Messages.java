/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AbstractCompletionProcessor_labelDefaultPackage;

    public static String DefaultLabelProvider_labelDefaultPackage;

    public static String FaktorIpsPreferencePage_advancedTeamFunctionsInProductDefExplorer;

    public static String FaktorIpsPreferencePage_simpleContextMenu;

    public static String FaktorIpsPreferencePage_label_fourSections;

    public static String FaktorIpsPreferencePage_label_twoSections;

    public static String FaktorIpsPreferencePage_labelEnumTypeDisplay;

    public static String FaktorIpsPreferencePage_labeRangeEditFieldsInOneRow;

    public static String FaktorIpsPreferencePage_title_numberOfSections;

    public static String FaktorIpsPreferencePage_title_refactoringMode;

    public static String FaktorIpsPreferencePage_label_direct;

    public static String FaktorIpsPreferencePage_label_explicit;

    public static String FaktorIpsPreferencePage_tooltip_direct;

    public static String FaktorIpsPreferencePage_tooltip_explicit;

    public static String PdPackageSelectionDialog_title;

    public static String PdPackageSelectionDialog_description;

    public static String FaktorIpsPreferencePage_labelNullValue;

    public static String FaktorIpsPreferencePage_labelProductTypePostfix;

    public static String FaktorIpsPreferencePage_labelNamingScheme;

    public static String PdObjectSelectionDialog_labelMatches;

    public static String PdObjectSelectionDialog_labelQualifier;

    public static String DatatypeSelectionDialog_title;

    public static String DatatypeSelectionDialog_description;

    public static String DatatypeSelectionDialog_labelMatchingDatatypes;

    public static String DatatypeSelectionDialog_msgLabelQualifier;

    public static String PdSourceRootSelectionDialog_title;

    public static String PdSourceRootSelectionDialog_description;

    public static String AbstractCompletionProcessor_msgNoProject;

    public static String AbstractCompletionProcessor_msgInternalError;

    public static String FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating;

    public static String FaktorIpsPreferencePage_LabelFormattingOfValues;

    public static String FaktorIpsPreferencePage_labelCanNavigateToModelOrSourceCode;

    public static String FaktorIpsPreferencePage_titleWorkingMode;

    public static String FaktorIpsPreferencePage_labelWorkingModeBrowse;

    public static String FaktorIpsPreferencePage_labelWorkingModeEdit;

    public static String FaktorIpsPreferencePage_modifyRuntimeId;

    public static String FaktorIpsPreferencePage_labelMaxHeapSizeIpsTestRunner;

    public static String IpsClasspathContainerPage_0;

    public static String IpsClasspathContainerPage_1;

    public static String IpsClasspathContainerPage_2;

    public static String IpsClasspathContainerPage_3;

    public static String IpsClasspathContainerPage_4;

    public static String IpsClasspathContainerPage_5;

    public static String LinkDropListener_selectAssociation;

}
