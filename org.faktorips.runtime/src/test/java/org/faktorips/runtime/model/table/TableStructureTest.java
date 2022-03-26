/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;

public class TableStructureTest {

    private TableStructure tableStructure;

    @Before
    public void setUp() {
        tableStructure = new TableStructure(TestTable.class);
    }

    @Test
    public void testName() {
        assertEquals("tables.TestTable", tableStructure.getName());
    }

    @Test
    public void testType() {
        assertEquals(TableStructureKind.MULTIPLE_CONTENTS, tableStructure.getKind());
    }

    @Test
    public void testGetColumns() {
        List<TableColumn> columns = tableStructure.getColumns();

        assertNotNull(columns);
        assertEquals(3, columns.size());

        List<String> names = Arrays.asList("company", "gender", "rate");
        List<Class<?>> datatypes = Arrays.asList(String.class, Integer.class, Decimal.class);

        for (int i = 0; i <= 2; i++) {
            assertEquals(names.get(i), columns.get(i).getName());
            assertEquals(datatypes.get(i), columns.get(i).getDatatype());
        }
    }

    @Test
    public void testGetColumn() {
        TableColumn companyColumn = tableStructure.getColumn("company");
        assertTrue(companyColumn != null);
        assertEquals("company", companyColumn.getName());
        assertEquals(String.class, companyColumn.getDatatype());

        assertTrue(tableStructure.getColumn("Company") == null);
    }

    @Test
    public void testGetColumnnames() {
        List<String> names = Arrays.asList("company", "gender", "rate");
        assertEquals(names, tableStructure.getColumnNames());
    }

    @Test
    public void testGetValues() {
        TestTableRow row = new TestTableRow("F10", 3, Decimal.valueOf(28.2));
        List<Object> values = tableStructure.getValues(row);

        List<Object> should = new ArrayList<>();
        should.add("F10");
        should.add(3);
        should.add(Decimal.valueOf(28.2));

        assertEquals(should, values);
    }
}