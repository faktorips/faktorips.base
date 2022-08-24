/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablecontents;

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.tablecontents.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AbstractXlsTableImportOperation_errRead;
    public static String TableExportOperation_labelMonitorTitle;
    public static String TableExportOperation_errStructureNotFound;
    public static String TableExportOperation_errWrite;
    public static String TableExportOperation_errStructureTooMuchColumns;
    public static String ExcelTableImportOperation_labelImportFile;
    public static String ExcelTableImportOperation_msgImportEscapevalue;

}
