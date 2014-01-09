/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.tableexport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TableExport_title;
    public static String TableExportPage_title;
    public static String TableExportPage_labelContents;
    public static String TableExportPage_msgContentsEmpty;
    public static String TableExportPage_msgNonExisitingContents;
    public static String TableExportWizard_msgFileExistsTitle;
    public static String TableExportWizard_msgFileExists;
    public static String TableExportWizard_operationName;
    public static String TableExportPage_msgStructureNotValid;

}
