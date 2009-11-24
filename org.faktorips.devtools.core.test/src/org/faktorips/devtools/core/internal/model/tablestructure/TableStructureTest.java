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

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 *
 */
public class TableStructureTest extends AbstractIpsPluginTest {

    private TableStructure table;
    private IIpsProject project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
    }

    public void testGetChildren() {
        IColumn c0 = table.newColumn();
        IColumnRange r0 = table.newRange();
        IUniqueKey uk0 = table.newUniqueKey();
        IForeignKey fk0 = table.newForeignKey();

        IIpsElement[] children = table.getChildren();
        assertEquals(4, children.length);
        assertEquals(c0, children[0]);
        assertEquals(r0, children[1]);
        assertEquals(uk0, children[2]);
        assertEquals(fk0, children[3]);
    }

    public void testGetColumns() {
        assertEquals(0, table.getColumns().length);
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();
        assertEquals(2, table.getColumns().length);
        assertEquals(c1, table.getColumns()[0]);
        assertEquals(c2, table.getColumns()[1]);
    }

    public void testGetNumOfColumns() {
        assertEquals(0, table.getNumOfColumns());
        table.newColumn();
        assertEquals(1, table.getNumOfColumns());
        table.newColumn();
        assertEquals(2, table.getNumOfColumns());
    }

    public void testNewColumn() {
        IColumn c = table.newColumn();
        assertEquals(table, c.getParent());
        assertEquals(c, table.getColumns()[0]);
    }

    public void testGetRanges() {
        assertEquals(0, table.getRanges().length);
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();
        assertEquals(2, table.getRanges().length);
        assertEquals(r1, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
    }

    public void testGetNumOfRanges() {
        assertEquals(0, table.getNumOfRanges());
        table.newRange();
        assertEquals(1, table.getNumOfRanges());
        table.newRange();
        assertEquals(2, table.getNumOfRanges());
    }

    public void testNewRange() {
        IColumnRange r = table.newRange();
        assertEquals(table, r.getParent());
        assertEquals(r, table.getRanges()[0]);
    }

    public void testGetColumnsNotInKey() {
        IColumn gender = table.newColumn();
        gender.setName("gender");
        IColumn ageFrom = table.newColumn();
        ageFrom.setName("ageFrom");
        IColumn ageTo = table.newColumn();
        ageTo.setName("ageTo");
        IColumn rate = table.newColumn();
        rate.setName("rate");
        IColumn premium = table.newColumn();
        premium.setName("premium");
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey key = table.newUniqueKey();
        key.setKeyItems(new String[] { gender.getName(), range.getName() });
        IColumn[] columns = table.getColumnsNotInKey(key);
        assertEquals(2, columns.length);
        assertEquals(rate, columns[0]);
        assertEquals(premium, columns[1]);
    }

    public void testGetAccessFunctions() {
        ITableAccessFunction[] fcts = table.getAccessFunctions();
        assertEquals(0, fcts.length);

        IColumn gender = table.newColumn();
        gender.setName("gender");
        gender.setDatatype(Datatype.STRING.getQualifiedName());
        IColumn ageFrom = table.newColumn();
        ageFrom.setName("ageFrom");
        ageFrom.setDatatype(Datatype.INTEGER.getQualifiedName());
        IColumn ageTo = table.newColumn();
        ageTo.setName("ageTo");
        IColumn rate = table.newColumn();
        rate.setName("rate");
        rate.setDatatype(Datatype.DECIMAL.getQualifiedName());
        IColumn premium = table.newColumn();
        premium.setName("minPremium");
        premium.setDatatype(Datatype.MONEY.getQualifiedName());
        IColumnRange range = table.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey key = table.newUniqueKey();
        key.setKeyItems(new String[] { gender.getName(), range.getName() });

        fcts = table.getAccessFunctions();
        assertEquals(2, fcts.length);
        assertSame(table, fcts[0].getTableStructure());
        assertEquals("TestTable.rate", fcts[0].getName());
        assertEquals("rate", fcts[0].getAccessedColumn());
        assertSame(rate, fcts[0].findAccessedColumn());
        assertEquals(rate.getDatatype(), fcts[0].getType());
        String[] argTypes = fcts[0].getArgTypes();
        assertEquals(2, argTypes.length);
        assertEquals(gender.getDatatype(), argTypes[0]);
        assertEquals(range.getDatatype(), argTypes[1]);

        assertEquals("TestTable.minPremium", fcts[1].getName());
    }

    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("blabla", table.getDescription());
        assertTrue(table.isMultipleContentsAllowed());
        assertEquals(3, table.getNumOfColumns());
        assertEquals("ageFrom", table.getColumns()[0].getName());
        assertEquals("ageTo", table.getColumns()[1].getName());
        assertEquals("rate", table.getColumns()[2].getName());

        assertEquals(1, table.getNumOfRanges());
        assertEquals("ageFrom-ageTo", table.getRanges()[0].getName());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        table.setDescription("blabla");
        table.setTableStructureType(TableStructureType.MULTIPLE_CONTENTS);
        IColumn column1 = table.newColumn();
        column1.setName("ageFrom");
        IColumn column2 = table.newColumn();
        column2.setName("ageTo");
        IColumnRange range = table.newRange();
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IUniqueKey uniqueKey = table.newUniqueKey();
        uniqueKey.setKeyItems(new String[] { "ageFrom", "ageTo" });

        Element element = table.toXml(newDocument());
        ITableStructure copy = new TableStructure();
        copy.initFromXml(element);

        assertEquals("blabla", copy.getDescription());
        assertEquals(TableStructureType.MULTIPLE_CONTENTS, copy.getTableStructureType());
        assertEquals(2, copy.getNumOfColumns());
        assertEquals("ageFrom", copy.getColumns()[0].getName());
        assertEquals("ageTo", copy.getColumns()[1].getName());

        assertEquals(1, copy.getNumOfRanges());
        assertEquals("ageFrom-ageTo", copy.getRanges()[0].getName());

        assertEquals(1, copy.getNumOfUniqueKeys());
        IUniqueKey copyKey = copy.getUniqueKeys()[0];
        assertEquals(2, copyKey.getKeyItemNames().length);

    }

    public void testNewPart() {
        try {
            assertTrue(table.newPart(IColumn.class) instanceof IColumn);
            assertTrue(table.newPart(IColumnRange.class) instanceof IColumnRange);
            assertTrue(table.newPart(IUniqueKey.class) instanceof IUniqueKey);
            assertTrue(table.newPart(IForeignKey.class) instanceof IForeignKey);

            table.newPart(Object.class);
            fail();
        } catch (IllegalArgumentException e) {
            // nothing to do :-)
        }
    }

    public void testGetColumn() {
        IColumn column1 = table.newColumn();
        table.newColumn();
        table.newColumn();
        IColumn column2 = table.newColumn();
        table.newColumn();

        assertEquals(column1, table.getColumn(0));
        assertEquals(column2, table.getColumn(3));
    }

    public void testGetColumnIndex() {
        IColumn column1 = table.newColumn();
        table.newColumn();
        table.newColumn();
        IColumn column2 = table.newColumn();
        table.newColumn();

        assertEquals(0, table.getColumnIndex(column1));
        assertEquals(3, table.getColumnIndex(column2));
    }

    public void testValidateMoreThanOneKeyNotAdvisableInFormulas() throws Exception {
        TableStructure structure = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "table");

        IColumn first = structure.newColumn();
        first.setDatatype(Datatype.STRING.getQualifiedName());
        first.setName("first");

        IColumn second = structure.newColumn();
        second.setDatatype(Datatype.INTEGER.getQualifiedName());
        second.setName("second");

        IColumn third = structure.newColumn();
        third.setDatatype(Datatype.INTEGER.getQualifiedName());
        third.setName("third");

        IUniqueKey firstKey = structure.newUniqueKey();
        firstKey.addKeyItem(second.getName());

        IUniqueKey secondKey = structure.newUniqueKey();
        secondKey.addKeyItem(third.getName());

        MessageList msgList = structure.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableStructure.MSGCODE_MORE_THAN_ONE_KEY_NOT_ADVISABLE_IN_FORMULAS));

        secondKey.delete();

        msgList = structure.validate(project);
        assertNull(msgList.getMessageByCode(ITableStructure.MSGCODE_MORE_THAN_ONE_KEY_NOT_ADVISABLE_IN_FORMULAS));
    }

    /**
     * Test for findAllMetaObjects(..)
     * 
     * @throws CoreException
     */
    public void testFindAllMetaObjects() throws CoreException {
        String tableStructureQName = "pack.MyTableStructure";
        String tableStructureProj2QName = "otherpack.MyTableStructureProj2";
        String tableContent1QName = "pack.MyTableContent1";
        String tableContent2QName = "pack.MyTableContent2";
        String tableContent3QName = "pack.MyTableContent3";
        String tableContentProj2QName = "otherpack.MyTableContentProj2";

        IIpsProject referencingProject = newIpsProject("referencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(project);
        referencingProject.setIpsObjectPath(path);

        IIpsProject independentProject = newIpsProject("independentProject");

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. project have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        IIpsProject leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        IIpsProject leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);

        TableStructure tableStructure = newTableStructure(project, tableStructureQName);
        TableContents tableContent1 = newTableContents(tableStructure, tableContent1QName);
        TableContents tableContent2 = newTableContents(tableStructure, tableContent2QName);
        TableContents tableContent3 = newTableContents(project, tableContent3QName);

        Object[] result = tableStructure.searchMetaObjectSrcFiles(true);
        List<Object> resultList = Arrays.asList(result);
        assertEquals(2, result.length);
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        result = tableStructure.searchMetaObjectSrcFiles(false);
        resultList = Arrays.asList(result);
        assertEquals(2, result.length);
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        TableContents tableContentProj2 = newTableContents(referencingProject, tableContentProj2QName);
        tableContentProj2.setTableStructure(tableStructureQName);

        result = tableStructure.searchMetaObjectSrcFiles(true);
        resultList = Arrays.asList(result);
        assertEquals(3, result.length);
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContentProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        TableStructure tableStructureProj2 = newTableStructure(independentProject, tableStructureProj2QName);

        result = tableStructureProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, result.length);

    }
}
