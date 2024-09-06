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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.tablestructure.ColumnRange;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.TableContentFormat;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableRowsTest extends AbstractIpsPluginTest {

    private ITableContents table;
    private TableRows tableRows;
    private IIpsProject project;
    private ITableStructure structure;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "StructureTable");
        structure.newColumn();
        structure.newColumn();
        structure.newColumn();
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        table.setTableStructure(structure.getQualifiedName());
        tableRows = (TableRows)table.newTableRows();
        table.newColumn("", "");
        table.newColumn("", "");
        table.newColumn("", "");

        tableRows.getIpsSrcFile().save(null);
    }

    @Test
    public void testGetChildren() {
        int childrenSizeBefore = tableRows.getChildren().length;
        structure.newColumn();
        structure.newColumn();
        table.newColumn("", "");
        table.newColumn("", "");
        tableRows.newRow();
        tableRows.newRow();
        IIpsElement[] children = tableRows.getChildren();
        assertEquals(childrenSizeBefore + 2, children.length);
    }

    @Test
    public void testNewRow() {
        structure.newColumn();
        structure.newColumn();
        table.newColumn("", "");
        table.newColumn("", "");
        IRow row0 = tableRows.newRow();
        String id0 = row0.getId();
        assertNotNull(id0);
        assertEquals(0, row0.getRowNumber());
        assertNull(row0.getValue(0));
        assertNull(row0.getValue(1));

        IRow row1 = tableRows.newRow();
        String id1 = row1.getId();
        assertNotNull(id1);
        assertFalse(id0.equals(id1));
        assertEquals(1, row1.getRowNumber());
    }

    @Test
    public void testNewRow_TableStructure() {
        structure.newColumn();
        structure.newColumn();
        table.newColumn("", "");
        table.newColumn("", "");
        table.getIpsSrcFile().save(new NullProgressMonitor());
        IRow row0 = tableRows.newRow(structure, Optional.of("id23"), Arrays.asList("a", "b"));
        assertThat(row0.getId(), is("id23"));
        assertThat(row0.getRowNumber(), is(0));
        assertThat(row0.getValue(0), is("a"));
        assertThat(row0.getValue(1), is("b"));
        assertThat(table.getIpsSrcFile().isDirty(), is(false));
        tableRows.markAsChanged();
        assertThat(table.getIpsSrcFile().isDirty(), is(true));

        table.getIpsSrcFile().save(new NullProgressMonitor());
        assertThat(table.getIpsSrcFile().isDirty(), is(false));

        IRow row1 = tableRows.newRow(structure, Optional.of("id42"), Arrays.asList("foo", "bar"));
        assertThat(row1.getId(), is("id42"));
        assertThat(row1.getRowNumber(), is(1));
        assertThat(row1.getValue(0), is("foo"));
        assertThat(row1.getValue(1), is("bar"));
        assertThat(table.getIpsSrcFile().isDirty(), is(false));
    }

    @Test
    public void testNewColumn() {
        IRow row1 = tableRows.newRow();
        IRow row2 = tableRows.newRow();
        tableRows.newColumn(3, "a");
        assertEquals("a", row1.getValue(3));
        assertEquals("a", row2.getValue(3));
    }

    @Test
    public void testRemoveColumn() {
        IRow row1 = tableRows.newRow();
        IRow row2 = tableRows.newRow();
        row1.setValue(0, "row1,col1");
        row1.setValue(1, "row1,col2");
        row1.setValue(2, "row1,col3");
        row2.setValue(0, "row2,col1");
        row2.setValue(1, "row2,col2");
        row2.setValue(2, "row2,col3");
        tableRows.removeColumn(1);
        assertEquals("row1,col1", row1.getValue(0));
        assertEquals("row1,col3", row1.getValue(1));
        try {
            row1.getValue(2);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }
        assertEquals("row2,col1", row2.getValue(0));
        assertEquals("row2,col3", row2.getValue(1));
        try {
            row2.getValue(2);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }

    }

    @Test
    public void testToXml_Roundtrip() {
        IRow row1 = newRow("A", "1", "\"ä,;\"");
        IRow row2 = newRow("", null, "|");

        Element element = tableRows.toXml(newDocument());
        row1.delete();
        row2.delete();
        tableRows.initFromXml(element);

        assertThat(tableRows.getNumOfRows(), is(2));
        assertThat(tableRows.getRow(0).getValue(0), is("A"));
        assertThat(tableRows.getRow(0).getValue(1), is("1"));
        assertThat(tableRows.getRow(0).getValue(2), is("\"ä,;\""));
        assertThat(tableRows.getRow(1).getValue(0), is(""));
        assertThat(tableRows.getRow(1).getValue(1), is(nullValue()));
        assertThat(tableRows.getRow(1).getValue(2), is("|"));
    }

    @Test
    public void testToXml_RoundtripWithCVS() {
        setFormatToCsv();
        IRow row1 = newRow("A", "1", "\"ä,;\"");
        IRow row2 = newRow("", null, "|");

        Element element = tableRows.toXml(newDocument());
        row1.delete();
        row2.delete();
        tableRows.initFromXml(element);

        assertThat(tableRows.getNumOfRows(), is(2));
        assertThat(tableRows.getRow(0).getValue(0), is("A"));
        assertThat(tableRows.getRow(0).getValue(1), is("1"));
        assertThat(tableRows.getRow(0).getValue(2), is("\"ä,;\""));
        assertThat(tableRows.getRow(1).getValue(0), is(""));
        assertThat(tableRows.getRow(1).getValue(1), is(nullValue()));
        assertThat(tableRows.getRow(1).getValue(2), is("|"));
    }

    private void setFormatToCsv() {
        IIpsProjectProperties properties = project.getProperties();
        properties.setTableContentFormat(TableContentFormat.CSV);
        project.setProperties(properties);
    }

    @Test
    public void testInitFromXml() {
        tableRows.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(2, tableRows.getNumOfRows());
    }

    @Test
    public void testNewPart() {
        // test rownumber init within newPart()
        IRow row0 = tableRows.newPart(IRow.class);
        String id0 = row0.getId();
        assertNotNull(id0);
        assertEquals(0, row0.getRowNumber());

        IRow row1 = tableRows.newPart(IRow.class);
        String id1 = row1.getId();
        assertNotNull(id1);
        assertFalse(id0.equals(id1));
        assertEquals(1, row1.getRowNumber());

        assertTrue(tableRows.newPart(IRow.class) != null);
    }

    @Test
    public void testClear() {
        tableRows.newRow();
        tableRows.newRow();
        tableRows.clear();
        assertEquals(0, tableRows.getNumOfRows());
    }

    @Test
    public void testGetRow() {
        IRow row1 = tableRows.newRow();
        tableRows.newRow();
        IRow row2 = tableRows.newRow();

        assertEquals(row1, tableRows.getRow(0));
        assertEquals(row2, tableRows.getRow(2));

        assertNull(tableRows.getRow(-1));
        assertNull(tableRows.getRow(42));
    }

    @Test
    public void testGetRowIndex() {
        IRow row = tableRows.newRow();
        assertEquals(0, row.getRowNumber());
        row = tableRows.newRow();
        assertEquals(1, row.getRowNumber());
        row = tableRows.newRow();
        assertEquals(2, row.getRowNumber());
    }

    @Test
    public void testInsertRowAfter() {
        IRow row0 = tableRows.insertRowAfter(999);
        assertEquals(0, row0.getRowNumber());
        assertEquals(true, tableRows.getIpsSrcFile().isDirty());

        IRow row1 = tableRows.newRow();
        assertEquals(1, row1.getRowNumber());
        IRow row2 = tableRows.insertRowAfter(0);
        assertEquals(1, row2.getRowNumber());
        IRow row3 = tableRows.insertRowAfter(0);
        assertEquals(1, row3.getRowNumber());
        assertEquals(2, row2.getRowNumber());
        IRow row4 = tableRows.insertRowAfter(999);
        assertEquals(4, row4.getRowNumber());
    }

    @Test
    public void testIsUniqueKeyValidatedAutomatically() {
        createColumnsWithRangeKey();
        for (int i = 0; i < 5000;) {
            newRow(Integer.toString(i++), Integer.toString(i));
        }
        tableRows.getRow(1).setValue(0, "0");
        tableRows.getRow(1).setValue(1, "1");
        // same as row 0 -> unique key validation

        assertTrue(tableRows.isUniqueKeyValidatedAutomatically());

        newRow("5000", "5001");

        assertFalse(tableRows.isUniqueKeyValidatedAutomatically());
    }

    @Test
    public void testPropertiesToXml_SetsCsvFormatProperty() {
        setFormatToCsv();
        Document document = newDocument();
        Element element = document.createElement(ITableRows.TAG_NAME);

        tableRows.propertiesToXml(element);

        assertThat(element.getAttribute(ITableRows.PROPERTY_FORMAT), is("CSV"));
    }

    @Test
    public void testPropertiesToXml_SetsNoFormatPropertyForXmlFormat() {
        IIpsProjectProperties properties = project.getProperties();
        properties.setTableContentFormat(TableContentFormat.XML);
        project.setProperties(properties);
        Document document = newDocument();
        Element element = document.createElement(ITableRows.TAG_NAME);

        tableRows.propertiesToXml(element);

        assertThat(element.hasAttribute(ITableRows.PROPERTY_FORMAT), is(false));
    }

    @Test
    public void testPartsToXml_writesCsv() {
        setFormatToCsv();
        Document document = newDocument();
        Element element = document.createElement(ITableRows.TAG_NAME);
        newRow("A", "1", "\"ä,;\"");
        newRow("", null, "|");

        tableRows.partsToXml(document, element);

        assertThat(element.hasChildNodes(), is(true));
        String textContent = element.getTextContent();
        assertThat(textContent, is("A|1|\"\\\"ä,;\\\"\"\n" +
                "|\"\\\\N\"|\"|\"\n"));
    }

    private IRow newRow(String... values) {
        IRow row = tableRows.newRow();
        for (int i = 0; i < values.length; i++) {
            row.setValue(i, values[i]);
        }
        return row;
    }

    private void createColumnsWithRangeKey() {
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("c");
        column.setDatatype("String");
        structure.getIpsSrcFile().save(null);
        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");
        IIndex uniqueKey = structure.newIndex();
        uniqueKey.addKeyItem(range.getName());
        // validation is now done by references
        table.newColumn("", "a");
        table.newColumn("", "b");
        table.newColumn("", "c");
        table.validate(project);
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile_ColumnRange() throws Exception {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start testDirectChangesToColumnRange() =====");
        }
        IIpsSrcFile ipsFile = structure.getIpsSrcFile();
        IColumnRange columnRange = structure.newRange();
        columnRange.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        columnRange.setFromColumn("foo");
        columnRange.setToColumn("bar");
        ipsFile.save(null);

        String encoding = structure.getIpsProject().getXmlFileCharset();
        AFile file = structure.getIpsSrcFile().getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("foo", "newFoo").replace("bar", "newBar");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);

        structure = (ITableStructure)ipsFile.getIpsObject(); // forces a reload
        ColumnRange range = (ColumnRange)structure.getRanges()[0];

        assertThat(range.isDeleted(), is(false));
        assertThat(range.getFromColumn(), is("newFoo"));
        assertThat(range.getToColumn(), is("newBar"));
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished testDirectChangesToColumnRange() =====");
        }
    }

}
