/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.tablestructure.ColumnRange;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;

public class UniqueKeyValidatorTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableContents table;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Tc");
    }

    public void testUniqueKeysTwoRanges() throws CoreException {
        ITableContentsGeneration gen1 = createTwoRangeTable("T1");

        createRow(gen1, new String[] { "7", "7", "1", "999" });
        createRow(gen1, new String[] { "1", "2", "500", "999" });
        createRow(gen1, new String[] { "1", "30", "1", "999" });

        MessageList messageList = null;
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    public void testUniqueKeysTwoRangesSameFrom() throws CoreException {
        ITableContentsGeneration gen1 = createTwoRangeTable("T2");
        createRow(gen1, new String[] { "405", "405", "101", "101" });
        createRow(gen1, new String[] { "1", "1", "111", "111" });
        createRow(gen1, new String[] { "2", "2", "1", "999" });
        createRow(gen1, new String[] { "405", "405", "1", "999" });
        createRow(gen1, new String[] { "3", "3", "1", "999" });
        createRow(gen1, new String[] { "4", "4", "1", "999" });

        // 405 in last ColumnRange
        MessageList messageList = null;
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    private ITableContentsGeneration createTwoRangeTable(String name) throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, name);
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("start1");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("end1");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("start2");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("end2");
        column.setDatatype("Integer");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        // range 1
        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("start1");
        range.setToColumn("end1");
        ((ColumnRange)range).setParameterName("start1-end1");
        uniqueKey.addKeyItem(range.getName());
        // range 2
        range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("start2");
        range.setToColumn("end2");
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("start1");
        table.newColumn("end1");
        table.newColumn("start2");
        table.newColumn("end2");

        return gen1;
    }

    public void testUniqueKeysMultipleKeys() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");
        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem("a");
        uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem("b");

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");

        // valid contents
        IRow row1 = gen1.newRow();
        IRow row2 = gen1.newRow();
        row1.setValue(0, "1");
        row1.setValue(1, "1");
        row2.setValue(0, "2");
        row2.setValue(1, "2");

        MessageList messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // invalid contents (unique violation)
        row2.setValue(0, "1");
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        row2.setValue(1, "1");
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(4, messageList.getNoOfMessages());

    }

    /**
     * Test with one unique key: column key item or range with only from or to column specified (not
     * two column range)
     */
    public void testUniqueKeysOneKey() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        //
        // test with one column as unique key
        //

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("age");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("rate");
        column.setDatatype("String");
        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem("age");

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("1");
        table.newColumn("2");

        // valid contents
        IRow row1 = gen1.newRow();
        IRow row2 = gen1.newRow();
        row1.setValue(0, "0");
        row1.setValue(1, "test");
        row2.setValue(0, "1");
        row2.setValue(1, "test2");

        MessageList messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // invalid contents (unique violation)
        row2.setValue(0, "0");
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        row2.setValue(0, "2");
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        //
        // test with one column (column range 'from' column) as unique key
        //

        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        range.setFromColumn("age");
        uniqueKey.removeKeyItem("age");
        uniqueKey.addKeyItem(range.getName());

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        row2.setValue(0, "0");
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        row2.setValue(0, "1");
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        uniqueKey.removeKeyItem(range.getName());

        //
        // test with one column (column range 'to' column) as unique key
        //
        range.setColumnRangeType(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        range.setToColumn("age");
        range.setFromColumn("");
        uniqueKey.addKeyItem(range.getName());

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        row2.setValue(0, "0");
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        row2.setValue(0, "1");
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    public void testUniqueKeysOneKeyTwoColumnRange() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");

        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("2b");

        MessageList messageList = null;

        // valid contents
        IRow row1 = gen1.newRow();
        IRow row2 = gen1.newRow();
        row1.setValue(0, "10");
        row1.setValue(1, "19");
        row2.setValue(0, "20");
        row2.setValue(1, "29");

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // invalid contents (to column - unique violation)
        row1.setValue(1, "20");

        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        // valid contents
        row2.setValue(0, "21");

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(0, messageList.getNoOfMessages());

        // invalid contents (from column - unique violation)
        row2.setValue(0, "19");

        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        // invalid contents (from column equal - unique violation)
        row1.setValue(0, "10");
        row1.setValue(1, "19");
        row2.setValue(0, "10");
        row2.setValue(1, "29");

        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());
    }

    /**
     * Test validation message TO_MANY_UNIQUE_KEY_VIOLATIONS, if there are to many unique key
     * validation then the validation aborts and a further validation message:
     * "TO_MANY_UNIQUE_KEY_VIOLATIONS" will be added.
     */
    public void testToManyUniqueKeyTwoColumnRangeViolation() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");

        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");

        MessageList messageList = null;

        // valid contents
        for (int i = 1; i < 50; i++) {
            createRow(gen1, new String[] { "1", "10" });
        }

        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_TO_MANY_UNIQUE_KEY_VIOLATIONS));
    }

    public void testUniqueKeysMultipleKeyTwoColumnRanges() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("aString");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("bString");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("c");
        column.setDatatype("Decimal");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        // column key enty
        uniqueKey.addKeyItem("c");
        // range 1
        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");
        ((ColumnRange)range).setParameterName("aBisB");
        uniqueKey.addKeyItem(range.getName());
        // range 2
        range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("aString");
        range.setToColumn("bString");
        // TODO Joerg: setParameterName ins Public Interface Aufnehmen?
        ((ColumnRange)range).setParameterName("aStringBisBString");
        uniqueKey.addKeyItem(range.getName());

        // precondition valid structure, all Datatypes found etc.
        structure.validate(project);
        assertEquals(0, structure.validate(project).getNoOfMessages());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("aString");
        table.newColumn("bString");
        table.newColumn("c");

        // valid contents
        IRow row1 = gen1.newRow();
        IRow row2 = gen1.newRow();
        // row 1
        row1.setValue(0, "10");
        row1.setValue(1, "19");
        row1.setValue(2, "2008-01-01");
        row1.setValue(3, "2008-12-31");
        row1.setValue(4, "1.23");
        // row 2
        row2.setValue(0, "20");
        row2.setValue(1, "29");
        row2.setValue(2, "2009-01-01");
        row2.setValue(3, "2009-12-31");
        row2.setValue(4, "1.23");

        MessageList messageList = null;

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // valid contents: invalid first range but second range valid
        row1.setValue(1, "21");

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // valid contents: valid first range, invalid second range
        row1.setValue(1, "19");
        row2.setValue(2, "2008-08-01");

        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // invalid contents: both ranges overlap
        row1.setValue(1, "21");

        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());

        // valid contents, both ranges overlap but different column key
        row2.setValue(4, "1.24");
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    /**
     * Test ranges, unique key is first column and 2 two-column-range's 1 column key: 1 column 1
     * range key: from=2 column, to=3 column 2 range key: from=4 column, to=5 column
     */
    public void testRanges() throws CoreException {
        assertRangeCollision("1", new String[][] { new String[] { "a", "1", "9", "10", "19" },
                new String[] { "a", "10", "19", "10", "19" }, new String[] { "a", "20", "29", "10", "19" } }, false);
        assertRangeCollision("2", new String[][] { new String[] { "a", "1", "9", "10", "19" },
                new String[] { "a", "10", "19", "10", "19" }, new String[] { "a", "2", "29", "10", "19" } }, true);
        assertRangeCollision("3", new String[][] { new String[] { "a", "1", "9", "10", "19" },
                new String[] { "b", "10", "19", "10", "19" }, new String[] { "a", "2", "29", "10", "19" } }, true);
        assertRangeCollision("4", new String[][] { new String[] { "a", "1", "9", "10", "19" },
                new String[] { "b", "10", "19", "10", "19" }, new String[] { "a", "2", "29", "10", "19" } }, true);
        assertRangeCollision("5", new String[][] { new String[] { "a", "1", "19", "10", "19" },
                new String[] { "b", "10", "19", "10", "19" }, new String[] { "a", "11", "29", "10", "19" } }, true);
        assertRangeCollision("6", new String[][] { new String[] { "a", "1", "19", "10", "19" },
                new String[] { "b", "10", "19", "10", "19" }, new String[] { "a", "1", "29", "10", "19" } }, true);
        assertRangeCollision("7", new String[][] { new String[] { "a", "1", "19", "10", "19" },
                new String[] { "a", "20", "29", "20", "29" }, new String[] { "a", "30", "39", "30", "39" },
                new String[] { "a", "1", "19", "40", "49" } }, false);
        assertRangeCollision("8", new String[][] { //
                new String[] { "a", "1", "19", "10", "19" }, //
                        new String[] { "a", "20", "29", "20", "29" }, //
                        new String[] { "a", "30", "39", "30", "39" }, //
                        new String[] { "a", "1", "19", "20", "29" } }, //
                false);
        assertRangeCollision("9", new String[][] { new String[] { "a", "1", "19", "10", "19" },
                new String[] { "a", "20", "29", "20", "29" }, new String[] { "a", "30", "39", "30", "39" },
                new String[] { "a", "1", "19", "10", "19" } }, true);
        assertRangeCollision("10", new String[][] { new String[] { "a", "1", "19", "10", "19" },
                new String[] { "a", "20", "29", "40", "49" }, new String[] { "a", "30", "39", "30", "39" },
                new String[] { "a", "1", "19", "30", "39" }, new String[] { "a", "1", "19", "40", "49" } }, false);
    }

    private void assertRangeCollision(String testName, String[][] rows, boolean collision) throws CoreException {
        setUpPerformanceTest(testName);

        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = null;
        column = structure.newColumn();
        column.setName("a");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("c");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("d");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("e");
        column.setDatatype("int");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("b");
        range.setToColumn("c");
        uniqueKey.addKeyItem("a");
        uniqueKey.addKeyItem(range.getName());
        range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("d");
        range.setToColumn("e");
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("c");
        table.newColumn("d");
        table.newColumn("e");

        for (String[] row : rows) {
            createRow(gen1, row);
        }

        MessageList messageList = table.validate(project);
        assertEquals(testName, collision,
                messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION) != null);

        tearDownPerformanceTest();
    }

    /**
     * Test two 'two column'-ranges, first range is equal
     */
    public void testIsRangeCollision() throws CoreException {
        assertSecondRangeCollision("1", 10, 19, 20, 29, false);

        assertSecondRangeCollision("2", 10, 19, 10, 19, true);
        assertSecondRangeCollision("3", 10, 19, 10, 29, true);
        assertSecondRangeCollision("4", 10, 19, 1, 19, true);

        assertSecondRangeCollision("5", 10, 19, 18, 29, true);
        assertSecondRangeCollision("6", 10, 19, 1, 11, true);
        assertSecondRangeCollision("7", 10, 19, 1, 20, true);
        assertSecondRangeCollision("8", 21, 22, 20, 29, true);
    }

    private void assertSecondRangeCollision(String testName, int from1, int to1, int from2, int to2, boolean collision)
            throws CoreException {
        setUpPerformanceTest(testName);
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("c");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("d");
        column.setDatatype("int");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");
        uniqueKey.addKeyItem(range.getName());
        range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("c");
        range.setToColumn("d");
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("c");
        table.newColumn("d");

        MessageList messageList = null;

        // valid contents
        IRow row1 = gen1.newRow();
        IRow row2 = gen1.newRow();
        row1.setValue(0, "1");
        row1.setValue(1, "2");
        row1.setValue(2, "" + from1);
        row1.setValue(3, "" + to1);
        row2.setValue(0, "1");
        row2.setValue(1, "2");
        row2.setValue(2, "" + from2);
        row2.setValue(3, "" + to2);

        messageList = table.validate(project);
        assertEquals(testName, collision,
                messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION) != null);
        tearDownPerformanceTest();
    }

    private ValueDatatype[] findColumnDatatypes(ITableStructure structure, IIpsProject ipsProject) throws CoreException {
        if (structure == null) {
            return new ValueDatatype[0];
        }
        IColumn[] columns = structure.getColumns();
        ValueDatatype[] datatypes = new ValueDatatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = columns[i].findValueDatatype(ipsProject);
        }
        return datatypes;
    }

    public void _testKeyValueRangePerformance() throws Exception {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("int");

        ColumnRange range = (ColumnRange)structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("a");
        range.setToColumn("b");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("a");
        table.newColumn("b");

        Row row = (Row)gen1.newRow();
        row.setValue(0, "1");
        row.setValue(0, "10");

        ValueDatatype[] datatypes = findColumnDatatypes(structure, project);

        for (int i = 0; i < 1000; i++) {
            KeyValueRange.createKeyValue(structure, datatypes, uniqueKey, row, range);
        }
        long sum = 0;
        int max = 1000;
        for (int i = 0; i < max; i++) {
            long start = System.currentTimeMillis();
            KeyValueRange keyValueRange = KeyValueRange.createKeyValue(structure, datatypes, uniqueKey, row, range);
            keyValueRange.isParsable();
            sum += System.currentTimeMillis() - start;
        }

        System.out.println((double)sum / max);
    }

    public void _testPerformance() throws Exception {
        StringBuffer sb = new StringBuffer();

        int noOfRunsBefore = 0;
        int noOfTests = 0;
        boolean testWithUniqueKeyValidation = true;

        noOfRunsBefore = 100;
        noOfTests = 20;

        testWithUniqueKeyValidation = true;
        sb.append("" + noOfRunsBefore + ", " + noOfTests + ", " + testWithUniqueKeyValidation + "\n");
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 1000, testWithUniqueKeyValidation);
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 2000, testWithUniqueKeyValidation);
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 5000, testWithUniqueKeyValidation);
        noOfRunsBefore = 10;
        noOfTests = 5;
        sb.append("" + noOfRunsBefore + ", " + noOfTests + ", " + testWithUniqueKeyValidation + "\n");
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 10000, testWithUniqueKeyValidation);
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 20000, testWithUniqueKeyValidation);
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 25000, testWithUniqueKeyValidation);
        testPerformanceInternal(sb, noOfRunsBefore, noOfTests, 30000, testWithUniqueKeyValidation);

        System.out.println(sb.toString());
    }

    /**
     * Test Scenario: TableStructure: mandant (String), ageMin (int), ageMax (int), Unique Key with
     * mandant, ageMin-ageMax Table: 1. Row: 1, 10, 19 2. Row: 2, 20, 25 3. Row: 3, 20, 30 Steps: 1.
     * validate after creation: validaion errors (row 2 and 3) 2. validate after changing row 3
     * ageMin to '10': validation error (row 1 and 3) 3. validate after changing row 3 ageMin to
     * '20': validation error (row 2 and 3) 4. validate after changing row 3 mandant to '2': no
     * validation error
     */
    public void testKeyValueRangeWithSameFromKeyAfter2ndChange() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("mandant");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("ageMin");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("ageMax");
        column.setDatatype("int");

        ColumnRange range = (ColumnRange)structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageMin");
        range.setToColumn("ageMax");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem("mandant");
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        Row row = (Row)gen1.newRow();
        row.setValue(0, "1");
        row.setValue(1, "10");
        row.setValue(2, "19");
        row = (Row)gen1.newRow();
        row.setValue(0, "1");
        row.setValue(1, "20");
        row.setValue(2, "25");
        row = (Row)gen1.newRow();
        row.setValue(0, "1");
        row.setValue(1, "20");
        row.setValue(2, "30");

        // Step 1
        MessageList messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());
        assertRowInValidationMsg(messageList, 2);
        assertRowInValidationMsg(messageList, 3);

        // Step 2
        row.setValue(1, "10"); // ageMin
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());
        assertRowInValidationMsg(messageList, 1);
        assertRowInValidationMsg(messageList, 3);

        // Step 3
        row.setValue(1, "20"); // ageMin
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());
        assertRowInValidationMsg(messageList, 2);
        assertRowInValidationMsg(messageList, 3);

        // Step 4
        row.setValue(0, "2"); // mandant
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    private void assertRowInValidationMsg(MessageList messageList, int row) throws CoreException {
        assertEquals(table.getNumOfGenerations(), 1);
        assertEquals(1, messageList.getMessagesFor(((IRow[])table.getGeneration(0).getChildren())[row - 1])
                .getNoOfMessages());
    }

    /**
     * Test Scenario: TableStructure: mandant (String), ageMin (int), ageMax (int), Unique Key with
     * mandant, ageMin-ageMax Table: 1. Row: 1, 10, 20 2. Row: 2, 10, 15 Steps: 1. validate after
     * creation: no validaion errors 2. validate after changing row 2 mandant to '1': two validation
     * error 3. validate after changing row 2 mandant to '2': no validation error again
     */
    public void testKeyValueRangeWithSameFromKeyAfterChange() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("mandant");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("ageMin");
        column.setDatatype("int");
        column = structure.newColumn();
        column.setName("ageMax");
        column.setDatatype("int");

        ColumnRange range = (ColumnRange)structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageMin");
        range.setToColumn("ageMax");

        IUniqueKey uniqueKey = structure.newUniqueKey();
        uniqueKey.addKeyItem("mandant");
        uniqueKey.addKeyItem(range.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        Row row = (Row)gen1.newRow();
        row.setValue(0, "1");
        row.setValue(1, "10");
        row.setValue(2, "20");
        row = (Row)gen1.newRow();
        row.setValue(0, "2");
        row.setValue(1, "10");
        row.setValue(2, "15");

        // Step 1
        MessageList messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));

        // Step 2
        row.setValue(0, "1"); // mandant
        messageList = table.validate(project);
        assertNotNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
        assertEquals(2, messageList.getNoOfMessages());
        assertRowInValidationMsg(messageList, 1);
        assertRowInValidationMsg(messageList, 2);

        // Step 3
        row.setValue(0, "2"); // mandant
        messageList = table.validate(project);
        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    private void testPerformanceInternal(StringBuffer sb,
            int noOfRunsBefore,
            int noOfTests,
            int noOfRows,
            boolean testWithUniqueKeyValidation) throws Exception {
        int noOfRowStartup = 100;

        for (int i = 0; i < noOfRunsBefore; i++) {
            setUpPerformanceTest("tmp" + i);
            testValidateUniqueKeyPerformance(noOfRowStartup, testWithUniqueKeyValidation, new InitAndValidateTime());
            tearDownPerformanceTest();
        }

        long sumOfInit = 0;
        long sumOfValidation = 0;
        for (int i = 0; i < noOfTests; i++) {
            setUpPerformanceTest("" + i);
            InitAndValidateTime initAndValidateTime = new InitAndValidateTime();
            testValidateUniqueKeyPerformance(noOfRows, testWithUniqueKeyValidation, initAndValidateTime);
            sumOfInit += initAndValidateTime.getDurrationInit();
            sumOfValidation += initAndValidateTime.getDurrationValidate();
            tearDownPerformanceTest();
        }

        sb.append(noOfRows + "\n");
        sb.append(("" + (((double)sumOfInit / noOfTests) / 1000)).replaceAll("\\.", ",") + "\n");
        sb.append(("" + (((double)sumOfValidation / noOfTests) / 1000)).replaceAll("\\.", ",") + "\n");

        System.out.println(sb.toString());
    }

    private void setUpPerformanceTest(String suffix) throws CoreException {
        project = newIpsProject("TestProject" + suffix);
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Tc" + suffix);
    }

    private void tearDownPerformanceTest() throws CoreException {
        project.getProject().close(null);
        project.getProject().delete(true, true, null);
    }

    private class InitAndValidateTime {
        private long durrationInit;
        private long durrationValidate;

        public long getDurrationInit() {
            return durrationInit;
        }

        public void setDurrationInit(long durrationInit) {
            this.durrationInit = durrationInit;
        }

        public long getDurrationValidate() {
            return durrationValidate;
        }

        public void setDurrationValidate(long durrationValidate) {
            this.durrationValidate = durrationValidate;
        }
    }

    public void testValidateUniqueKey() throws CoreException {
        InitAndValidateTime time = new InitAndValidateTime();
        testValidateUniqueKeyPerformance(1, true, time);
    }

    private void testValidateUniqueKeyPerformance(int noOfRows,
            boolean withUniqueKeyValidation,
            InitAndValidateTime resultTime) throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table.setTableStructure(structure.getQualifiedName());

        // init table structure
        IColumn column = structure.newColumn();
        column.setName("a");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("b");
        column.setDatatype("Integer");
        column = structure.newColumn();
        column.setName("aString");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("bString");
        column.setDatatype("String");
        column = structure.newColumn();
        column.setName("c");
        column.setDatatype("Decimal");

        IUniqueKey uniqueKey = structure.newUniqueKey();

        uniqueKey.addKeyItem("c");
        IColumnRange newRange = structure.newRange();
        newRange.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        newRange.setFromColumn("a");
        newRange.setToColumn("b");
        uniqueKey.addKeyItem(newRange.getName());
        newRange = structure.newRange();
        newRange.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        newRange.setFromColumn("aString");
        newRange.setToColumn("bString");
        uniqueKey.addKeyItem(newRange.getName());

        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        if (!withUniqueKeyValidation) {
            ((TableContentsGeneration)gen1).initUniqueKeyValidator(null, null);
        }
        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("aString");
        table.newColumn("bString");
        table.newColumn("c");

        int noOfTestRows = noOfRows;
        GregorianCalendar calendarFrom = DateUtil.parseIsoDateStringToGregorianCalendar("1900-01-01");
        GregorianCalendar calendarTo = DateUtil.parseIsoDateStringToGregorianCalendar("1900-01-02");
        long start = System.currentTimeMillis();
        for (int i = 1; i < noOfTestRows + 1; i++) {
            if (i % 1000 == 1) {
            }
            int from = i * 10;
            createRow(gen1, new String[] { "" + from, "" + (from + 9),
                    DateUtil.gregorianCalendarToIsoDateString(calendarFrom),
                    DateUtil.gregorianCalendarToIsoDateString(calendarTo), "" + i });
            calendarFrom.add(Calendar.DATE, 2);
            calendarTo.add(Calendar.DATE, 2);
        }
        resultTime.setDurrationInit(System.currentTimeMillis() - start);

        MessageList messageList = null;
        start = System.currentTimeMillis();
        messageList = table.validate(project);
        resultTime.setDurrationValidate(System.currentTimeMillis() - start);

        assertNull(messageList.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION));
    }

    private IRow createRow(ITableContentsGeneration gen, String[] values) {
        IRow row = gen.newRow();
        for (int i = 0; i < values.length; i++) {
            row.setValue(i, values[i]);
        }
        return row;
    }
}
