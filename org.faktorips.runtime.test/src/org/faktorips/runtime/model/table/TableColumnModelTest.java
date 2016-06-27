package org.faktorips.runtime.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
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

    /**
     * <strong>Scenario:</strong><br>
     * The table row class provides getter methods for columns that are not annotated in the table
     * class.
     * 
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Illegal state Exception with the message that column annotated in the getter method is not
     * declared as column in the table class.
     */
    @Test
    public void testCreateModelsFromNotDeclared() throws IllegalStateException {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("is not listed as column in the @" + IpsTableStructure.class.getSimpleName()
                + " annotation");
        TableColumnModel.createModelsFrom(tableModel, Arrays.asList(new String[] { "company" }), TestTableRow.class);
    }

    /**
     * <strong>Scenario:</strong><br>
     * The table class is annotated with a column ("troll") that does not have a matching getter in
     * the row class.
     * 
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Illegal state Exception with the message that no getter could be found for the annotated
     * column
     */
    @Test
    public void testCreateModelsFromNoGetter() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter method found for annotated column \"troll\"");
        TableColumnModel.createModelsFrom(tableModel,
                Arrays.asList(new String[] { "company", "gender", "rate", "troll" }), TestTableRow.class);
    }

    /**
     * <strong>Scenario:</strong><br>
     * The table class is annotated with multiple columns ("troll", "ips") that does not have a
     * matching getter in the row class.
     * 
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Illegal state Exception with the message that no getter could be found for the annotated
     * columns
     */
    @Test
    public void testCreateModelsFromNoGetterMult() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("No getter methods found for annotated columns \"troll\", \"ips\"");
        TableColumnModel.createModelsFrom(tableModel,
                Arrays.asList(new String[] { "company", "gender", "rate", "troll", "ips" }), TestTableRow.class);
    }
}
