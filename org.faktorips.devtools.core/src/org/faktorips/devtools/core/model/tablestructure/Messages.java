/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.tablestructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ColumnRangeType_fromColumnOnly;
    public static String ColumnRangeType_toColumnOnly;
    public static String ColumnRangeType_twoColumns;

}
