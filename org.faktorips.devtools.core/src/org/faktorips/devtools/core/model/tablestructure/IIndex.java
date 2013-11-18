/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import java.util.List;

/**
 * An index is a list of key items that, given a value for each item, you can find one or many rows
 * in the table is belongs to or no none. An index could be marked as unique key. Is it is unique,
 * there is exactly one row for every key.
 */
public interface IIndex extends IKey {

    public static final String PROPERTY_UNIQUE_KEY = "uniqueKey"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public static final String MSGCODE_ENUM_TABLE_ID_KEY = "EnumTableIdKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public static final String MSGCODE_ENUM_TABLE_NAME_KEY = "EnumTableNameKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public static final String MSGCODE_ENUM_TABLE_NAME_KEY_DATATYPE = "EnumTableNameKeyMustBeString"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public static final String MSGCODE_TOO_LESS_ITEMS = "TooLessItems"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public static final String MSGCODE_KEY_ITEM_MISMATCH = "KeyItemMismatch"; //$NON-NLS-1$

    /**
     * The name of the index is the concatenation of it's items separated by a comma and a space
     * character (<code>", "</code>).
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    @Override
    public String getName();

    /**
     * Returns true if the key contains any ranges.
     */
    public boolean containsRanges();

    /**
     * Returns <code>true</code> if the key contains ranges with two columns.
     * 
     * @see ColumnRangeType#TWO_COLUMN_RANGE
     */
    public boolean containsTwoColumnRanges();

    /**
     * Returns <code>true</code> if the key contains any columns.
     */
    public boolean containsColumns();

    /**
     * Returns <code>true</code> if the key contains only ranges.
     */
    public boolean containsRangesOnly();

    /**
     * Returns a list with datatypes the key is based on.
     */
    public List<String> getDatatypes();

    /**
     * Returns <code>true</code> if this index is marked as unique key, false if it is no unique key
     * and hence it supports multiple rows with the same index value.
     * 
     * @return <code>true</code> if this index is a unique key, <code>false</code> if not.
     */
    public boolean isUniqueKey();

    /**
     * Setting the property unique key. Set this property to <code>true</code> if this index should
     * be a unique key. Set to <code>false</code> if is should be a non-unique key.
     * 
     * @param unique <code>true</code> to set this index to be a unique key.
     */
    public void setUniqueKey(boolean unique);

}
