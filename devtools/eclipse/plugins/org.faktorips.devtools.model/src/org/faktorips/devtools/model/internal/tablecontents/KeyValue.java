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

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A key value is an object which stores the value (string) of the content of all key item column
 * for a unique key of a table row. This object stores only the values of none two column range key
 * items.
 */
public class KeyValue extends AbstractKeyValue {

    private final String value;

    public KeyValue(ITableStructure structure, IIndex uniqueKey, Row row) {
        super(structure, uniqueKey, row);
        value = evalValue(row);
    }

    /**
     * Creates a new key value object of an a given unique key for a given row.
     */
    public static KeyValue createKeyValue(ITableStructure structure, IIndex uniqueKey, Row row) {
        return new KeyValue(structure, uniqueKey, row);
    }

    /**
     * The key value is the string representation of all key items columns in the table contents
     * row.
     */
    @Override
    protected String getKeyValue() {
        return getValue();
    }

    @Override
    public boolean isValid(Row row) {
        String valueNew = evalValue(row);
        if (IpsStringUtils.isEmpty(valueNew) || IpsStringUtils.isEmpty(getValue())) {
            return false;
        }
        return getValue().equals(valueNew);
    }

    private String evalValue(Row row) {
        String[] values;
        List<IKeyItem> keyItems = getNonTwoColumnRangeKeyItems(getUniqueKey());

        values = new String[keyItems.size()];
        for (int i = 0; i < keyItems.size(); i++) {
            values[i] = getValueForKeyItem(getStructure(), row, keyItems.get(i));
        }
        return Arrays.toString(values);
    }

    private static String getValueForKeyItem(ITableStructure structure, Row row, IKeyItem keyItem) {
        IColumn[] columns = keyItem.getColumns();
        String value = ""; //$NON-NLS-1$
        for (int i = 0; i < columns.length; i++) {
            int columnIndex = structure.getColumnIndex(columns[i]);
            if (columnIndex >= row.getNoOfColumns()) {
                // invalid table contents
                continue;
            }
            value += i > 0 ? "#" : ""; //$NON-NLS-1$ //$NON-NLS-2$
            value += row.getValue(columnIndex);
        }
        return value;
    }

    @Override
    public String toString() {
        return getUniqueKey().getName() + ": " + getValue(); //$NON-NLS-1$
    }

    public String getValue() {
        return value;
    }

}
