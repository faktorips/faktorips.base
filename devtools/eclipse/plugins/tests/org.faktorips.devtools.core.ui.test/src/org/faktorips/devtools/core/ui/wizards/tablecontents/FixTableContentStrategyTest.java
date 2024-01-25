/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FixTableContentStrategyTest extends AbstractIpsPluginTest {

    @Mock
    private AssignContentAttributesPage<ITableStructure, IColumn> assignTableAttributesPage;

    private FixTableContentStrategy tableStrategy;
    private IIpsProject project;
    private TableContents table;
    private ITableStructure structure;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        structure.newColumn();
        structure.newColumn();
        table = newTableContents(structure, "Tc");
        IColumn column_ID = structure.newColumn();
        column_ID.setName("Id");
        IColumn column_Name = structure.newColumn();
        column_Name.setName("Name");
        table.newColumn("", "");
        table.newColumn("", "");
        tableStrategy = new FixTableContentStrategy(table);

        ITableRows rows = table.newTableRows();
        IRow firstRow = rows.newRow();
        firstRow.setValue(0, "m");
        firstRow.setValue(1, "male");
        IRow secondtRow = rows.newRow();
        secondtRow.setValue(0, "w");
        secondtRow.setValue(1, "female");
    }

    @Test
    public void testGetContentValuesCount() {
        assertEquals(2, tableStrategy.getContentValuesCount());
    }

    @Test
    public void testDeleteObsoleteContentAttributeValues() {
        Integer[] notAssignedColumnsList = { 1 };
        when(assignTableAttributesPage.getCurrentlyNotAssignedColumns())
                .thenReturn(Arrays.asList(notAssignedColumnsList));
        tableStrategy.deleteObsoleteContentAttributeValues(assignTableAttributesPage);
        // column_ID is the first column in the table and gets deleted (m | w). New first column
        // is now column_Name(male | female)
        assertEquals("male", table.getTableRows().getRow(0).getValue(0));
        assertEquals("female", table.getTableRows().getRow(1).getValue(0));
    }

    @Test
    public void testCreateNewContentAttributeValues() {
        int[] notAssignedColumnsList = { 1, 2, 0 };
        when(assignTableAttributesPage.getColumnOrder()).thenReturn(notAssignedColumnsList);
        IColumn newColumn = structure.newColumn();
        newColumn.setName("Deutsch");
        tableStrategy.createNewContentAttributeValues(assignTableAttributesPage);

        assertEquals(3, table.getColumnReferencesCount());
        assertEquals("", table.getTableRows().getRow(0).getValue(2));
        assertEquals("", table.getTableRows().getRow(1).getValue(2));
    }

    @Test
    public void testCreateNewContentAttributeValues_DefaultNull() {
        int[] notAssignedColumnsList = { 1, 2, 0 };
        when(assignTableAttributesPage.getColumnOrder()).thenReturn(notAssignedColumnsList);
        when(assignTableAttributesPage.isFillNewColumnsWithNull()).thenReturn(true);
        IColumn newColumn = structure.newColumn();
        newColumn.setName("Deutsch");
        tableStrategy.createNewContentAttributeValues(assignTableAttributesPage);

        assertEquals(3, table.getColumnReferencesCount());
        assertNull(table.getTableRows().getRow(0).getValue(2));
        assertNull(table.getTableRows().getRow(1).getValue(2));
    }

    @Test
    public void testMoveAttributeValues() {
        tableStrategy.moveAttributeValues(new int[] { 2, 1 });
        assertEquals("male", table.getTableRows().getRow(0).getValue(0));
        assertEquals("m", table.getTableRows().getRow(0).getValue(1));
        assertEquals("female", table.getTableRows().getRow(1).getValue(0));
        assertEquals("w", table.getTableRows().getRow(1).getValue(1));
    }

}
