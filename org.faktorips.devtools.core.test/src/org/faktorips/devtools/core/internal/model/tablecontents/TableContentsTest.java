/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TableContentsTest extends AbstractDependencyTest {

    private IIpsProject project;
    private IIpsSrcFile pdSrcFile;
    private ITableContents table;
    private ITableStructure structure;
    private IDependency structureDependency;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Ts");
        table = newTableContents(structure, "Tc");
        pdSrcFile = table.getIpsSrcFile();

        structureDependency = IpsObjectDependency.createInstanceOfDependency(table.getQualifiedNameType(),
                structure.getQualifiedNameType());
    }

    @Test
    public void testDependsOn_noStructure_noDependency() throws Exception {
        table.setTableStructure(null);

        IDependency[] dependsOn = table.dependsOn();

        assertEquals(0, dependsOn.length);
    }

    @Test
    public void testDependsOn_structureDependency() throws Exception {
        List<IDependency> dependsOnAsList = Arrays.asList(table.dependsOn());

        assertEquals(1, dependsOnAsList.size());
        assertThat(dependsOnAsList, hasItem(structureDependency));
        assertSingleDependencyDetail(table, dependsOnAsList.get(0), table, ITableContents.PROPERTY_TABLESTRUCTURE);
    }

    @Test
    public void testDependsOn_validationDependency_singleContentStructure() throws Exception {
        structure.setTableStructureType(TableStructureType.SINGLE_CONTENT);
        TableContents table2 = newTableContents(structure, "SecondTableContents");
        TableContents table3 = newTableContents(structure, "ThirdTableContents");

        List<IDependency> dependsOnAsList = Arrays.asList(table.dependsOn());

        assertEquals(3, dependsOnAsList.size());
        assertThat(dependsOnAsList, hasItem(structureDependency));
        assertThat(dependsOnAsList, hasItem(validationDependencyBetween(table, table2)));
        assertThat(dependsOnAsList, hasItem(validationDependencyBetween(table, table3)));
    }

    @Test
    public void testDependsOn_zeroValidationDependencies_multipleContentStructure() throws Exception {
        structure.setTableStructureType(TableStructureType.MULTIPLE_CONTENTS);
        newTableContents(structure, "SecondTableContents");
        newTableContents(structure, "ThirdTableContents");

        List<IDependency> dependsOnAsList = Arrays.asList(table.dependsOn());

        assertEquals(1, dependsOnAsList.size());
        assertThat(dependsOnAsList, hasItem(structureDependency));
    }

    private IDependency validationDependencyBetween(IIpsObject o1, IIpsObject o2) {
        return IpsObjectDependency.create(o1.getQualifiedNameType(), o2.getQualifiedNameType(),
                DependencyType.VALIDATION);
    }

    @Test
    public void testNewColumn() {
        ITableRows gen1 = table.getFirstGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        table.newTableRows();
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
        ITableRows gen1 = table.getFirstGeneration();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        table.newTableRows();
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
        assertTrue(table.hasTableRows());
        assertEquals(2, table.getNumOfColumns());
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
        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "RateTableStructure");
        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));
        assertEquals("RateTableStructure", table.getTableStructure());
        assertEquals(2, table.getNumOfColumns());
        ITableRows generation = table.getFirstGeneration();
        IRow[] rows = generation.getRows();
        assertEquals(2, rows.length);
        assertEquals("18", rows[0].getValue(0));
        assertEquals("0.5", rows[0].getValue(1));
        assertEquals("19", rows[1].getValue(0));
        assertEquals("0.6", rows[1].getValue(1));

        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));
        assertTrue(table.hasTableRows());
    }

    /**
     * Test init via SAX
     */
    @Test
    public void testInitFromInputStreamWithExtensionProperties() throws CoreException {
        addExtensionPropertyDefinition("prop1");
        addExtensionPropertyDefinition("prop2");

        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "RateTableStructure");
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
        table.setTableStructure(structure.getQualifiedName());
        table.newColumn("");
        ITableRows gen1 = table.getFirstGeneration();
        ITableRows gen2 = table.newTableRows();
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
        assertEquals(structure.getQualifiedName(), table.getTableStructure());
        assertEquals(1, table.getNumOfColumns());
        assertTrue(table.hasTableRows());
        ITableRows gen = table.getTableRows();
        assertEquals(1, gen.getRows().length);
        row = gen.getRows()[0];
        assertEquals("value", row.getValue(0));

    }

    @Test
    public void testValidateKeyValuesFromTo() throws Exception {
        MessageList msgList = null;

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

        structure.newIndex().addKeyItem(range.getName());

        table.setTableStructure(structure.getQualifiedName());
        ITableRows tableGen = table.newTableRows();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));

        IRow newRow = tableGen.newRow();
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(msgList.toString(), 2, msgList.size());

        newRow.setValue(0, "1");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(1, msgList.size());

        newRow.setValue(1, "2");
        msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));
        assertEquals(0, msgList.size());
    }

    @Test
    public void testValidateRowRangeFromGreaterToValue() throws Exception {
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

        structure.newIndex().addKeyItem(range.getName());

        table.setTableStructure(structure.getQualifiedName());
        ITableRows tableGen = table.newTableRows();
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
        IColumn column1 = structure.newColumn();
        column1.setDatatype(Datatype.STRING.getQualifiedName());
        column1.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setDatatype(Datatype.STRING.getQualifiedName());
        column2.setName("second");
        IColumn column3 = structure.newColumn();
        column3.setDatatype(Datatype.STRING.getQualifiedName());
        column3.setName("third");

        IIndex key = structure.newIndex();
        key.addKeyItem("first");
        key.addKeyItem("third");

        table.setTableStructure(structure.getQualifiedName());
        ITableRows tableGen = table.newTableRows();
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        tableGen.newRow();
        MessageList msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));

        table.deleteColumn(0);
        // there was an error in the code of the Row validate method that caused an
        // IndexOutOfBoundsException if a column was removed from the tablecontents
        // but not from the table structure and a Index was defined which contained an item
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
        ITableStructure structure = newTableStructure(project, "SearchStructure");
        table.setTableStructure(structure.getQualifiedName());

        IIpsSrcFile typeSrcFile = table.findMetaClassSrcFile(project);
        assertEquals(structure.getIpsSrcFile(), typeSrcFile);
    }
}
