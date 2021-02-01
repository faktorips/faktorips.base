/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class RowImplTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private ITableContents table;
    private ITableRows generation;
    private Row row;
    private Row row2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable");
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        table.setTableStructure(structure.getQualifiedName());
        generation = table.newTableRows();
        structure.newColumn();
        structure.newColumn();
        structure.newColumn();
        table.newColumn(null, "");
        table.newColumn(null, "");
        table.newColumn(null, "");
        row = (Row)generation.newRow();
        row2 = (Row)generation.newRow();
        ipsSrcFile = table.getIpsSrcFile();
    }

    @Test
    public void testSetValue() {
        row.setValue(0, "newValue0");
        assertEquals("newValue0", row.getValue(0));
        row.setValue(1, "newValue1");
        assertEquals("newValue1", row.getValue(1));
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test(expected = RuntimeException.class)
    public void testSetValue_Exception() {
        row.setValue(4, "newValue2");
    }

    @Test
    public void testRemove() {
        // Rownumbers before delete
        assertEquals(0, row.getRowNumber());
        assertEquals(1, row2.getRowNumber());

        row.delete();
        assertEquals(1, generation.getNumOfRows());
        assertTrue(ipsSrcFile.isDirty());

        // Rownumber after delete
        assertEquals(0, row2.getRowNumber());
    }

    @Test
    public void testToXml() {
        row.setValue(0, "value0");
        row.setValue(1, "");
        row.setValue(2, null);

        Element element = row.toXml(newDocument());
        row.setValue(0, null);
        row.setValue(1, null);
        row.setValue(2, "");
        row.initFromXml(element);
        assertEquals("value0", row.getValue(0));
        assertEquals("", row.getValue(1));
        assertNull(row.getValue(2));
    }

    @Test
    public void testInitFromXml() {
        row.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", row.getId());
        assertEquals("0.15", row.getValue(0));
        assertEquals("", row.getValue(1));
        assertNull(row.getValue(2));
    }

    @Test
    public void testGetRowNumber() {
        assertEquals(0, row.getRowNumber());
        assertEquals(1, row2.getRowNumber());
        row.delete();
        assertEquals(0, row2.getRowNumber());
    }
}
