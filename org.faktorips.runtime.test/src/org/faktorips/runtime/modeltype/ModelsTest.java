package org.faktorips.runtime.modeltype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.faktorips.runtime.internal.TestTable;
import org.junit.Test;

public class ModelsTest {

    @Test
    public void testGetTableModel() {
        TableModel model = Models.getTableModel(TestTable.class);

        assertNotNull(model);
        assertEquals("org.faktorips.runtime.internal.TestTable", model.getName());
    }

    @Test
    public void testGetTableModelByInstance() {
        TableModel model = Models.getTableModel(new TestTable());

        assertNotNull(model);
        assertEquals("org.faktorips.runtime.internal.TestTable", model.getName());
    }
}
