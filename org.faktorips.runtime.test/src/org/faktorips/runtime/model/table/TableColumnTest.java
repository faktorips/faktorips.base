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
import static org.junit.Assert.assertTrue;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.values.Decimal;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TableColumnTest {

    private final TableStructure tableStructure = IpsModel.getTableStructure(TestTable.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetValue() throws NoSuchMethodException, SecurityException {
        TableColumn columnModel = new TableColumn(tableStructure, "gender", Integer.class,
                TestTableRow.class.getDeclaredMethod("getGender"));

        TestTableRow row = new TestTableRow("F10", 3, Decimal.valueOf(28.2));
        Object value = columnModel.getValue(row);

        assertTrue(value != null);
        assertTrue(value instanceof Integer);
        assertEquals(3, value);
    }
}
