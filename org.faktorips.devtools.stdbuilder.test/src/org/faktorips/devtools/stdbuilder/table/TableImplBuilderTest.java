/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class TableImplBuilderTest extends AbstractStdBuilderTest {

    private final static String TABLE_STRUCTURE_NAME = "TestTable";

    private ITableStructure structure;

    private TableImplBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        structure = newTableStructure(ipsProject, TABLE_STRUCTURE_NAME);
        builder = new TableImplBuilder(builderSet);
    }

    @Test
    public void testDelete() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IFile file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertTrue(file.exists());
        structure.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertFalse(file.exists());
    }

    private TableImplBuilder getTableImpleBuilder() {
        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder2 : builders) {
            if (builder2.getClass() == TableImplBuilder.class) {
                return (TableImplBuilder)builder2;
            }
        }
        throw new RuntimeException("The " + TableImplBuilder.class + " is not in the builder set.");
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(structure);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaClass(structure, false, TABLE_STRUCTURE_NAME);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneColumnParam() throws Exception {
        setUpForAppendMapClassWithGenerics();
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        IKeyItem keyItem1 = mock(IKeyItem.class);
        keyItems.add(keyItem1);
        when(keyItem1.isRange()).thenReturn(false);

        String keyStructureFieldClass = builder.getKeyStructureFieldClass(keyItems, "MyKeyClass");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.KeyStructure<MyKeyClass, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneRangeFromParam() throws Exception {
        setUpForAppendMapClassWithGenerics();
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        IColumnRange keyItem1 = mock(IColumnRange.class);
        keyItems.add(keyItem1);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        when(keyItem1.getDatatype()).thenReturn("Integer");

        String keyStructureFieldClass = builder.getKeyStructureFieldClass(keyItems, "MyKeyClass");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.RangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneRangeToParam() throws Exception {
        setUpForAppendMapClassWithGenerics();
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        IColumnRange keyItem1 = mock(IColumnRange.class);
        keyItems.add(keyItem1);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        when(keyItem1.getDatatype()).thenReturn("Integer");

        String keyStructureFieldClass = builder.getKeyStructureFieldClass(keyItems, "MyKeyClass");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.RangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyTwoColumnRangeParam() throws Exception {
        setUpForAppendMapClassWithGenerics();
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        IColumnRange keyItem1 = mock(IColumnRange.class);
        keyItems.add(keyItem1);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.TWO_COLUMN_RANGE);
        when(keyItem1.getDatatype()).thenReturn("Integer");

        String keyStructureFieldClass = builder.getKeyStructureFieldClass(keyItems, "MyKeyClass");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyManyParam() throws Exception {
        setUpForAppendMapClassWithGenerics();
        List<IKeyItem> keyItems = new ArrayList<IKeyItem>();
        IColumn keyItem0 = mock(IColumn.class);
        keyItems.add(keyItem0);
        when(keyItem0.isRange()).thenReturn(false);
        when(keyItem0.getDatatype()).thenReturn("Date");
        IColumn keyItem1 = mock(IColumn.class);
        keyItems.add(keyItem1);
        when(keyItem1.isRange()).thenReturn(false);
        when(keyItem1.getDatatype()).thenReturn("Integer");
        IColumnRange keyItem2 = mock(IColumnRange.class);
        keyItems.add(keyItem2);
        when(keyItem2.isRange()).thenReturn(true);
        when(keyItem2.getColumnRangeType()).thenReturn(ColumnRangeType.TWO_COLUMN_RANGE);
        when(keyItem2.getDatatype()).thenReturn("String");
        IColumnRange keyItem3 = mock(IColumnRange.class);
        keyItems.add(keyItem3);
        when(keyItem3.isRange()).thenReturn(true);
        when(keyItem3.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        when(keyItem3.getDatatype()).thenReturn("Money");

        String keyStructureFieldClass = builder.getKeyStructureFieldClass(keyItems, "MyKeyClass");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.KeyStructure<MyKeyClass, org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure<java.lang.String, org.faktorips.runtime.internal.tableindex.RangeStructure<org.faktorips.values.Money, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>, MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    private void setUpForAppendMapClassWithGenerics() throws CoreException {
        TableRowBuilder tableRowBuilder = mock(TableRowBuilder.class);
        when(tableRowBuilder.getQualifiedClassName(structure.getIpsSrcFile())).thenReturn("MyRow");
        builder.setTableRowBuilder(tableRowBuilder);
        builder.beforeBuild(structure.getIpsSrcFile(), null);
    }

}
