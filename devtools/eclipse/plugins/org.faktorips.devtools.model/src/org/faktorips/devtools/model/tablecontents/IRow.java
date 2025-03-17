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

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

public interface IRow extends IIpsObjectPart {

    /**
     * Value-Property.
     */
    String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this row does not define values in columns a unique
     * key of its table dictates.
     */
    String MSGCODE_UNDEFINED_UNIQUEKEY_VALUE = "UNDEFINED_UNIQUE_KEY_VALUE"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value in a row does not match its column's
     * datatype (is not parsable).
     */
    String MSGCODE_VALUE_NOT_PARSABLE = "VALUE_NOT_PARSABLE"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_VALID_NAME_WHEN_TABLE_ENUM_TYPE = "ValidNameWhenEnumTypeContent"; //$NON-NLS-1$

    /**
     * Identifies that in case of an existing two column unique key ('from'- and 'to'-column) the
     * 'from'-column is greater that the 'to'-column
     */
    String MSGCODE_UNIQUE_KEY_FROM_COLUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE = "UniqueKeyFromVolumnValueIsGreaterToColumnValue"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of values for this row is not matching
     * the number of columns.
     */
    String MSGCODE_NUMBER_OF_VALUES_IS_INVALID = "NUMBER_OF_VALUES_IS_INVALID"; //$NON-NLS-1$

    /**
     * Returns the row number as string.
     * 
     * @see org.faktorips.devtools.model.IIpsElement#getName()
     */
    @Override
    String getName();

    /**
     * Returns the table contents this row belongs to.
     */
    ITableContents getTableContents();

    /**
     * Returns the number of the row in the table. First row has number 0.
     */
    int getRowNumber();

    /**
     * Returns the value for the indicated column index.
     * 
     * @param column The column index.
     * 
     * @throws IllegalArgumentException if the row does no contain a cell for the indicated column
     *             index.
     */
    String getValue(int column);

    /**
     * Sets the value for the indicated column index.
     * 
     * @param column The column index.
     * @param newValue The new value as string.
     * 
     * @throws IndexOutOfBoundsException if the row does no contain a cell for the indicated column
     *             index.
     */
    void setValue(int column, String newValue);

    /**
     * Moves the Value at the given Index up/down by one
     * 
     * @param columnIndex Index of the Value to move
     * @param up Flag indicating whether to move upwards (<code>true</code>) or downwards (
     *            <code>false</code>).
     * @return new Index of the Value
     */
    int moveValue(int columnIndex, boolean up);

    /**
     * Swap two Values with each other
     */
    void swapValue(int firstColumnIndex, int secondColumnIndex);

    void newColumn(int insertAfter, String defaultValue);

}
