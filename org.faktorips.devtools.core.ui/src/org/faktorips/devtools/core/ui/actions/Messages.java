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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.actions.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ExpandAllAction_Selection_Description;
    public static String ExpandAllAction_Description;
    public static String CollapseAllAction_Description;
    public static String AddIpsNatureAction_basePackage_default;
    public static String AddIpsNatureAction_basePackageName;
    public static String AddIpsNatureAction_basePackageNameNotValid;
    public static String AddIpsNatureAction_defaultRuntimeIdPrefix;
    public static String AddIpsNatureAction_defaultSourceFolderName;
    public static String AddIpsNatureAction_dialogMessage;
    public static String AddIpsNatureAction_dialogTitle;
    public static String AddIpsNatureAction_ErrorNoSourceFolderName;
    public static String AddIpsNatureAction_fullProject;
    public static String AddIpsNatureAction_modelProject;
    public static String AddIpsNatureAction_productDefinitionProject;
    public static String AddIpsNatureAction_ProjectType;
    public static String AddIpsNatureAction_readdNature;
    public static String AddIpsNatureAction_runtimeIdPrefix;
    public static String AddIpsNatureAction_sourceFolderName;
    public static String AddIpsNatureAction_TheSourceFolderMustBeADirectChildOfTheProject;
    public static String AddIpsNatureAction_needToSelectOneSingleJavaProject;
    public static String AddIpsNatureAction_noJavaProject;
    public static String AddIpsNatureAction_errorTitle;
    public static String AddIpsNatureAction_msgIPSNatureAlreadySet;
    public static String AddIpsNatureAction_msgSourceInProjectImpossible;
    public static String AddIpsNatureAction_msgErrorCreatingIPSProject;
    public static String AddIpsNatureAction_mustSelectAJavaProject;
    public static String AddIpsNatureAction_titleAddFaktorIpsNature;

    public static String DeleteRowAction_Label;
    public static String DeleteRowAction_Tooltip;

    public static String FindProductReferencesAction_description;
    public static String FindProductReferencesAction_name;
    public static String FindPolicyReferencesAction_description;
    public static String FindPolicyReferencesAction_name;
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

    public static String ShowStructureAction_description;
    public static String ShowStructureAction_name;

    public static String OpenEditorAction_name;
    public static String OpenEditorAction_description;
    public static String OpenEditorAction_tooltip;

    public static String IpsAction_msgUnsupportedSelection;

    public static String IpsPasteAction_cannot_copy;
    public static String IpsPasteAction_errorTitle;
    public static String IpsPasteAction_suggestedNamePrefixSimple;
    public static String IpsPasteAction_suggestedNamePrefixComplex;
    public static String IpsPasteAction_titleNamingConflict;
    public static String IpsPasteAction_msgNamingConflict;
    public static String IpsPasteAction_msgFileAllreadyExists;

    public static String NewProductComponentAction_name;

    public static String IpsRefactoringAction_refactoringCurrentlyNotApplicable;
    public static String RenameAction_name;

    public static String MoveAction_name;

    public static String NewProductCmptRelationAction_name;

    public static String NewPolicyComponentTypeAction_name;

    public static String ChangeWorkingDateAction_title;
    public static String ChangeWorkingDateAction_description;
    public static String ChangeWorkingDateAction_errorFallbackMessageParameter;
    public static String ChangeWorkingDateAction_errorPrefix;

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

    public static String ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow1;
    public static String ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow2;
    public static String ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow3;
    public static String ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow4;
    public static String ChangeWorkingDateAction_warningRecentGenerationReadOnlyRow5;
    public static String ChangeWorkingDateAction_labelEditRecentGenerations;

    public static String ShowInstanceAction_descriptionForTypes;
    public static String ShowInstanceAction_nameForTypes;
    public static String ShowInstanceAction_descriptionForInstances;
    public static String ShowInstanceAction_nameForInstances;

    public static String CreateMissingEnumContentsAction_text;

}
