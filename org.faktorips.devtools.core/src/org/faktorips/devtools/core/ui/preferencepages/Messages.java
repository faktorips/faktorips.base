/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.preferencepages.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String ArchiveComposite_button_add_archive;
    public static String ArchiveComposite_button_add_external_archive;
    public static String ArchiveComposite_button_remove_archive;
    public static String ArchiveComposite_dialog_message_add;
    public static String ArchiveComposite_dialog_title_add;
    public static String ArchiveComposite_dialog_warning_select_archive;
    public static String ArchiveComposite_labelProvider_invalid_element;
    public static String ArchiveComposite_viewer_label;
    public static String IpsObjectPathLabelProvider_label_output_folder;
    public static String IpsObjectPathPropertyPage_1;
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
    public static String SrcFolderComposite_derived_sources_label;
    public static String SrcFolderComposite_edit_item_tet;
    public static String SrcFolderComposite_enterFolderName_validator;
    public static String SrcFolderComposite_folder_already_exists_validator;
    public static String SrcFolderComposite_folderSelection_dialog_message;
    public static String SrcFolderComposite_folderSelection_dialog_title;
    public static String SrcFolderComposite_mergable_sources_label;
    public static String SrcFolderComposite_multipleFolders_checkbox_label;
    public static String SrcFolderComposite_remove_folder_text;
    public static String SrcFolderComposite_treeViewer_label;
}
