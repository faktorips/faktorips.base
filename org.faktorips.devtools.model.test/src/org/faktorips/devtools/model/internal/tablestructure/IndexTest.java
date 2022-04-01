/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKey;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class IndexTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private Index key;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        key = (Index)table.newIndex();
        ipsSrcFile.save(null);
    }

    @Test
    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfUniqueKeys());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testGetName() {
        assertEquals("", key.getName());
        key.setKeyItems(new String[] { "age" });
        assertEquals("age", key.getName());
        key.setKeyItems(new String[] { "age", "gender" });
        assertEquals("age, gender", key.getName());
    }

    @Test
    public void testGetKeyItemNames() {
        assertEquals(0, key.getKeyItemNames().length);
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    @Test
    public void testGetKeyItems() {
        assertEquals(0, key.getKeyItems().length);
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);

        key.setKeyItems(new String[] { "c0", range.getName(), "unknown" });
        IKeyItem[] keyItems = key.getKeyItems();
        assertEquals(2, keyItems.length);
        assertEquals(c0, keyItems[0]);
        assertEquals(range, keyItems[1]);
    }

    @Test
    public void testSetKeyItems() {
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testGetKeyItemAt() {
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumn c2 = table.newColumn();
        c2.setName("c2");
        key.setKeyItems(new String[] { "c0", "c1" });
        IKeyItem[] keyItems = key.getKeyItems();
        assertEquals(2, keyItems.length);
        IKeyItem item = key.getKeyItemAt(1);
        assertEquals("c1", item.getName());

        try {
            key.getKeyItemAt(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // do nothing
        }

    }

    @Test
    public void testGetNameOfKeyItemAt() {
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumn c2 = table.newColumn();
        c2.setName("c2");
        key.setKeyItems(new String[] { "c0", "c1" });
        String itemName = key.getNameOfKeyItemAt(1);
        assertEquals("c1", itemName);

        try {
            key.getNameOfKeyItemAt(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // do nothing
        }

    }

    @Test
    public void testGetIndexForKeyItem() throws Exception {
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumn c2 = table.newColumn();
        c2.setName("c2");
        key.setKeyItems(new String[] { "c0", "c1" });
        int index = key.getIndexForKeyItem(key.getKeyItems()[1]);
        assertEquals(1, index);

        TableStructure table2 = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable2");
        IColumn column = table2.newColumn();
        column.setName("t2c0");
        try {
            key.getIndexForKeyItem(column);
            fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }

    }

    @Test
    public void testGetDatatypes() throws Exception {

        IColumn firstString = table.newColumn();
        firstString.setDatatype(Datatype.STRING.getQualifiedName());
        firstString.setName("firstString");

        IColumn secondString = table.newColumn();
        secondString.setDatatype(Datatype.STRING.getQualifiedName());
        secondString.setName("secondString");

        IColumn firstInteger = table.newColumn();
        firstInteger.setDatatype(Datatype.INTEGER.getQualifiedName());
        firstInteger.setName("firstInteger");

        IColumn secondInteger = table.newColumn();
        secondInteger.setDatatype(Datatype.INTEGER.getQualifiedName());
        secondInteger.setName("secondInteger");

        IColumnRange range = table.newRange();
        range.setFromColumn(firstInteger.getName());
        range.setToColumn(secondInteger.getName());
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);

        IIndex firstStringKey = table.newIndex();
        firstStringKey.addKeyItem(firstString.getName());

        assertEquals(Arrays.asList("String"), firstStringKey.getDatatypes());

        IIndex firstIntegerKey = table.newIndex();
        firstIntegerKey.addKeyItem(firstInteger.getName());

        assertEquals(Arrays.asList("Integer"), firstIntegerKey.getDatatypes());

        IIndex rangeKey = table.newIndex();
        rangeKey.addKeyItem(range.getName());

        assertEquals(Arrays.asList("Integer"), firstIntegerKey.getDatatypes());

        IIndex combinedKey = table.newIndex();
        combinedKey.addKeyItem(firstString.getName());
        combinedKey.addKeyItem(firstInteger.getName());

        assertEquals(Arrays.asList("String", "Integer"), combinedKey.getDatatypes());
    }

    @Test
    public void testGetIndexForKeyItemName() {
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumn c2 = table.newColumn();
        c2.setName("c2");
        key.setKeyItems(new String[] { "c0", "c1" });
        int index = key.getIndexForKeyItemName("c1");
        assertEquals(1, index);

        try {
            key.getIndexForKeyItemName("c3");
            fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }

    @Test
    public void testGetItemCandidates() {
        assertEquals(0, key.getItemCandidates().length);
        IColumn columnGender = table.newColumn();
        columnGender.setName("gender");
        IColumn columnSmoker = table.newColumn();
        columnSmoker.setName("smoker");
        IColumnRange range = table.newRange();
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");

        assertEquals(3, key.getItemCandidates().length);
        assertEquals(columnGender, key.getItemCandidates()[0]);
        assertEquals(columnSmoker, key.getItemCandidates()[1]);
        assertEquals(range, key.getItemCandidates()[2]);

        String[] items = new String[] { "gender", range.getName() };
        key.setKeyItems(items);
        assertEquals(1, key.getItemCandidates().length);
        assertEquals(columnSmoker, key.getItemCandidates()[0]);
    }

    @Test
    public void testToXml() {
        key = (Index)table.newIndex();
        key.setUniqueKey(false);
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);

        Element element = key.toXml(newDocument());
        Index copy = new Index();
        copy.initFromXml(element);

        assertEquals(key.getId(), copy.getId());
        assertEquals(false, copy.isUniqueKey());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    @Test
    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());

        assertEquals("42", key.getId());
        assertEquals(false, key.isUniqueKey());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    @Test
    public void testContainsTwoColumnRanges() {
        key.getTableStructure().newColumn().setName("a");
        key.getTableStructure().newColumn().setName("b");

        assertFalse(key.containsTwoColumnRanges());

        IColumnRange range = key.getTableStructure().newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        range.setFromColumn("a");
        key.addKeyItem(range.getName());
        assertFalse(key.containsTwoColumnRanges());

        key.removeKeyItem(range.getName());
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setToColumn("b");
        key.addKeyItem(range.getName());
        assertTrue(key.containsTwoColumnRanges());

        key.removeKeyItem(range.getName());
        assertFalse(key.containsTwoColumnRanges());
    }

    @Test
    public void testValidateThis() throws Exception {
        assertEquals(0, key.getKeyItems().length);
        IColumn c0 = table.newColumn();
        c0.setName("c0");
        IColumn c1 = table.newColumn();
        c1.setName("c1");
        IColumn c2 = table.newColumn();
        c2.setName("c2");
        IColumnRange range = table.newRange();
        range.setFromColumn(c2.getName());
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        key.setKeyItems(new String[] { "c0", range.getName() });
        MessageList msgList = key.validate(project);
        assertTrue(msgList.isEmpty());

        key.setKeyItems(new String[] { range.getName(), "c0" });
        msgList = key.validate(project);
        assertFalse(msgList.isEmpty());
        msgList = msgList.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS);
        assertFalse(msgList.isEmpty());

        // test correct index (column) of message
        key.setKeyItems(new String[] { "c0", range.getName(), "c1" });
        msgList = key.validate(project);
        assertFalse(msgList.isEmpty());
        msgList = msgList.getMessagesFor(key, IKey.PROPERTY_KEY_ITEMS, 1);
        assertFalse(msgList.isEmpty());
    }
}
