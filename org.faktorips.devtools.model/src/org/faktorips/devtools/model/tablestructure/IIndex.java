/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

import java.util.List;

import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

/**
 * An index is a list of key items that, given a value for each item, you can find zero, one or many
 * rows in the table. An index can be marked as unique key. There can be only one (or zero) rows for
 * a unique key in a table.
 */
public interface IIndex extends IKey, IVersionControlledElement {

    String PROPERTY_UNIQUE_KEY = "uniqueKey"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_ENUM_TABLE_ID_KEY = "EnumTableIdKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_ENUM_TABLE_NAME_KEY = "EnumTableNameKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_ENUM_TABLE_NAME_KEY_DATATYPE = "EnumTableNameKeyMustBeString"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_TOO_LESS_ITEMS = "TooLessItems"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    String MSGCODE_KEY_ITEM_MISMATCH = "KeyItemMismatch"; //$NON-NLS-1$

    /**
     * The name of the index is the concatenation of it's items separated by a comma and a space
     * character (<code>", "</code>).
     * 
     * @see org.faktorips.devtools.model.IIpsElement#getName()
     */
    @Override
    String getName();

    /**
     * Returns true if the key contains any ranges.
     */
    boolean containsRanges();

    /**
     * Returns <code>true</code> if the key contains ranges with two columns.
     * 
     * @see ColumnRangeType#TWO_COLUMN_RANGE
     */
    boolean containsTwoColumnRanges();

    /**
     * Returns <code>true</code> if the key contains any columns.
     */
    boolean containsColumns();

    /**
     * Returns <code>true</code> if the key contains only ranges.
     */
    boolean containsRangesOnly();

    /**
     * Returns a list with datatypes the key is based on.
     */
    List<String> getDatatypes();

    /**
     * Returns <code>true</code> if this index is marked as unique key, false if it is no unique key
     * and hence it supports multiple rows with the same index value.
     * 
     * @return <code>true</code> if this index is a unique key, <code>false</code> if not.
     */
    boolean isUniqueKey();

    /**
     * Setting the property unique key. Set this property to <code>true</code> if this index should
     * be a unique key. Set to <code>false</code> if is should be a non-unique key.
     * 
     * @param unique <code>true</code> to set this index to be a unique key.
     */
    void setUniqueKey(boolean unique);

}
