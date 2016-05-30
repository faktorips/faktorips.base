package org.faktorips.runtime.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.table.TableColumnModel;
import org.faktorips.runtime.model.table.TableModel;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

public class TableModelTest {

    private TableModel tableModel;

    @Before
    public void setUp() {
        tableModel = new TableModel(TestTable.class);
    }

    @Test
    public void testName() {
        assertEquals("tables.TestTable", tableModel.getName());
    }

    @Test
    public void testType() {
        assertEquals(TableStructureType.MULTIPLE_CONTENTS, tableModel.getType());
    }

    @Test
    public void testGetColumns() {
        List<TableColumnModel> columns = tableModel.getColumns();

        assertNotNull(columns);
        assertEquals(3, columns.size());

        List<String> names = Arrays.asList(new String[] { "company", "gender", "rate" });
        List<Class<?>> datatypes = Arrays.asList(new Class<?>[] { String.class, Integer.class, Decimal.class });

        for (int i = 0; i <= 2; i++) {
            assertEquals(names.get(i), columns.get(i).getName());
            assertEquals(datatypes.get(i), columns.get(i).getDatatype());
        }
    }

    @Test
    public void testGetColumn() {
        TableColumnModel companyColumn = tableModel.getColumn("company");
        assertTrue(companyColumn != null);
        assertEquals("company", companyColumn.getName());
        assertEquals(String.class, companyColumn.getDatatype());

        assertTrue(tableModel.getColumn("Company") == null);
    }

    @Test
    public void testGetColumnnames() {
        List<String> names = Arrays.asList(new String[] { "company", "gender", "rate" });
        assertEquals(names, tableModel.getColumnNames());
    }

    @Test
    public void testGetValues() {
        TestTableRow row = new TestTableRow("F10", 3, Decimal.valueOf(28.2));
        List<Object> values = tableModel.getValues(row);

        List<Object> should = new ArrayList<Object>();
        should.add("F10");
        should.add(3);
        should.add(Decimal.valueOf(28.2));

        assertEquals(should, values);
    }
}