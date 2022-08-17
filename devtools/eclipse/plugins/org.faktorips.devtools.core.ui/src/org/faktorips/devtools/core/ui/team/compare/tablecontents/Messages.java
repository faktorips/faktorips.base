/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.team.compare.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TableContentsCompareItem_TableStructure;
    public static String TableContentsCompareItem_SrcFile;
    public static String TableContentsCompareItem_Row;
    public static String TableContentsCompareItem_rows;
    public static String TableContentsCompareItem_TableContents;
    public static String TableContentsCompareViewer_TableContentsCompare;
    public static String TableContentsCompareItemCreator_TableContentsStructureCompare;

}
