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

public interface IRow extends IIpsObjectPart {

    /**
     * Value-Property.
     */
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this row does not define values in columns a unique
     * key of its table dictates.
     */
    public final static String MSGCODE_UNDEFINED_UNIQUEKEY_VALUE = "UNDEFINED_UNIQUE_KEY_VALUE"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value in a row does not match its column's
     * datatype (is not parsable).
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = "VALUE_NOT_PARSABLE"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_VALID_NAME_WHEN_TABLE_ENUM_TYPE = "ValidNameWhenEnumTypeContent"; //$NON-NLS-1$

    /**
     * Identifies that in case of an existing two column unique key ('from'- and 'to'-column) the
     * 'from'-column is greater that the 'to'-column
     */
    public final static String MSGCODE_UNIQUE_KEY_FROM_COlUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE = "UniqueKeyFromVolumnValueIsGreaterToColumnValue"; //$NON-NLS-1$

    /**
     * Returns the row number as string.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    @Override
    public String getName();

    /**
     * Returns the table contents this row belongs to.
     */
    public ITableContents getTableContents();

    /**
     * Returns the number of the row in the table. First row has number 0.
     */
    public int getRowNumber();

    /**
     * Returns the value for the indicated column index.
     * 
     * @param column The column index.
     * 
     * @throws IllegalArgumentException if the row does no contain a cell for the indicated column
     *             index.
     */
    public String getValue(int column);

    /**
     * Sets the value for the indicated column index.
     * 
     * @param column The column index.
     * @param newValue The new value as string.
     * 
     * @throws IndexOutOfBoundsException if the row does no contain a cell for the indicated column
     *             index.
     */
    public void setValue(int column, String newValue);

}
