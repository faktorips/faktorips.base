/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;

/**
 * A key value is an object which stores the value (string) of the content of all key item column
 * for a unique key of a table row. This object stores only the values of none two column range key
 * items.
 */
public class KeyValue extends AbstractKeyValue {

    protected String value;

    public KeyValue(ITableStructure structure, IUniqueKey uniqueKey, Row row) {
        super(structure, uniqueKey, row);
        this.value = evalValue(row);
    }

    /**
     * Creates a new key value object of an a given unique key for a given row.
     */
    public static KeyValue createKeyValue(ITableStructure structure, IUniqueKey uniqueKey, Row row) {
        KeyValue keyValue = new KeyValue(structure, uniqueKey, row);
        return keyValue;
    }

    /**
     * The key value is the string representation of all key items columns in the table contents
     * row.
     */
    @Override
    protected String getKeyValue() {
        return value;
    }

    @Override
    public boolean isValid(Row row) {
        String valueNew = evalValue(row);
        if (StringUtils.isEmpty(valueNew) || StringUtils.isEmpty(value)) {
            return false;
        }
        return value.equals(valueNew);
    }

    private String evalValue(Row row) {
        return evalValue(structure, uniqueKey, row);
    }

    private String evalValue(ITableStructure structure, IUniqueKey uniqueKey, Row row) {
        String[] values;
        List<IKeyItem> keyItems = getNonTwoColumnRangeKeyItems(uniqueKey);

        values = new String[keyItems.size()];
        for (int i = 0; i < keyItems.size(); i++) {
            values[i] = getValueForKeyItem(structure, row, keyItems.get(i));
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
        return uniqueKey.getName() + ": " + value; //$NON-NLS-1$
    }
}
