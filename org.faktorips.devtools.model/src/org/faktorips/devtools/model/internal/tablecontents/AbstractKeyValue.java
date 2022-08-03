/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.internal.tablestructure.ColumnRange;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * Abstract basis class of all kind of key values (e.g. column 'only' key values or 'range' key
 * values). A key value is an object which stores the value of an unique key of a specific table
 * row.
 */
public abstract class AbstractKeyValue {

    /** reference to the table structure this key value belongs to */
    private final ITableStructure structure;

    /** reference to the unique key this key value belongs to */
    private final IIndex uniqueKey;

    /** reference to the row this key value belongs to */
    private final Row row;

    protected AbstractKeyValue(ITableStructure structure, IIndex uniqueKey, Row row) {
        this.structure = structure;
        this.uniqueKey = uniqueKey;
        this.row = row;
    }

    /**
     * Helper method, returns all non two column range key items: column key items or column range
     * key items with type ONE_COLUMN_RANGE_FROM or ONE_COLUMN_RANGE_TO
     */
    protected static List<IKeyItem> getNonTwoColumnRangeKeyItems(IIndex uniqueKey) {
        IKeyItem[] keyItems = uniqueKey.getKeyItems();
        List<IKeyItem> result = new ArrayList<>(keyItems.length);
        for (IKeyItem keyItem : keyItems) {
            if (keyItem instanceof IColumnRange) {
                if (ColumnRangeType.TWO_COLUMN_RANGE.equals(((IColumnRange)keyItem).getColumnRangeType())) {
                    continue;
                }
            }
            result.add(keyItem);
        }
        return result;
    }

    /**
     * Helper method, returns a list of all two column ranges inside the given unique key
     */
    protected static List<ColumnRange> getTwoColumnRanges(IIndex uniqueKey) {
        IKeyItem[] keyItems = uniqueKey.getKeyItems();
        List<ColumnRange> columnRanges = new ArrayList<>();
        for (IKeyItem keyItem : keyItems) {
            if (keyItem instanceof ColumnRange) {
                if (!ColumnRangeType.TWO_COLUMN_RANGE.equals(((ColumnRange)keyItem).getColumnRangeType())) {
                    continue;
                }
                columnRanges.add((ColumnRange)keyItem);
            }
        }
        return columnRanges;
    }

    /**
     * Returns the table structure this key value belongs to.
     */
    public ITableStructure getStructure() {
        return structure;
    }

    /**
     * Returns the row this key value belongs to.
     */
    public Row getRow() {
        return row;
    }

    /**
     * Returns the unique key this key value belongs to
     */
    public IIndex getUniqueKey() {
        return uniqueKey;
    }

    /**
     * Returns <code>true</code> if the key value is valid using the row it was created for.
     * 
     * @see #isValid(Row)
     */
    public boolean isValid() {
        return isValid(row);
    }

    /**
     * Returns <code>true</code> if the key value is valid otherwise <code>false</code>. The key
     * value is valid if the stored key value is equal to the key value which will be computed using
     * the given row. In other words: if a key value was created using a specific row, and
     * afterwards this row has changed the column - which is used within an unique key - then the
     * key value will be invalid (obsolete).
     */
    public abstract boolean isValid(Row row);

    /**
     * Returns the key value of the key value object. The key value is the computed String value of
     * an unique key for a specific row.
     */
    protected abstract String getKeyValue();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        return prime * result + getKeyValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        AbstractKeyValue other = (AbstractKeyValue)obj;
        if (!getKeyValue().equals(other.getKeyValue())) {
            return false;
        }
        return true;
    }
}
