/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class TableSorterTest extends AbstractIpsPluginTest {

    private IRow rowValid;
    private IRow rowInvalid;
    private IRow rowNull;

    private TableSorter sorter = new TableSorter();
    private ITableContents tableContents;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject("TableContentsLabelProviderProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];

        ITableStructure structure = (ITableStructure)newIpsObject(root, IpsObjectType.TABLE_STRUCTURE,
                "TestTableStructure");
        IColumn column0 = structure.newColumn();
        column0.setDatatype("Integer");
        IColumn column1 = structure.newColumn();
        column1.setDatatype("Integer");
        IColumn column2 = structure.newColumn();
        column2.setDatatype("Integer");

        tableContents = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "TestTableContents");
        tableContents.setTableStructure(structure.getQualifiedName());
        ITableRows gen = tableContents.newTableRows();
        rowValid = gen.newRow();
        rowInvalid = gen.newRow();
        rowNull = gen.newRow();

        tableContents.newColumn("1", "");
        tableContents.newColumn("2", "");
        tableContents.newColumn("3", "");

        rowValid.setValue(0, "1");
        rowValid.setValue(1, "2");
        rowValid.setValue(2, "3");
        rowInvalid.setValue(0, "A");
        rowInvalid.setValue(1, "B");
        rowInvalid.setValue(2, "C");
        rowNull.setValue(0, null);
        rowNull.setValue(1, null);
        rowNull.setValue(2, null);
    }

    @Test
    public void testCompareViewerObjectObject() {
        assertEquals(0, sorter.compare(null, rowValid, rowValid));

        /*
         * Order: rowValid rowInvalid rowNull
         */
        assertEquals(-1, sorter.compare(null, rowValid, rowInvalid));
        assertEquals(-2, sorter.compare(null, rowValid, rowNull));
        assertEquals(1, sorter.compare(null, rowNull, rowInvalid));

        rowInvalid.delete();
        IRow newRow = tableContents.getTableRows().newRow();

        /*
         * Order: rowValid rowNull newRow
         */
        assertEquals(-1, sorter.compare(null, rowValid, rowNull));
        assertEquals(-2, sorter.compare(null, rowValid, newRow));
        assertEquals(1, sorter.compare(null, newRow, rowNull));
    }

}
