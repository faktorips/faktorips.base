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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IForeignKey;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TableStructureTest extends AbstractIpsPluginTest {

    private TableStructure table;
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ArrayList<Locale> supportedLanguages = new ArrayList<>();
        supportedLanguages.add(Locale.GERMAN);
        supportedLanguages.add(Locale.ENGLISH);
        supportedLanguages.add(Locale.FRENCH);
        project = newIpsProject(supportedLanguages);
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
    }

    @Test
    public void testGetChildren() {
        IColumn c0 = table.newColumn();
        IColumnRange r0 = table.newRange();
        IIndex uk0 = table.newIndex();
        IForeignKey fk0 = table.newForeignKey();

        List<IIpsElement> children = Arrays.asList(table.getChildren());
        assertTrue(children.contains(c0));
        assertTrue(children.contains(r0));
        assertTrue(children.contains(uk0));
        assertTrue(children.contains(fk0));
    }

    @Test
    public void testGetColumns() {
        assertEquals(0, table.getColumns().length);
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();
        assertEquals(2, table.getColumns().length);
        assertEquals(c1, table.getColumns()[0]);
        assertEquals(c2, table.getColumns()[1]);
    }

    @Test
    public void testGetNumOfColumns() {
        assertEquals(0, table.getNumOfColumns());
        table.newColumn();
        assertEquals(1, table.getNumOfColumns());
        table.newColumn();
        assertEquals(2, table.getNumOfColumns());
    }

    @Test
    public void testNewColumn() {
        IColumn c = table.newColumn();
        assertEquals(table, c.getParent());
        assertEquals(c, table.getColumns()[0]);
    }

    @Test
    public void testGetRanges() {
        assertEquals(0, table.getRanges().length);
        IColumnRange r1 = table.newRange();
        IColumnRange r2 = table.newRange();
        assertEquals(2, table.getRanges().length);
        assertEquals(r1, table.getRanges()[0]);
        assertEquals(r2, table.getRanges()[1]);
    }

    @Test
    public void testGetNumOfRanges() {
        assertEquals(0, table.getNumOfRanges());
        table.newRange();
        assertEquals(1, table.getNumOfRanges());
        table.newRange();
        assertEquals(2, table.getNumOfRanges());
    }

    @Test
    public void testNewRange() {
        IColumnRange r = table.newRange();
        assertEquals(table, r.getParent());
        assertEquals(r, table.getRanges()[0]);
    }

    @Test
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
        IIndex key = table.newIndex();
        key.setKeyItems(new String[] { gender.getName(), range.getName() });
        IColumn[] columns = table.getColumnsNotInKey(key);
        assertEquals(2, columns.length);
        assertEquals(rate, columns[0]);
        assertEquals(premium, columns[1]);
    }

    @Test
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
        IIndex key = table.newIndex();
        key.setKeyItems(new String[] { gender.getName(), range.getName() });

        fcts = table.getAccessFunctions();
        assertEquals(2, fcts.length);
        assertSame(table, fcts[0].getTableStructure());
        assertEquals("rate", fcts[0].getAccessedColumnName());
        assertSame(rate, fcts[0].getAccessedColumn());
        assertEquals(rate.getDatatype(), fcts[0].getType());
        List<String> argTypes = fcts[0].getArgTypes();
        assertEquals(2, argTypes.size());
        assertEquals(gender.getDatatype(), argTypes.get(0));
        assertEquals(range.getDatatype(), argTypes.get(1));

        assertEquals("minPremium", fcts[1].getAccessedColumnName());

        IColumn newKeyColumn = table.newColumn();
        newKeyColumn.setName("newKeyColumn");
        newKeyColumn.setDatatype(Datatype.INTEGER.getQualifiedName());
        IIndex secondKey = table.newIndex();
        secondKey.setKeyItems(new String[] { gender.getName(), newKeyColumn.getName() });

        fcts = table.getAccessFunctions();
        assertEquals(7, fcts.length);
        assertSame(table, fcts[0].getTableStructure());
        assertEquals("rate", fcts[0].getAccessedColumnName());
        assertSame(rate, fcts[0].getAccessedColumn());
        assertEquals(rate.getDatatype(), fcts[0].getType());
        argTypes = fcts[0].getArgTypes();
        assertEquals(2, argTypes.size());
        assertEquals(gender.getDatatype(), argTypes.get(0));
        assertEquals(range.getDatatype(), argTypes.get(1));
        assertEquals("minPremium", fcts[1].getAccessedColumnName());
        assertEquals("newKeyColumn", fcts[2].getAccessedColumnName());
        assertEquals("ageFrom", fcts[3].getAccessedColumnName());
        assertEquals("ageTo", fcts[4].getAccessedColumnName());
        assertEquals("rate", fcts[5].getAccessedColumnName());
        assertEquals("minPremium", fcts[6].getAccessedColumnName());

    }

    public void testGetAccessFunctions_description() {
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
        IIndex key = table.newIndex();
        key.setKeyItems(new String[] { gender.getName(), range.getName() });
    }

    @Test
    public void testInitFromXml() {
        table.initFromXml(getTestDocument().getDocumentElement());
        assertTrue(table.isMultipleContentsAllowed());
        assertEquals(3, table.getNumOfColumns());
        assertEquals("ageFrom", table.getColumns()[0].getName());
        assertEquals("ageTo", table.getColumns()[1].getName());
        assertEquals("rate", table.getColumns()[2].getName());

        assertEquals(1, table.getNumOfRanges());
        assertEquals("ageFrom-ageTo", table.getRanges()[0].getName());

        assertEquals(2, table.getIndices().size());
    }

    @Test
    public void testToXmlDocument() {
        table.setTableStructureType(TableStructureType.MULTIPLE_CONTENTS);
        IColumn column1 = table.newColumn();
        column1.setName("ageFrom");
        IColumn column2 = table.newColumn();
        column2.setName("ageTo");
        IColumnRange range = table.newRange();
        range.setFromColumn("ageFrom");
        range.setToColumn("ageTo");
        IIndex uniqueKey = table.newIndex();
        uniqueKey.setKeyItems(new String[] { "ageFrom", "ageTo" });

        Element element = table.toXml(newDocument());
        ITableStructure copy = new TableStructure();
        copy.initFromXml(element);

        assertEquals(TableStructureType.MULTIPLE_CONTENTS, copy.getTableStructureType());
        assertEquals(2, copy.getNumOfColumns());
        assertEquals("ageFrom", copy.getColumns()[0].getName());
        assertEquals("ageTo", copy.getColumns()[1].getName());

        assertEquals(1, copy.getNumOfRanges());
        assertEquals("ageFrom-ageTo", copy.getRanges()[0].getName());

        assertEquals(1, copy.getNumOfUniqueKeys());
        IIndex copyKey = copy.getUniqueKeys()[0];
        assertEquals(2, copyKey.getKeyItemNames().length);
    }

    @Test
    public void testNewPart() {
        assertNotNull(table.newPart(IColumn.class));
        assertNotNull(table.newPart(IColumnRange.class));
        assertNotNull(table.newPart(IIndex.class));
        assertNotNull(table.newPart(IForeignKey.class));
    }

    @Test
    public void testGetColumn() {
        IColumn column1 = table.newColumn();
        table.newColumn();
        table.newColumn();
        IColumn column2 = table.newColumn();
        table.newColumn();

        assertEquals(column1, table.getColumn(0));
        assertEquals(column2, table.getColumn(3));
    }

    @Test
    public void testGetColumnIndex() {
        IColumn column1 = table.newColumn();
        table.newColumn();
        table.newColumn();
        IColumn column2 = table.newColumn();
        table.newColumn();

        assertEquals(0, table.getColumnIndex(column1));
        assertEquals(3, table.getColumnIndex(column2));
    }

    @Test
    public void testHasIndexWithSameDatatype() throws Exception {

        TableStructure structure = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TableStruct");

        IColumn firstString = structure.newColumn();
        firstString.setDatatype(Datatype.STRING.getQualifiedName());
        firstString.setName("firstString");

        IColumn secondString = structure.newColumn();
        secondString.setDatatype(Datatype.STRING.getQualifiedName());
        secondString.setName("secondString");

        IColumn firstInteger = structure.newColumn();
        firstInteger.setDatatype(Datatype.INTEGER.getQualifiedName());
        firstInteger.setName("firstInteger");

        IColumn secondInteger = structure.newColumn();
        secondInteger.setDatatype(Datatype.INTEGER.getQualifiedName());
        secondInteger.setName("secondInteger");

        IColumnRange range = structure.newRange();
        range.setFromColumn(firstInteger.getName());
        range.setToColumn(secondInteger.getName());
        range.setColumnRangeType(ColumnRangeType.TWO_COLUMN_RANGE);

        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex firstStringKey = structure.newIndex();
        firstStringKey.addKeyItem(firstString.getName());

        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex firstIntegerKey = structure.newIndex();
        firstIntegerKey.addKeyItem(firstInteger.getName());

        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex secondIntegerKey = structure.newIndex();
        secondIntegerKey.addKeyItem(secondInteger.getName());

        assertTrue(structure.hasIndexWithSameDatatype());

        structure.removeIndex(secondIntegerKey);

        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex rangeKey = structure.newIndex();
        rangeKey.addKeyItem(range.getName());

        assertTrue(structure.hasIndexWithSameDatatype());

        structure.removeIndex(rangeKey);
        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex combinedKey = structure.newIndex();
        combinedKey.addKeyItem(firstString.getName());
        combinedKey.addKeyItem(firstInteger.getName());
        assertFalse(structure.hasIndexWithSameDatatype());

        IIndex secondCombinedKey = structure.newIndex();
        secondCombinedKey.addKeyItem(secondString.getName());
        secondCombinedKey.addKeyItem(secondInteger.getName());

        assertTrue(structure.hasIndexWithSameDatatype());
    }

    @Test
    public void testFindAllMetaObjects() {
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

        Collection<IIpsSrcFile> resultList = tableStructure.searchMetaObjectSrcFiles(true);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        resultList = tableStructure.searchMetaObjectSrcFiles(false);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        TableContents tableContentProj2 = newTableContents(referencingProject, tableContentProj2QName);
        tableContentProj2.setTableStructure(tableStructureQName);
        // need to save to update cache
        tableContentProj2.newTableRows();
        tableContentProj2.getIpsSrcFile().save(null);

        resultList = tableStructure.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(tableContent1.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContent2.getIpsSrcFile()));
        assertTrue(resultList.contains(tableContentProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(tableContent3.getIpsSrcFile()));

        TableStructure tableStructureProj2 = newTableStructure(independentProject, tableStructureProj2QName);

        resultList = tableStructureProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testGetUniqueKeys() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);

        IIndex[] uniqueKeys = table.getUniqueKeys();

        assertEquals(1, uniqueKeys.length);
        assertEquals(index2, uniqueKeys[0]);
    }

    @Test
    public void testGetUniqueKey() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);
        index2.setKeyItems(new String[] { "index2" });

        IIndex uniqueKey = table.getUniqueKey("index2");

        assertEquals(index2, uniqueKey);
    }

    @Test
    public void testGetNumOfUniqueKeys() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);

        int numOfUniqueKeys = table.getNumOfUniqueKeys();

        assertEquals(1, numOfUniqueKeys);
    }

    @Test
    public void testGetIndices() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);

        List<IIndex> indices = table.getIndices();

        assertEquals(2, indices.size());
        assertEquals(index1, indices.get(0));
        assertEquals(index2, indices.get(1));
    }

    @Test
    public void testGetIndex_nonUnqieu() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        index1.setKeyItems(new String[] { "index1" });
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);
        index2.setKeyItems(new String[] { "index2" });

        IIndex index = table.getIndex("index1");

        assertEquals(index1, index);
    }

    @Test
    public void testGetIndex_unqieu() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        index1.setKeyItems(new String[] { "index1" });
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);
        index2.setKeyItems(new String[] { "index2" });

        IIndex index = table.getIndex("index2");

        assertEquals(index2, index);
    }

    @Test
    public void testGetNumOfIndices() throws Exception {
        IIndex index1 = table.newIndex();
        index1.setUniqueKey(false);
        IIndex index2 = table.newIndex();
        index2.setUniqueKey(true);

        int numOfIndices = table.getNumOfIndices();

        assertEquals(2, numOfIndices);
    }

    @Test
    public void testDependsOn_Datatype() throws Exception {
        EnumType depEnumType = newEnumType(project, "DependantEnumType");
        TableStructure tableStructure = newTableStructure(project, "AnyTableStructure");
        IColumn column1 = tableStructure.newColumn();
        column1.setDatatype(ValueDatatype.STRING.getQualifiedName());
        IColumn column2 = tableStructure.newColumn();
        column2.setDatatype(depEnumType.getQualifiedName());

        IDependency[] dependencies = tableStructure.dependsOn();

        assertThat(dependencies.length, is(2));
        assertThat(dependencies[0], is((IDependency)new DatatypeDependency(tableStructure.getQualifiedNameType(),
                ValueDatatype.STRING.getQualifiedName())));
        assertThat(dependencies[1], is((IDependency)new DatatypeDependency(tableStructure.getQualifiedNameType(),
                depEnumType.getQualifiedName())));
        List<IDependencyDetail> detail1 = tableStructure.getDependencyDetails(dependencies[0]);
        assertThat(detail1.size(), is(1));
        assertThat(detail1, hasItem(new DependencyDetail(column1, IColumn.PROPERTY_DATATYPE)));
        List<IDependencyDetail> detail2 = tableStructure.getDependencyDetails(dependencies[1]);
        assertThat(detail2.size(), is(1));
        assertThat(detail2, hasItem(new DependencyDetail(column2, IColumn.PROPERTY_DATATYPE)));
    }

}
