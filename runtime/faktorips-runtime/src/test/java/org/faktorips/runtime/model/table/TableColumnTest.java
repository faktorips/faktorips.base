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
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.faktorips.runtime.internal.TestTable;
import org.faktorips.runtime.internal.TestTableRow;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class TableColumnTest {

    private final TableStructure tableStructure = IpsModel.getTableStructure(TestTable.class);
    @SuppressWarnings("deprecation")
    private final TableStructure deprecatedTableStructure = IpsModel
            .getTableStructure(TableStructureTest.DeprecatedTable.class);

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

    @Test
    public void testIsDeprecated() {
        assertThat(deprecatedTableStructure.getColumn("aColumn").isDeprecated(), is(false));
        assertThat(deprecatedTableStructure.getColumn("deprecatedColumn").isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecated() {
        assertThat(deprecatedTableStructure.getColumn("aColumn").getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = deprecatedTableStructure.getColumn("deprecatedColumn").getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }
}
