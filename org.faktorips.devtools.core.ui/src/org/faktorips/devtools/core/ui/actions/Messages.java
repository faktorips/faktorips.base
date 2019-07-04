/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.actions.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AddIpsNatureAction_needToSelectOneSingleJavaProject;
    public static String AddIpsNatureAction_titleAddFaktorIpsNature;
    public static String AddIpsNatureAction_msgIPSNatureAlreadySet;
    public static String AddIpsNatureAction_mustSelectAJavaProject;
    public static String AddIpsNatureAction_readdNature;
    public static String AddIpsNatureAction_msgSourceInProjectImpossible;
    public static String AddIpsNatureAction_msgErrorCreatingIPSProject;

    public static String ExpandAllAction_Selection_Description;
    public static String ExpandAllAction_Description;
    public static String CollapseAllAction_Description;
    public static String CopyTableAction_title;

    public static String DeleteRowAction_Label;
    public static String DeleteRowAction_Tooltip;

    public static String FixDifferencesAction_text;

    public static String IpsEditSortOrderAction_description;
    public static String IpsEditSortOrderAction_dialogInfoText;
    public static String IpsEditSortOrderAction_dialogTitle;
    public static String IpsEditSortOrderAction_text;
    public static String IpsEditSortOrderAction_tooltip;

    public static String IpsTestCaseCopyAction_name;

    public static String NewEnumTypeAction_title;
    public static String NewEnumContentAction_title;
    public static String NewBusinessFunctionAction_title;
    public static String NewProductCmptTypeAction_name;
    public static String NewTableStructureAction_name;
    public static String NewTestCaseTypeAction_name;

    public static String OpenIpsObjectAction_description;
    public static String OpenIpsObjectAction_dialogMessage;
    public static String OpenIpsObjectAction_dialogTitle;
    public static String OpenIpsObjectAction_titleText;
    public static String OpenIpsObjectAction_tooltip;
    public static String OpenIpsObjectSelectionDialog_processName;

    public static String OpenEditorAction_name;
    public static String OpenEditorAction_description;
    public static String OpenEditorAction_tooltip;

    public static String IpsAction_msgUnsupportedSelection;

    public static String IpsPasteAction_cannot_copy;
    public static String IpsPasteAction_errorTitle;
    public static String IpsPasteAction_titleNamingConflict;
    public static String IpsPasteAction_msgNamingConflict;
    public static String IpsPasteAction_msgFileAllreadyExists;

    public static String NewResourceNameValidator_suggestedNamePrefixSimple;
    public static String NewResourceNameValidator_suggestedNamePrefixComplex;

    public static String IpsRefactoringAction_refactoringCurrentlyNotApplicable;
    public static String RenameAction_name;

    public static String MoveAction_name;

    public static String NewPolicyComponentTypeAction_name;

    public static String IpsDeepCopyAction_name;
    public static String IpsDeepCopyAction_nameNewVersion;
    public static String IpsDeepCopyAction_titleNoVersion;
    public static String IpsDeepCopyAction_msgNoVersion;

    public static String NewTableContentAction_name;

    public static String NewFolderAction_titleNewFolder;
    public static String NewFolderAction_descriptionNewFolder;
    public static String NewFolderAction_valueNewFolder;
    public static String NewFolderAction_msgNoParentFound;
    public static String NewFolderAction_msgFolderAllreadyExists;
    public static String NewFolderAction_name;
    public static String NewFolderAction_FoldernameMustNotContainBlanks;
    public static String NewFolderAction_InvalidFoldername;

    public static String IpsTestCaseAction_name;
    public static String IpsTestCaseAction_description;
    public static String IpsTestCaseAction_tooltip;

    public static String NewTestCaseAction_name;

    public static String NewFileResourceAction_name;
    public static String NewFileResourceAction_description;

    public static String IpsPropertiesAction_name;

    public static String IpsTestAction_titleCantRunTest;
    public static String IpsTestAction_msgCantRunTest;

    public static String TableImportExportAction_confirmDialogDirtyTableContentsText;
    public static String TableImportExportAction_confirmDialogDirtyTableContentsTitle;
    public static String TableImportExportAction_exportActionTitle;
    public static String TableImportExportAction_exportActionTooltip;
    public static String TableImportExportAction_importActionTitle;
    public static String TableImportExportAction_importActionTooltip;

    public static String EnumImportExportAction_confirmDialogDirtyTableContentsText;
    public static String EnumImportExportAction_confirmDialogDirtyTableContentsTitle;
    public static String EnumImportExportAction_exportActionTitle;
    public static String EnumImportExportAction_exportActionTooltip;
    public static String EnumImportExportAction_importActionTitle;
    public static String EnumImportExportAction_importActionTooltip;

    public static String ToggleLinkingAction_Description;
    public static String ToggleLinkingAction_Text;
    public static String ToggleLinkingAction_ToolTipText;

    public static String TreeViewerRefreshAction_TooltipText;

    public static String IpsTestAction_RunMessageDialogNoTestsFound_Title;
    public static String IpsTestAction_RunMessageDialogNoTestsFound_Text;

    public static String NewIpsPacketAction_name;

    public static String CreateIpsArchiveAction_Name;
    public static String CreateIpsArchiveAction_Description;
    public static String CreateIpsArchiveAction_Tooltip;

    public static String IpsPasteAction_Error_CannotPasteIntoSelectedElement;

    public static String MigrateProjectAction_text;

    public static String CreateMissingEnumContentsAction_text;

    public static String CleanUpTranslationsAction_text;
    public static String CleanUpTranslationsAction_progressTask;

    public static String CreateNewGenerationAction_title;

}
