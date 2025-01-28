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

import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractDependencyTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.preferences.DefaultIpsModelPreferences;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TableContentsTest extends AbstractDependencyTest {

    private IIpsProject project;
    private IIpsSrcFile pdSrcFile;
    private TableContents table;
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
        ITableRows gen1 = table.getTableRows();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();

        pdSrcFile.markAsClean();
        IColumn column1 = structure.newColumn();
        column1.setName("first");
        table.newColumn("a", "first");
        assertTrue(pdSrcFile.isDirty());
        assertEquals(1, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));

        IColumn column2 = structure.newColumn();
        column2.setName("second");
        table.newColumn("b", "second");
        assertEquals(2, table.getNumOfColumns());
        assertEquals("a", row11.getValue(0));
        assertEquals("a", row12.getValue(0));
        assertEquals("a", row21.getValue(0));
        assertEquals("a", row22.getValue(0));
        assertEquals("b", row11.getValue(1));
        assertEquals("b", row12.getValue(1));
        assertEquals("b", row21.getValue(1));
        assertEquals("b", row22.getValue(1));

        assertEquals("first", table.getColumnReferences().get(0).getName());
        assertEquals("second", table.getColumnReferences().get(1).getName());
    }

    @Test
    public void testDeleteColumn() {
        ITableRows gen1 = table.getTableRows();
        IRow row11 = gen1.newRow();
        IRow row12 = gen1.newRow();
        IRow row21 = gen1.newRow();
        IRow row22 = gen1.newRow();

        IColumn first = structure.newColumn();
        first.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setName("second");
        IColumn column3 = structure.newColumn();
        column3.setName("third");

        table.newColumn("a", "first");
        table.newColumn("b", "second");
        table.newColumn("c", "third");

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
        assertEquals("first", table.getColumnReferences().get(0).getName());
        assertEquals("third", table.getColumnReferences().get(1).getName());
    }

    @Test
    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());

        assertThat(table.getDescriptionText(Locale.GERMAN), is("blabla"));
        assertThat(table.getTableStructure(), is("Ts"));
        assertThat(table.getNumOfColumns(), is(2));
        IDeprecation deprecation = table.getDeprecation();
        assertThat(deprecation, is(notNullValue()));
        assertThat(deprecation.getSinceVersionString(), is("22.6"));
        assertThat(deprecation.isForRemoval(), is(true));
        assertThat(deprecation.getDescriptionText(Locale.GERMAN), is("Andere Tabelle benutzen"));
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
    public void testInitFromInputStream() {
        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "RateTableStructure");
        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));

        assertEquals("Ts", table.getTableStructure());
        assertEquals(2, table.getNumOfColumns());
        ITableRows tableRows = table.getTableRows();
        IRow[] rows = tableRows.getRows();
        assertEquals(0, rows.length);
    }

    /**
     * Test init via SAX
     */
    @Test
    public void testInitFromInputStream_WithExtensionProperties() {
        addExtensionPropertyDefinition("prop1");
        addExtensionPropertyDefinition("prop2");

        newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "RateTableStructure");
        table.initFromInputStream(getClass().getResourceAsStream(getXmlResourceName()));

        assertEquals("XYZ", table.getExtPropertyValue("prop1"));
        assertEquals("ABC", table.getExtPropertyValue("prop2"));
    }

    /**
     * test invalid XML table content with extension properties inside generation node
     */
    @Test(expected = IpsException.class)
    public void testgetTableRows_WithExtensionPropertiesError() {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        table = spy(newTableContents(structure, "Tc"));
        doReturn(ipsSrcFile).when(table).getIpsSrcFile();
        doReturn(getClass().getResourceAsStream("TableContentsTest2.xml"))
                .when(ipsSrcFile).getContentFromEnclosingResource();

        table.setTableRowsInternal(null);
        table.getTableRows();
    }

    @Test
    public void testToXmlDocument() {
        IDescription description = table.getDescription(Locale.GERMAN);
        description.setText("blabla");
        IColumn first = structure.newColumn();
        first.setName("first");
        table.setTableStructure(structure.getQualifiedName());
        structure.newColumn();
        table.newColumn("", "");
        ITableRows gen1 = table.getTableRows();
        IRow row = gen1.newRow();
        row.setValue(0, "value");

        Element element = table.toXml(newDocument());
        description.setText("");
        table.setTableStructure("");
        table.deleteColumn(0);
        gen1.delete();
        table.initFromXml(element);
        description = table.getDescription(Locale.GERMAN);
        assertEquals("blabla", description.getText());
        assertEquals(structure.getQualifiedName(), table.getTableStructure());
    }

    @Test
    public void testValidateKeyValuesFromTo() throws Exception {
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
        table.newColumn("1", "first");
        table.newColumn("2", "second");
        table.newColumn("3", "third");

        MessageList msgList = table.validate(project);
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
        table.newColumn("fromColumn", "fromColumn");
        table.newColumn("toColumn", "toColumn");
        IRow newRow = tableGen.newRow();
        newRow.setValue(0, "10");
        newRow.setValue(1, "20");

        MessageList msgList = table.validate(project);
        assertNull(msgList.getMessageByCode(IRow.MSGCODE_UNIQUE_KEY_FROM_COLUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE));

        newRow.setValue(0, "21");
        newRow.setValue(1, "20");

        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNIQUE_KEY_FROM_COLUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE));
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
        table.newColumn("1", "first");
        table.newColumn("2", "second");
        table.newColumn("3", "third");

        tableGen.newRow();
        MessageList msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_UNDEFINED_UNIQUEKEY_VALUE));

        // test validate with column added
        IColumn column4 = structure.newColumn();
        column4.setDatatype(Datatype.STRING.getQualifiedName());
        column4.setName("fourth");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMNS_COUNT_INVALID));
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_INVALID_NUM_OF_COLUMNS));
        column4.delete();

        // test validate with column renamed
        column3.setName("notThird");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_NAMES_INVALID));
        column3.setName("third");

        // test validate with columns moved
        structure.moveColumns(new int[] { 2 }, true);
        msgList = table.validate(project);
        assertNotNull(
                msgList.getMessageByCode(ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_ORDERING_INVALID));
        structure.moveColumns(new int[] { 2 }, false);

        table.deleteColumn(0);
        // there was an error in the code of the Row validate method that caused an
        // IndexOutOfBoundsException if a column was removed from the tablecontents
        // but not from the table structure and a Index was defined which contained an item
        // which index number was equal
        // or greater than the number of table contents columns.
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMNS_COUNT_INVALID));

        // test validate with missing table structure
        table.setTableStructure("NONE");
        msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_UNKNWON_STRUCTURE));
    }

    /**
     * test the findMetaClass method
     */
    @Test
    public void testFindMetaClass() {
        ITableStructure structure = newTableStructure(project, "SearchStructure");
        table.setTableStructure(structure.getQualifiedName());

        IIpsSrcFile typeSrcFile = table.findMetaClassSrcFile(project);
        assertEquals(structure.getIpsSrcFile(), typeSrcFile);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddPartThis() {
        ITableRows rows = table.getTableRows();
        assertNotNull(rows);

        table.addPartThis(rows);
    }

    @Test
    public void testAddPartThis_WithInvalidPartType() {
        IIpsObjectPart invalidPart = mock(IIpsObjectPart.class);

        assertThat(table.addPartThis(invalidPart), is(false));
        assertThat(table.getColumnReferencesCount(), is(0));
    }

    @Test
    public void testAddPartThis_WithTableRows_NotInitialized() {
        TableRows newTableRows = new TableRows(table, "rows");
        table.setTableRowsInternal(null);

        assertThat(table.addPartThis(newTableRows), is(true));
        assertThat(table.getTableRows(), is(newTableRows));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddPartThis_WithTableRows_AlreadyInitialized() {
        TableRows firstTableRows = new TableRows(table, "rows1");
        table.addPartThis(firstTableRows);

        TableRows secondTableRows = new TableRows(table, "rows2");
        table.addPartThis(secondTableRows);
    }

    @Test
    public void testAddPartThis_WithTableColumnReference() {
        TableColumnReference columnRef = new TableColumnReference(table, "columnRef");
        columnRef.setName("columnRef");

        assertThat(table.addPartThis(columnRef), is(true));
        assertThat(table.getColumnReferencesCount(), is(1));
        assertThat(table.getColumnReferences().get(0).getName(), is("columnRef"));
    }

    @Test
    public void testAddPartThis_WithMultipleTableColumnReferences() {
        TableColumnReference columnRef1 = new TableColumnReference(table, "ref1");
        TableColumnReference columnRef2 = new TableColumnReference(table, "ref2");
        columnRef1.setName("column1");
        columnRef2.setName("column2");

        table.addPartThis(columnRef1);
        table.addPartThis(columnRef2);

        assertThat(table.getColumnReferencesCount(), is(2));
        assertThat(table.getColumnReferences().get(0).getName(), is("column1"));
        assertThat(table.getColumnReferences().get(1).getName(), is("column2"));
    }

    @Test
    public void testGetTableRows() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        TableContents tableContents = spy(table);
        when(tableContents.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getContentFromEnclosingResource())
                .thenReturn(getClass().getResourceAsStream(getXmlResourceName()));
        tableContents.setNumOfColumnsInternal(2);

        tableContents.setTableRowsInternal(null);
        ITableRows tableRows = tableContents.getTableRows();

        IRow[] rows = tableRows.getRows();

        assertEquals(2, rows.length);
        assertEquals("18", rows[0].getValue(0));
        assertEquals("0.5", rows[0].getValue(1));
        assertEquals("19", rows[1].getValue(0));
        assertEquals("0.6", rows[1].getValue(1));
    }

    @Test
    public void testValidate_InvalidNumberOfColumns() throws Exception {
        IColumn column1 = structure.newColumn();
        column1.setDatatype(Datatype.STRING.getQualifiedName());
        column1.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setDatatype(Datatype.STRING.getQualifiedName());
        column2.setName("second");

        table.setTableStructure(structure.getQualifiedName());
        table.newColumn("1", "first");
        table.newColumn("2", "second");

        IColumn column3 = structure.newColumn();
        column3.setDatatype(Datatype.STRING.getQualifiedName());
        column3.setName("third");

        MessageList msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(ITableContents.MSGCODE_INVALID_NUM_OF_COLUMNS));
    }

    @Test
    public void testValidate_InvalidNumberOfValuesInRow() throws Exception {
        IColumn column1 = structure.newColumn();
        column1.setDatatype(Datatype.STRING.getQualifiedName());
        column1.setName("first");
        IColumn column2 = structure.newColumn();
        column2.setDatatype(Datatype.STRING.getQualifiedName());
        column2.setName("second");
        IColumn column3 = structure.newColumn();
        column3.setDatatype(Datatype.STRING.getQualifiedName());
        column3.setName("third");

        table.setTableStructure(structure.getQualifiedName());
        ITableRows tableGen = table.newTableRows();
        table.newColumn("1", "first");
        table.newColumn("2", "second");
        table.newColumn("3", "third");

        Row row = (Row)tableGen.newRow();
        row.setNumberOfValues(2);

        MessageList msgList = table.validate(project);
        assertNotNull(msgList.getMessageByCode(IRow.MSGCODE_NUMBER_OF_VALUES_IS_INVALID));
    }

    @Test
    public void testValidateChildren() {
        TableContents tableContents = spy(table);
        ((DefaultIpsModelPreferences)IIpsModelExtensions.get().getModelPreferences()).setAutoValidateTables(true);
        TableRows tableRows = spy(new TableRows(tableContents, "Test"));
        tableContents.setTableRowsInternal(tableRows);
        MessageList list = new MessageList();

        tableContents.validateChildren(list, project);

        verify(tableRows).validateThis((MessageList)any(), eq(project));
    }

    @Test
    public void testValidateChildren_TableRowNull() {
        TableContents tableContents = spy(table);
        ((DefaultIpsModelPreferences)IIpsModelExtensions.get().getModelPreferences()).setAutoValidateTables(true);
        TableRows tableRows = spy(new TableRows(tableContents, "Test"));
        tableContents.setTableRowsInternal(null);
        MessageList list = new MessageList();

        tableContents.validateChildren(list, project);

        verifyNoInteractions(tableRows);
    }

    @Test
    public void testValidateChildren_AutoValidateTablesFalse() {
        TableContents tableContents = spy(table);
        ((DefaultIpsModelPreferences)IIpsModelExtensions.get().getModelPreferences()).setAutoValidateTables(false);
        TableRows tableRows = spy(new TableRows(tableContents, "Test"));
        tableContents.setTableRowsInternal(tableRows);
        MessageList list = new MessageList();

        tableContents.validateChildren(list, project);

        verify(tableRows).validateThis((MessageList)any(), eq(project));
    }

    @Test
    public void testValidateChildren_NotCall() {
        TableContents tableContents = spy(table);
        ((DefaultIpsModelPreferences)IIpsModelExtensions.get().getModelPreferences()).setAutoValidateTables(false);
        TableRows tableRows = spy(new TableRows(tableContents, "Test"));
        tableContents.setTableRowsInternal(null);
        MessageList list = new MessageList();

        tableContents.validateChildren(list, project);

        verify(tableRows, never()).validateThis((MessageList)any(), eq(project));
    }

    @Test
    public void testValidate_TableStructureIsNotDeprecated() {
        var messageList = table.validate(project);
        assertThat(messageList, not(hasMessageCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE)));
    }

    @Test
    public void testValidate_TableStructureIsDeprecated() {
        ((IpsObjectPartContainer)structure).setDeprecated(true);
        var deprecation = structure.getDeprecation();
        deprecation.setSinceVersionString("1.2.3");
        deprecation.setForRemoval(true);
        var locale = IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale();
        IDescription description = deprecation.newDescription();
        description.setLocale(locale);
        deprecation.setDescriptionText(locale, "Use Foo instead");

        var messageList = table.validate(project);

        assertThat(messageList, hasMessageCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE));
        Message message = messageList.getMessageByCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE);
        assertThat(message, hasInvalidObject(table, ITableContents.PROPERTY_TABLESTRUCTURE));
        assertThat(message, containsText("1.2.3"));
        assertThat(message, containsText("Use Foo instead"));
    }

    @Test
    public void testValidate_TableStructureIsDeprecated_UseExistingDeprecationDescription() {
        var properties = project.getProperties();
        var usedLanguagePackLocale = IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale();
        var defaultLocale = usedLanguagePackLocale.equals(Locale.ITALIAN) ? Locale.CHINESE : Locale.ITALIAN;
        properties.addSupportedLanguage(defaultLocale);
        var otherLocale = usedLanguagePackLocale.equals(Locale.FRENCH) ? Locale.KOREAN : Locale.FRENCH;
        properties.addSupportedLanguage(otherLocale);
        properties.setDefaultLanguage(defaultLocale);
        project.setProperties(properties);
        ((IpsObjectPartContainer)structure).setDeprecated(true);
        var deprecation = structure.getDeprecation();
        deprecation.setSinceVersionString("1.2.3");
        deprecation.setForRemoval(true);
        deprecation.setDescriptionText(defaultLocale, "Default Description");
        deprecation.setDescriptionText(otherLocale, "Other Description");

        var messageList = table.validate(project);

        assertThat(messageList, hasMessageCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE));
        Message message = messageList.getMessageByCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE);
        assertThat(message, hasInvalidObject(table, ITableContents.PROPERTY_TABLESTRUCTURE));
        assertThat(message, containsText("1.2.3"));
        assertThat(message, containsText("Default Description"));
    }

    @Test
    public void testToXML_UUID_removal() {
        IRow tr1 = table.getTableRows().newRow();
        IRow tr2 = table.getTableRows().newRow();
        assertNotNull(tr1.getId());
        assertNotNull(tr2.getId());

        Element xml = table.toXml(newDocument());

        NodeList rowNodeList = xml.getElementsByTagName(Row.TAG_NAME);
        assertEquals(2, rowNodeList.getLength());
        Element row1 = (Element)rowNodeList.item(0);
        Element row2 = (Element)rowNodeList.item(1);
        assertFalse(row1.hasAttribute("id"));
        assertFalse(row2.hasAttribute("id"));
    }

    @Test
    public void testInitFromInputStream_UUID_creation() {
        // create valid TableContents
        TableContents tempTc = newTableContents(project, "UUIDTestTC");
        table.setTableStructure(structure.getQualifiedName());
        tempTc.getTableRows().newRow();
        tempTc.getTableRows().newRow();
        // save to file
        tempTc.getIpsSrcFile().save(null);

        // create actual TableContents to test init with
        TableContents testTc = new TableContents(tempTc.getIpsSrcFile());
        // force init of rows (lazily loaded)
        ITableRows tableRows = testTc.getTableRows();

        assertEquals(2, tableRows.getRows().length);
        IRow row1 = tableRows.getRow(0);
        IRow row2 = tableRows.getRow(1);
        assertNotNull(row1.getId());
        assertNotNull(row2.getId());
        // check that a new UUID is created for each row
        assertThat(row1.getId(), is(not(row2.getId())));
    }
}
