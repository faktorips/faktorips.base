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

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.tablestructure.ColumnRange;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * A key value range is an object which stores the 'from'-value for a unique key of a table row. The
 * unique key must contain at least one two column range key item.
 */
public class KeyValueRange extends AbstractKeyValue implements Comparable<KeyValueRange> {

    /** reference to the column range this key value is created for */
    private final ColumnRange columnRange;

    /** contains the value datatype (cached) of the 'from' and 'to' column */
    private final ValueDatatype valueDatatype;

    /**
     * only the 'from' value is stored, because this value is used as key within the sorted map the
     * 'to'-value will be evaluated (using the range and row object) if necessary (see evalToValue)
     */
    private String valueFrom;

    private KeyValueRange(ITableStructure structure, ValueDatatype[] datatypes, IIndex uniqueKey,
            ColumnRange columnRange, Row row) {

        super(structure, uniqueKey, row);
        this.columnRange = columnRange;
        this.valueDatatype = getValueDatatypeOfColumnRange(structure, datatypes, columnRange);
        this.valueFrom = evalValueFrom(row, columnRange);
    }

    /**
     * Creates a new key value range object of an a given unique key for a given row. The unique key
     * must contain at least one column range. The key value range object will be created using the
     * given column range.
     */
    public static KeyValueRange createKeyValue(ITableStructure structure,
            ValueDatatype[] datatypes,
            IIndex uniqueKey,
            Row row,
            ColumnRange columnRange) {

        KeyValueRange keyValue = new KeyValueRange(structure, datatypes, uniqueKey, columnRange, row);
        return keyValue;
    }

    /**
     * Returns the key value. The key value of a key value range object is the 'from'-column-value.
     */
    @Override
    protected String getKeyValue() {
        return getValueFrom();
    }

    @Override
    public boolean isValid(Row row) {
        return isValid(valueFrom, evalValueFrom(row, columnRange)) && isToGreaterOrEqualFrom();
    }

    /**
     * Returns <code>true</code> if the 'from'- and 'to'-value are parsable. For instance the values
     * are not parsable if the table contents row is erroneous.
     * 
     * @see ValueDatatype#isParsable(String)
     */
    public boolean isParsable() {
        if (valueDatatype == null) {
            return false;
        }
        return valueDatatype.isParsable(valueFrom) && valueDatatype.isParsable(evalValueTo(getRow(), columnRange));
    }

    /**
     * Returns the 'to'-value of the column range of the row this key value range was created for.
     */
    public String getValueTo() {
        return evalValueTo(getRow(), columnRange);
    }

    /**
     * Returns the stored 'from'-value of the column range. Note that the from value could be
     * another than the current column value in the row.
     * 
     * @see #isValid()
     */
    public String getValueFrom() {
        return valueFrom;
    }

    /**
     * Returns <code>true</code> if the to value is greater than the from value.
     */
    private boolean isToGreaterOrEqualFrom() {
        return compareTo(evalValueTo(getRow(), columnRange), valueFrom) >= 0;
    }

    /**
     * Returns <code>true</code> if the 'from'-value is less or equal compared to the given value.
     */
    public boolean isFromLessOrEqual(String value) {
        return compareTo(valueFrom, value) <= 0;
    }

    private boolean isValid(String value1, String value2) {
        if (StringUtils.isEmpty(value1) || StringUtils.isEmpty(value2)) {
            return false;
        }
        return value1.equals(value2);
    }

    /**
     * Returns the value of the 'from' column of the range this key value range object belongs to.
     * The 'from' value will be read using the given row and column range. Note that if a row has
     * changed the evalValueFrom() method can return a different value as getKeyValue() method. (see
     * also #isValid())
     */
    private String evalValueFrom(Row row, ColumnRange columnRange) {
        return evalValue(getStructure(), row, columnRange.getFromColumn());
    }

    /**
     * Returns the value of the 'from' column of the range this key value range object belongs to.
     * The 'to' value will be read using the given row and column range.
     */
    private String evalValueTo(Row row, ColumnRange columnRange) {
        return evalValue(getStructure(), row, columnRange.getToColumn());
    }

    /**
     * Compares the two key value range objects. Note that a compare uses only the 'from'-value.
     */
    @Override
    public int compareTo(KeyValueRange keyValueRange) {
        return compareTo(getKeyValue(), keyValueRange.getKeyValue());
    }

    private int compareTo(String value1, String value2) {
        return compareTo(valueDatatype, value1, value2);
    }

    private static int compareTo(ValueDatatype valueDatatype, String value1, String value2) {
        return valueDatatype.compare(value1, value2);
    }

    /**
     * Returns the value datatype of the column range this key value range belongs to
     */
    private static ValueDatatype getValueDatatypeOfColumnRange(ITableStructure structure,
            ValueDatatype[] datatypes,
            ColumnRange columnRange) {

        if (datatypes != null) {
            try {
                int columnIndex = structure.getColumnIndex(columnRange.getFromColumn());
                if (columnIndex <= datatypes.length) {
                    return datatypes[columnIndex];
                } else {
                    IpsLog.log(new IpsStatus("Datatype of column " + columnRange.getFromColumn() + " not found!")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } catch (IpsException e) {
                IpsLog.log(new IpsStatus("Column " + columnRange.getFromColumn() + " not found!")); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }
        }
        return null;
    }

    private static String evalValue(ITableStructure tableStructure, Row row, String columnName) {
        return row.getValue(tableStructure.getColumnIndex(columnName));
    }

    public static boolean isRangeCollision(ITableStructure tableStructure,
            ValueDatatype[] valueDatatypes,
            ColumnRange otherColumnRange,
            Row row,
            Row otherRow) {

        String fromColumnName = otherColumnRange.getFromColumn();
        String toColumnName = otherColumnRange.getToColumn();
        String from = evalValue(tableStructure, row, fromColumnName);
        String otherTo = evalValue(tableStructure, otherRow, toColumnName);
        ValueDatatype valueDatatype = getValueDatatypeOfColumnRange(tableStructure, valueDatatypes, otherColumnRange);

        if (compareTo(valueDatatype, otherTo, from) < 0) {
            return false;
        }

        String otherFrom = evalValue(tableStructure, otherRow, fromColumnName);
        String to = evalValue(tableStructure, row, toColumnName);

        if (compareTo(valueDatatype, to, otherFrom) < 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns the key value range object as string, note that the 'from' value is stored value in
     * this object and the to value is the current value of the corresponding column in the table
     * contents row.
     */
    @Override
    public String toString() {
        return getUniqueKey().getName() + ": " + valueFrom + "- /" + evalValueTo(getRow(), columnRange); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
