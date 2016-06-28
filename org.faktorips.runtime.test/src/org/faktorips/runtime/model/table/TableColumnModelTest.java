package org.faktorips.runtime.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.Models;
import org.faktorips.values.Decimal;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TableColumnModelTest {

    private final TableModel tableModel = Models.getTableModel(TestTable.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetValue() throws NoSuchMethodException, SecurityException {
        TableColumnModel columnModel = new TableColumnModel(tableModel, "gender", Integer.class,
                TestTableRow.class.getDeclaredMethod("getGender"));

        TestTableRow row = new TestTableRow("F10", 3, Decimal.valueOf(28.2));
        Object value = columnModel.getValue(row);

        assertTrue(value != null);
        assertTrue(value instanceof Integer);
        assertEquals(3, value);
    }
}
