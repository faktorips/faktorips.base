/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

public interface ITableRows extends IIpsObjectPart {

    /**
     * The name of the XML tag used if this object is saved to XML.
     */
    public static final String TAG_NAME = "Rows"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TABLECONTENTSGENERATION-"; //$NON-NLS-1$

    /**
     * Returns the rows that make up the table.
     */
    public IRow[] getRows();

    /**
     * Returns the row of the table at the given index (the first element has the index 0). Returns
     * null if the given index is out of bounds (less than zero or greater or equal than the number
     * of rows).
     */
    public IRow getRow(int rowIndex);

    /**
     * Returns the number of rows in the table.
     */
    public int getNumOfRows();

    /**
     * Creates a new row.
     */
    public IRow newRow();

    /**
     * Creates a new row after the given row index.<br>
     * If the index is greater than the number of rows the row will be added at the end.
     */
    public IRow insertRowAfter(int rowIndex);

    /**
     * removes all rows
     */
    public void clear();

}
