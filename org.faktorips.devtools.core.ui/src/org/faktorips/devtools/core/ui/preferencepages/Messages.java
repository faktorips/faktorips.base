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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.preferencepages.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ArchiveComposite_button_add_archive;
    public static String ArchiveComposite_button_add_external_archive;
    public static String ArchiveComposite_button_remove_archive;
    public static String ArchiveComposite_dialog_message_add;
    public static String ArchiveComposite_dialog_title_add;
    public static String ArchiveComposite_dialog_warning_select_archive;
    public static String ArchiveComposite_viewer_label;
    public static String BuilderSetContainer_builderSetComboLabel;
    public static String BuilderSetContainer_tableColumnLabel_Description;
    public static String BuilderSetContainer_tableColumnLabel_Property;
    public static String BuilderSetContainer_tableColumnLabel_Value;
    public static String BuilderSetContainer_tableViewerLabel;
    public static String BuilderSetPropertyEditingSupport_validatorErrorMessage;
    public static String BuilderSetPropertyPage_saveDialog_Apply;
    public static String BuilderSetPropertyPage_saveDialog_ApplyLater;
    public static String BuilderSetPropertyPage_saveDialog_Discard;
    public static String BuilderSetPropertyPage_saveDialog_Message;
    public static String BuilderSetPropertyPage_saveDialog_Title;
    public static String IpsObjectPathContainer_tab_archives;
    public static String IpsObjectPathContainer_tab_path_order;
    public static String IpsObjectPathContainer_tab_source;
    public static String IpsObjectPathLabelProvider_base_package_derived;
    public static String IpsObjectPathLabelProvider_default;
    public static String IpsObjectPathLabelProvider_output_folder_derived;
    public static String IpsObjectPathLabelProvider_output_folder_mergable;
    public static String IpsObjectPathLabelProvider_package_name_derived;
    public static String IpsObjectPathLabelProvider_package_name_mergable;
    public static String IpsObjectPathLabelProvider_toc_file;
    public static String IpsObjectPathPropertyPage_apply_discard_applyLater_message;
    public static String IpsObjectPathPropertyPage_apply_button;
    public static String IpsObjectPathPropertyPage_apply_later_button;
    public static String IpsObjectPathPropertyPage_changes_in_dialog_title;
    public static String IpsObjectPathPropertyPage_discard_button;
    public static String IpsObjectPathsPropertyPage_closed_project_message;
    public static String IpsObjectPathContainer_tab_projects;
    public static String ObjectPathOrderComposite_buttonBottom_label;
    public static String ObjectPathOrderComposite_buttonDown_label;
    public static String ObjectPathOrderComposite_buttonTop_label;
    public static String ObjectPathOrderComposite_buttonUp_label;
    public static String ObjectPathOrderComposite_tableViewer_label;
    public static String OutputFolderEditDialog_button_title_browse;
    public static String OutputFolderEditDialog_dialog_title;
    public static String OutputFolderEditDialog_use_default_label;
    public static String OutputFolderEditDialog_use_sepcific_label;
    public static String PackageNameEditDialog_button_text_use_default_folder;
    public static String PackageNameEditDialog_button_text_use_specific_folder;
    public static String PackageNameEditDialog_dialog_title;
    public static String ObjectPathEntryLabelDecorator_suffix_derived_folder_not_existing;
    public static String ObjectPathEntryLabelDecorator_suffix_derived_folder_undefined;
    public static String ObjectPathEntryLabelDecorator_suffix_mergable_folder_not_existing;
    public static String ObjectPathEntryLabelDecorator_suffix_mergable_folder_undefined;
    public static String ObjectPathEntryLabelDecorator_suffix_missing;
    public static String ObjectPathEntryLabelDecorator_suffix_missing_folder;
    public static String ObjectPathEntryLabelDecorator_suffix_missing_project;
    public static String ObjectPathEntryLabelDecorator_suffix_not_child_of_root;
    public static String ObjectPathEntryLabelDecorator_suffix_not_specified;
    public static String ObjectPathEntryLabelDecorator_suffix_project_not_specified;
    public static String ReferencedProjectsComposite_label_provider_invalid_element;
    public static String ReferencedProjectsComposite_projects_add_button;
    public static String ReferencedProjectsComposite_projects_remove_button;
    public static String ReferencedProjectsComposite_required_projects_label;
    public static String ReferencedProjectsComposite_select_projects_label;
    public static String ReferencedProjectsComposite_select_projects_title;
    public static String SrcFolderComposite_add_folder_text;
    public static String SrcFolderComposite_browse_button_text;
    public static String SrcFolderComposite_create_new_folder_defaultText;
    public static String SrcFolderComposite_create_new_folder_message;
    public static String SrcFolderComposite_create_new_folder_title;
    public static String SrcFolderComposite_derived_package_label;
    public static String SrcFolderComposite_derived_sources_label;
    public static String SrcFolderComposite_edit_item_tet;
    public static String SrcFolderComposite_enterFolderName_validator;
    public static String SrcFolderComposite_filename_invalid_validator;
    public static String SrcFolderComposite_folder_already_exists_validator;
    public static String SrcFolderComposite_folderSelection_dialog_message;
    public static String SrcFolderComposite_folderSelection_dialog_title;
    public static String SrcFolderComposite_mergable_package_label;
    public static String SrcFolderComposite_mergable_sources_label;
    public static String SrcFolderComposite_multipleFolders_checkbox_label;
    public static String SrcFolderComposite_remove_folder_text;
    public static String SrcFolderComposite_tocpath_message;
    public static String SrcFolderComposite_tocpath_title;
    public static String SrcFolderComposite_treeViewer_label;
}
