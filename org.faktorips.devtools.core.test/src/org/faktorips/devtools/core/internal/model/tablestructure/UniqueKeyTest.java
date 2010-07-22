/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class UniqueKeyTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private UniqueKey key;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        key = (UniqueKey)table.newUniqueKey();
        ipsSrcFile.save(true, null);
    }

    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfUniqueKeys());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testGetName() {
        assertEquals("", key.getName());
        key.setKeyItems(new String[] { "age" });
        assertEquals("age", key.getName());
        key.setKeyItems(new String[] { "age", "gender" });
        assertEquals("age, gender", key.getName());
    }

    public void testGetKeyItemNames() {
        assertEquals(0, key.getKeyItemNames().length);
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

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

    public void testSetKeyItems() {
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertTrue(ipsSrcFile.isDirty());
    }

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

    public void testToXml() {
        key = (UniqueKey)table.newUniqueKey();
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        Element element = key.toXml(newDocument());
        UniqueKey copy = new UniqueKey();
        copy.initFromXml(element);
        assertEquals(key.getId(), copy.getId());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", key.getId());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    /**
     * Tests for the correct type of exception to be thrown - no part of any type could ever be
     * created.
     */
    public void testNewPart() {
        try {
            key.newPart(IPolicyCmptTypeAttribute.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }

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
