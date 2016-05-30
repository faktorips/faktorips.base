package org.faktorips.runtime.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.table.TableModel;
import org.junit.Test;

public class ModelsTest {

    @Test
    public void testGetTableModel() {
        TableModel model = Models.getTableModel(TestTable.class);

        assertNotNull(model);
        assertEquals("tables.TestTable", model.getName());
    }

    @Test
    public void testGetTableModelByInstance() {
        TableModel model = Models.getTableModel(new TestTable());

        assertNotNull(model);
        assertEquals("tables.TestTable", model.getName());
    }
}
