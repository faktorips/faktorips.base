/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

/**
 * A unique key is a list of key items that, given a value for each item, you can find exactly one
 * row in the table is belongs to or no none.
 */
public interface IUniqueKey extends IKey {

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_ENUM_TABLE_ID_KEY = "EnumTableIdKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_ENUM_TABLE_NAME_KEY = "EnumTableNameKeyOnlyOneItem"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_ENUM_TABLE_NAME_KEY_DATATYPE = "EnumTableNameKeyMustBeString"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_TOO_LESS_ITEMS = "TooLessItems"; //$NON-NLS-1$

    /**
     * Identifies a validation rule.
     */
    public final static String MSGCODE_KEY_ITEM_MISMATCH = "KeyItemMismatch"; //$NON-NLS-1$

    /**
     * The name of the unique key is the concatenation of it's items separated by a comma and a
     * space character (<code>", "</code>).
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

}
