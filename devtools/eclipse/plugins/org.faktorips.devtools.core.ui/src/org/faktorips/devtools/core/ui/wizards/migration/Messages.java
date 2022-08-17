/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.migration.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProjectSelectionPage_titleSelectProjects;
    public static String ProjectSelectionPage_buttonSelectAll;
    public static String ProjectSelectionPage_buttonDeselectAll;
    public static String MigrationPage_titleMigrationOperations;
    public static String MigrationPage_labelHeader;
    public static String MigrationPage_labelError;
    public static String MigrationWizard_title;
    public static String MigrationWizard_titleAbortion;
    public static String MigrationWizard_msgAbortion;
    public static String MigrationWizard_titleError;
    public static String MigrationWizard_msgError;
    public static String ProjectSelectionPage_msgNoProjects;
    public static String ProjectSelectionPage_msgSelectProjects;
    public static String MigrationPage_msgShortDescription;
    public static String MigrationPage_titleProject;
}
