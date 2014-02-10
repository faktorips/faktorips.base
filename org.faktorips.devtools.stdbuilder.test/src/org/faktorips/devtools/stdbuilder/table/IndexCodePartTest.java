/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.table;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexCodePartTest {
    @Mock
    private IIndex index;

    @Mock
    private IIpsProject ipsProject;

    private IndexCodePart indexCodePart;

    @Before
    public void setUpIndexAndCodePart() {
        indexCodePart = new IndexCodePart(index);
        when(index.getIpsProject()).thenReturn(ipsProject);
        indexCodePart.setIndexClassName("MyKeyClass");
    }

    @Before
    public void mockProjectDatatypes() throws CoreException {
        when(ipsProject.findDatatype("Integer")).thenReturn(Datatype.INTEGER);
        when(ipsProject.findDatatype("String")).thenReturn(Datatype.STRING);
        when(ipsProject.findDatatype("Money")).thenReturn(Datatype.MONEY);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneColumnParam() throws Exception {
        IKeyItem keyItem1 = mock(IKeyItem.class);
        when(keyItem1.isRange()).thenReturn(false);
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem1 });

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.KeyStructure<MyKeyClass, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneColumnParam_unique() throws Exception {
        IKeyItem keyItem1 = mock(IKeyItem.class);
        when(keyItem1.isRange()).thenReturn(false);
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem1 });
        when(index.isUniqueKey()).thenReturn(true);

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.KeyStructure<MyKeyClass, org.faktorips.runtime.internal.tableindex.UniqueResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneRangeFromParam() throws Exception {
        IColumnRange keyItem1 = mock(IColumnRange.class);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        when(keyItem1.getDatatype()).thenReturn("Integer");
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem1 });

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.RangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyOneRangeToParam() throws Exception {
        IColumnRange keyItem1 = mock(IColumnRange.class);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_TO);
        when(keyItem1.getDatatype()).thenReturn("Integer");
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem1 });

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.RangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyTwoColumnRangeParam() throws Exception {
        IColumnRange keyItem1 = mock(IColumnRange.class);
        when(keyItem1.isRange()).thenReturn(true);
        when(keyItem1.getColumnRangeType()).thenReturn(ColumnRangeType.TWO_COLUMN_RANGE);
        when(keyItem1.getDatatype()).thenReturn("Integer");
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem1 });

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure<java.lang.Integer, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testAppendMapClassWithGenerics_onlyManyParam() throws Exception {
        IColumn keyItem0 = mock(IColumn.class);
        when(keyItem0.isRange()).thenReturn(false);
        when(keyItem0.getDatatype()).thenReturn("Date");
        IColumn keyItem1 = mock(IColumn.class);
        when(keyItem1.isRange()).thenReturn(false);
        when(keyItem1.getDatatype()).thenReturn("Integer");
        IColumnRange keyItem2 = mock(IColumnRange.class);
        when(keyItem2.isRange()).thenReturn(true);
        when(keyItem2.getColumnRangeType()).thenReturn(ColumnRangeType.TWO_COLUMN_RANGE);
        when(keyItem2.getDatatype()).thenReturn("String");
        IColumnRange keyItem3 = mock(IColumnRange.class);
        when(keyItem3.isRange()).thenReturn(true);
        when(keyItem3.getColumnRangeType()).thenReturn(ColumnRangeType.ONE_COLUMN_RANGE_FROM);
        when(keyItem3.getDatatype()).thenReturn("Money");
        when(index.getKeyItems()).thenReturn(new IKeyItem[] { keyItem0, keyItem1, keyItem2, keyItem3 });

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals(
                "org.faktorips.runtime.internal.tableindex.KeyStructure<MyKeyClass, org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure<java.lang.String, org.faktorips.runtime.internal.tableindex.RangeStructure<org.faktorips.values.Money, org.faktorips.runtime.internal.tableindex.ResultStructure<MyRow>, MyRow>, MyRow>, MyRow>",
                keyStructureFieldClass);
    }

    @Test
    public void testGetKeyStructureFieldClass_empty() throws Exception {
        when(index.getKeyItems()).thenReturn(new IKeyItem[] {});
        when(index.isUniqueKey()).thenReturn(true);

        String keyStructureFieldClass = indexCodePart.getKeyStructureFieldClass("MyRow");

        assertEquals("org.faktorips.runtime.internal.tableindex.UniqueResultStructure<MyRow>", keyStructureFieldClass);
    }

}
