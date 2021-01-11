/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productrelease;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productrelease.messages"; //$NON-NLS-1$
    public static String ProductReleaserBuilderWizard_complete_error;
    public static String ProductReleaserBuilderWizard_complete_aborted;
    public static String ProductReleaserBuilderWizard_complete_success;
    public static String ReleaserBuilderWizard_title;
    public static String ReleaserBuilderWizard_exception_NotReady;
    public static String ReleaserBuilderWizardSelectionPage_error_couldNotDetermineFormat;
    public static String ReleaserBuilderWizardSelectionPage_error_illegalVersion;
    public static String ReleaserBuilderWizardSelectionPage_error_noDeploymentExtension;
    public static String ReleaserBuilderWizardSelectionPage_group_project;
    public static String ReleaserBuilderWizardSelectionPage_group_targetsystem;
    public static String ReleaserBuilderWizardSelectionPage_group_version;
    public static String ReleaserBuilderWizardSelectionPage_info_selectProject;
    public static String ReleaserBuilderWizardSelectionPage_latest_version;
    public static String ReleaserBuilderWizardSelectionPage_new_version;
    public static String ReleaserBuilderWizardSelectionPage_select_project;
    public static String ReleaserBuilderWizardSelectionPage_title;
    public static String ReleaserBuilderWizardSelectionPage_warning_sameVersion;
    public static String ProductReleaserBuilderWizard_exception_unsavedChanges;
    public static String StatusPage_copyToClipboardActionText;
    public static String UsernamePasswordDialog_title;
    public static String UsernamePasswordDialog_password;
    public static String UsernamePasswordDialog_prompt;
    public static String UsernamePasswordDialog_savePassword;
    public static String UsernamePasswordDialog_username;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do not initialize
    }
}
