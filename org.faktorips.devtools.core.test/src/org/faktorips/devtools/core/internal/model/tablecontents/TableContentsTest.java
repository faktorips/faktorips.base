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

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TableContentsTest extends AbstractDependencyTest {

    private IIpsProject project;
    private IIpsSrcFile pdSrcFile;
    private ITableContents table;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Tc");
        pdSrcFile = table.getIpsSrcFile();
    }

    @Test
    public void testDependsOn() throws Exception {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        IDependency[] dependsOn = table.dependsOn();
        assertEquals(0, dependsOn.length);

        table.setTableStructure(structure.getQualifiedName());
        List<IDependency> dependsOnAsList = Arrays.asList(table.dependsOn());
        IDependency dependency = IpsObjectDependency.createInstanceOfDependency(table.getQualifiedNameType(),
                structure.getQualifiedNameType());
        assertTrue(dependsOnAsList.contains(dependency));
        assertSingleDependencyDetail(table, dependency, table, ITableContents.PROPERTY_TABLESTRUCTURE);
    }

    @Test
    public void testNewColumn() {
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        table.newGeneration();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();

        pdSrcFile.markAsClean();
        table.newColumn("a");
        assertTrue(pdSrcFile.isDirty());
        assertEquals(1, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));

        table.newColumn("b");
        assertEquals(2, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        assertEquals("b", row11.getValue(1));
        assertEquals("b", row12.getValue(1));
        assertEquals("b", row21.getValue(1));
        assertEquals("b", row22.getValue(1));
    }

    @Test
    public void testDeleteColumn() {
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        table.newGeneration();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();

        table.newColumn("a");
        table.newColumn("b");
        table.newColumn("c");

        pdSrcFile.markAsClean();
        table.deleteColumn(1);
        assertTrue(pdSrcFile.isDirty());
        assertEquals(2, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        assertEquals("c", row11.getValue(1));
        assertEquals("c", row12.getValue(1));
        assertEquals("c", row21.getValue(1));
        assertEquals("c", row22.getValue(1));
    }

    @Test
    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("blabla", table.getDescriptionText(Locale.GERMAN));
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(2, table.getNumOfColumns());
        assertEquals(2, table.getNumOfGenerations());
        assertEquals("2008-01-01", DateUtil.dateToIsoDateString(table.getGeneration(0).getValidFrom().getTime()));
        assertEquals("2008-02-01", DateUtil.dateToIsoDateString(table.getGeneration(1).getValidFrom().getTime()));
    }

    private void addExtensionPropertyDefinition(String propId) {
        Class<TableContents> extendedClass = TableContents.class;
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId(propId);
        property.setExtendedType(extendedClass);
        ((IpsModel)table.getIpsModel()).addIpsObjectExtensionProperty(property);
    }

    @Test
    public void testInitFromXmlWithExtensionProperties() {
        addExtensionPropertyDefinition("prop1");
        addExtensionPropertyDefinition("prop2");

        table.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("XYZ", table.getExtPropertyValue("prop1"));
        assertEquals("ABC", table.getExtPropertyValue("prop2"));
    }

    /**
     * Test init via SAX
     */
    @Test
    public void testInitFromInputStream() throws CoreException {
        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(2, table.getNumOfColumns());
        assertEquals(2, table.getNumOfGenerations());
        ITableContentsGeneration generation = (ITableContentsGeneration)table.getFirstGeneration();
        assertEquals("2008-01-01", DateUtil.dateToIsoDateString(generation.getValidFrom().getTime()));
        IRow[] rows = generation.getRows();
        assertEquals(2, rows.length);
        assertEquals("18", rows[0].getValue(0));
        assertEquals("0.5", rows[0].getValue(1));
        assertEquals("19", rows[1].getValue(0));
        assertEquals("0.6", rows[1].getValue(1));

        generation = (ITableContentsGeneration)generation.getNextByValidDate();
        assertEquals("2008-02-01", DateUtil.dateToIsoDateString(generation.getValidFrom().getTime()));
        rows = generation.getRows();
        assertEquals(2, rows.length);
        assertEquals("180", rows[0].getValue(0));
        assertEquals("0.05", rows[0].getValue(1));
        assertEquals("190", rows[1].getValue(0));
        assertEquals("0.06", rows[1].getValue(1));

        assertEquals(2, table.getNumOfGenerations());

        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));
        assertEquals(2, table.getNumOfGenerations());
    }

    /**
     * Test init via SAX
     */
    @Test
    public void testInitFromInputStreamWithExtensionProperties() throws CoreException {
        addExtensionPropertyDefinition("prop1");
        addExtensionPropertyDefinition("prop2");

        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));

        assertEquals("XYZ", table.getExtPropertyValue("prop1"));
        assertEquals("ABC", table.getExtPropertyValue("prop2"));

        // test invalid XML table content with extension properties inside generation node
        boolean exception = false;
        try {
            table.initFromInputStream(getClass().getResourceAsStream("TableContentsTest2.xml"));
        } catch (CoreException e) {
            exception = true;
        }
        assertTrue(
                "Expected RuntimeException because extension properties inside generations are not supported using SAX",
                exception);
    }

    @Test
    public void testToXmlDocument() {
        IDescription description = table.newDescription();
        description.setLocale(Locale.GERMAN);
        description.setText("blabla");
        table.setTableStructure("RateTableStructure");
        table.newColumn("");
        ITableContentsGeneration gen1 = (ITableContentsGeneration)table.newGeneration();
        IIpsObjectGeneration gen2 = table.newGeneration();
        IRow row = gen1.newRow();
        row.setValue(0, "value");

        Element element = table.toXml(newDocument());
        description.setText("");
        table.setTableStructure("");
        table.deleteColumn(0);
        gen1.delete();
        gen2.delete();
        table.initFromXml(element);
        assertEquals("blabla", description.getText());
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(1, table.getNumOfColumns());
        assertEquals(2, table.getNumOfGenerations());
        ITableContentsGeneration gen = (ITableContentsGeneration)table.getGenerationsOrderedByValidDate()[0];
        assertEquals(1, gen.getRows().length);
        row = gen.getRows()[0];
        assertEquals("value", row.getValue(0));

    }

    @Test
    public void testValidateKeyValuesFromTo() throws Exception {
        MessageList msgList = null;

        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        IColumn column1 = structure.newColumn();
        column1.setDatatype(Datatype.STRING.getQualifiedName());
        column1.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setDatatype(Datatype.STRING.getQualifiedName());
        column2.setName("second");
        IColumn column3 = structure.newColumn();
        column3.setDatatype(Datatype.STRING.getQualifiedName());
        column3.setName("third");

        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("first");
        range.setToColumn("second");

        structure.newUniqueKey().addKeyItem(range.getName());

        table.setTableStructure(structure.getQualifiedName());
        ITableContentsGeneration tableGen = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));

        IRow newRow = tableGen.newRow();
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(2, msgList.getNoOfMessages());

        newRow.setValue(0, "1");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(1, msgList.getNoOfMessages());

        newRow.setValue(1, "2");
        msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(0, msgList.getNoOfMessages());
    }

    @Test
    public void testValidateRowRangeFromGreaterToValue() throws Exception {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        IColumn fromColumn = structure.newColumn();
        fromColumn.setDatatype(Datatype.INTEGER.getQualifiedName());
        fromColumn.setName("fromColumn");
        IColumn toColumn = structure.newColumn();
        toColumn.setDatatype(Datatype.INTEGER.getQualifiedName());
        toColumn.setName("toColumn");

        IColumnRange range = structure.newRange();
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);
        range.setFromColumn("fromColumn");
        range.setToColumn("toColumn");

        structure.newUniqueKey().addKeyItem(range.getName());

        table.setTableStructure(structure.getQualifiedName());
        ITableContentsGeneration tableGen = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("fromColumn");
        table.newColumn("toColumn");
        IRow newRow = tableGen.newRow();
        newRow.setValue(0, "10");
        newRow.setValue(1, "20");

        MessageList msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNIQUE_KEY_FROM_COlUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE));

        newRow.setValue(0, "21");
        newRow.setValue(1, "20");

        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNIQUE_KEY_FROM_COlUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE));
    }

    @Test
    public void testValidate() throws Exception {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        IColumn column1 = structure.newColumn();
        column1.setDatatype(Datatype.STRING.getQualifiedName());
        column1.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setDatatype(Datatype.STRING.getQualifiedName());
        column2.setName("second");
        IColumn column3 = structure.newColumn();
        column3.setDatatype(Datatype.STRING.getQualifiedName());
        column3.setName("third");

        IUniqueKey key = structure.newUniqueKey();
        key.addKeyItem("first");
        key.addKeyItem("third");

        table.setTableStructure(structure.getQualifiedName());
        ITableContentsGeneration tableGen = (ITableContentsGeneration)table.newGeneration();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        tableGen.newRow();
        MessageList msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));

        table.deleteColumn(0);
        // there was an error in the code of the Row validate method that caused an
        // IndexOutOfBoundsException if a column was removed from the tablecontents
        // but not from the table structure and a UniqueKey was defined which contained an item
        // which index number was equal
        // or greater than the number of table contents columns.
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_COLUMNCOUNT_MISMATCH));

        // test validate with missing table structure
        table.setTableStructure("NONE");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_UNKNWON_STRUCTURE));
    }

    /**
     * test the findMetaClass method
     */
    @Test
    public void testFindMetaClass() throws CoreException {
        ITableStructure structure = newTableStructure(project, "Structure");
        table.setTableStructure(structure.getQualifiedName());

        IIpsSrcFile typeSrcFile = table.findMetaClassSrcFile(project);
        assertEquals(structure.getIpsSrcFile(), typeSrcFile);
    }

}
