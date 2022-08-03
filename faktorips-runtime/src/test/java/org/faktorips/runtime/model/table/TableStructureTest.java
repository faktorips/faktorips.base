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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsTableColumn;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.type.Deprecation;
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

        List<String> names = Arrays.asList("company", "Gender", "rate");
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

        assertEquals(companyColumn, tableStructure.getColumn("Company"));
        assertEquals("Gender", tableStructure.getColumn("Gender").getName());
    }

    @Test
    public void testGetColumnnames() {
        List<String> names = Arrays.asList("company", "Gender", "rate");
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

    static class DeprecatedTableRow {

        @IpsTableColumn(name = "aColumn")
        public int getAColumn() {
            return 1;
        }

        @Deprecated
        @IpsTableColumn(name = "deprecatedColumn")
        public int getDeprecatedColumn() {
            return -1;
        }

    }

    @Test
    public void testIsDeprecated() {
        assertThat(IpsModel.getTableStructure(TestTable.class).isDeprecated(), is(false));
        assertThat(IpsModel.getTableStructure(DeprecatedTable.class).isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecated() {
        assertThat(IpsModel.getTableStructure(TestTable.class).getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = IpsModel.getTableStructure(DeprecatedTable.class).getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @IpsTableStructure(name = "tables.DeprecatedTable", type = TableStructureKind.MULTIPLE_CONTENTS, columns = {
            "aColumn", "deprecatedColumn" })
    @Deprecated
    static class DeprecatedTable extends Table<DeprecatedTableRow> {

        @Override
        protected void addRow(List<String> columns, IRuntimeRepository productRepository) {
            // ignore
        }

        @Override
        protected void initKeyMaps() {
            // ignore
        }

    }
}
