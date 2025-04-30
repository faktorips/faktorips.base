/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    public static String DefaultValueRepresentation_RadioButtonGroup;

    public static String DefaultValueRepresentation_EditField;

    public static String FaktorIpsPreferencePage_advancedTeamFunctionsInProductDefExplorer;

    public static String FaktorIpsPreferencePage_simpleContextMenu;

    public static String FaktorIpsPreferencePage_autoValidationTables;

    public static String FaktorIpsPreferencePage_tooltipAutoValidationTables;

    public static String FaktorIpsPreferencePage_label_fourSections;

    public static String FaktorIpsPreferencePage_label_twoSections;

    public static String FaktorIpsPreferencePage_labelNamedDataTypeDisplay;

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

    public static String FaktorIpsPreferencePage_CopyWizardModeTooltip;

    public static String FaktorIpsPreferencePage_CopyWizardModeTitle;

    public static String FaktorIpsPreferencePage_CopyWizardModeCopy;

    public static String FaktorIpsPreferencePage_CopyWizardModeLink;

    public static String FaktorIpsPreferencePage_CopyWizardModeSmartMode;

    public static String IpsClasspathContainerPage_description;

    public static String IpsClasspathContainerPage_includeJoda;

    public static String IpsClasspathContainerPage_includeGroovy;

    public static String IpsClasspathContainerPage_disclaimer1;

    public static String IpsClasspathContainerPage_disclaimer2;

    public static String IpsClasspathContainerPage_bundleNotInstalled;

    public static String IpsClasspathContainerPage_title;

    public static String LinkDropListener_selectAssociation;
    public static String IpsUIPlugin_titleErrorDialog;
    public static String IpsUIPlugin_msgUnexpectedError;
    public static String IpsUIPlugin_infoDefaultTextEditorWasOpened;
    public static String IpsUIPlugin_dialogSaveDirtyEditorMessageMany;
    public static String IpsUIPlugin_dialogSaveDirtyEditorMessageSimple;
    public static String IpsUIPlugin_dialogSaveDirtyEditorTitle;
    public static String IpsClasspathContainerPage_includeJaxbSupport;
}
