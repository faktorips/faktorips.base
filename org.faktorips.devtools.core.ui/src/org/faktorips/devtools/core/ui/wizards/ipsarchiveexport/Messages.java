/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsarchiveexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.ipsarchiveexport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsArchivePackageWizardPage_Title;
    public static String IpsArchiveExportWizard_Export;
    public static String IpsArchivePackageWizardPage_Description_EnterDestination;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaSources;
    public static String IpsArchivePackageWizardPage_Label_IncludeJavaBinaries;
    public static String IpsArchivePackageWizardPage_Label_Target;
    public static String IpsArchivePackageWizardPage_Label_Browse;
    public static String IpsArchivePackageWizardPage_Description_EnterValidDestination;
    public static String IpsArchivePackageWizardPage_Description_DefineWhichResource;
    public static String IpsArchivePackageWizardPage_Description;
    public static String IpsArchivePackageWizardPage_WarningNoIpsProjectSelected;

}
