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
    public static String IpsObjectPathsPropertyPage_closed_project_message;
    public static String IpsObjectPathContainer_tab_projects;
    public static String ReferencedProjectsComposite_label_provider_invalid_element;
    public static String ReferencedProjectsComposite_projects_add_button;
    public static String ReferencedProjectsComposite_projects_remove_button;
    public static String ReferencedProjectsComposite_required_projects_label;
    public static String ReferencedProjectsComposite_select_projects_label;
    public static String ReferencedProjectsComposite_select_projects_title;
}
