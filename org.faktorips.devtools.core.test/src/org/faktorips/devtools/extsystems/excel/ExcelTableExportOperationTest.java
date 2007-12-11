/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.io.File;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

public class ExcelTableExportOperationTest extends AbstractIpsPluginTest {

    ITableContentsGeneration exportSource;
    ExcelTableFormat format;
    ITableStructure structure;
    ITableContents contents;
    File file;
    IColumn col1;
    IColumn col2;
    IColumn col3;
    IColumn col4;
    IColumn col5;
    IColumn col6;
    IColumn col7;
    IColumn col8;

    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("test");
        IIpsProjectProperties props = project.getProperties();
        String[] datatypes = new String[] { Datatype.BOOLEAN.getQualifiedName(), Datatype.DECIMAL.getQualifiedName(),
                Datatype.DOUBLE.getQualifiedName(), Datatype.GREGORIAN_CALENDAR_DATE.getQualifiedName(),
                Datatype.INTEGER.getQualifiedName(), Datatype.LONG.getQualifiedName(),
                Datatype.MONEY.getQualifiedName(), Datatype.STRING.getQualifiedName() };
        props.setPredefinedDatatypesUsed(datatypes);
        project.setProperties(props);

        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE,
                "TestSructure");
        col1 = structure.newColumn();
        col1.setName("col1");
        col1.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        col2 = structure.newColumn();
        col2.setName("col2");
        col2.setDatatype(Datatype.DECIMAL.getQualifiedName());
        col3 = structure.newColumn();
        col3.setName("col3");
        col3.setDatatype(Datatype.DOUBLE.getQualifiedName());
        col4 = structure.newColumn();
        col4.setName("col4");
        col4.setDatatype(Datatype.GREGORIAN_CALENDAR_DATE.getQualifiedName());
        col5 = structure.newColumn();
        col5.setName("col5");
        col5.setDatatype(Datatype.INTEGER.getQualifiedName());
        col6 = structure.newColumn();
        col6.setName("col6");
        col6.setDatatype(Datatype.LONG.getQualifiedName());
        col7 = structure.newColumn();
        col7.setName("col7");
        col7.setDatatype(Datatype.MONEY.getQualifiedName());
        col8 = structure.newColumn();
        col8.setName("col8");
        col8.setDatatype(Datatype.STRING.getQualifiedName());
        structure.getIpsSrcFile().save(true, null);
        this.structure = structure;

        format = new ExcelTableFormat();
        format.setName("Excel");
        format.setDefaultExtension(".xls");
        format.addValueConverter(new BooleanValueConverter());
        format.addValueConverter(new DecimalValueConverter());
        format.addValueConverter(new DoubleValueConverter());
        format.addValueConverter(new DateValueConverter());
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "ImportTarget");
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.setTableStructure(structure.getQualifiedName());
        exportSource = (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());

        String filename = "excel.xls";
        file = new File(filename);
        file.delete();
        assertTrue(file.createNewFile());

    }

    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    public void testExportValid() throws Exception {
        MessageList ml = new MessageList();
        fillValid();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, file.getName(), format, "NULL", ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    public void testExportValidRowMismatch() throws Exception {
        MessageList ml = new MessageList();
        fillValid();

        // too many columns
        IColumn col = structure.newColumn();

        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, file.getName(), format, "NULL", ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // invalid structure
        ml.clear();
        col.delete();
        col1.setDatatype("");
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // too less columns
        ml.clear();
        col1.delete();
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    public void testExportInvalid() throws Exception {
        MessageList ml = new MessageList();
        createInvalid();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, file.getName(), format, "NULL", ml);
        op.run(new NullProgressMonitor());
        assertEquals(7, ml.getNoOfMessages());
    }

    private void fillValid() throws Exception {
        IRow row1 = this.exportSource.newRow();
        row1.setValue(0, "true");
        row1.setValue(1, "12.3");
        row1.setValue(2, "" + Double.MAX_VALUE);
        row1.setValue(3, "2001-04-26");
        row1.setValue(4, "" + Integer.MAX_VALUE);
        row1.setValue(5, "" + Long.MAX_VALUE);
        row1.setValue(6, "10.23EUR");
        row1.setValue(7, "simple text");

        IRow row2 = this.exportSource.newRow();
        row2.setValue(0, "false");
        row2.setValue(1, "12.3");
        row2.setValue(2, "" + Double.MIN_VALUE);
        row2.setValue(3, "2001-04-26");
        row2.setValue(4, "" + Integer.MIN_VALUE);
        row2.setValue(5, "" + Long.MIN_VALUE);
        row2.setValue(6, "1 EUR");
        row2.setValue(7, "öäüÖÄÜß{[]}");

        IRow row3 = this.exportSource.newRow();
        row3.setValue(0, null);
        row3.setValue(1, null);
        row3.setValue(2, null);
        row3.setValue(3, null);
        row3.setValue(4, null);
        row3.setValue(5, null);
        row3.setValue(6, null);
        row3.setValue(7, null);

        this.exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);
    }

    private void createInvalid() throws Exception {
        IRow row1 = this.exportSource.newRow();
        row1.setValue(0, "INVALID"); //BOOLEAN
        row1.setValue(1, "INVALID"); //DECIMAL
        row1.setValue(2, "INVALID"); //DOUBLE
        row1.setValue(3, "INVALID"); //GREGORIAN_CALENDAR_DATE
        row1.setValue(4, "INVALID"); //INTEGER
        row1.setValue(5, "INVALID"); //LONG
        row1.setValue(6, "INVALID"); //MONEY
        row1.setValue(7, "invalid is impossible"); //STRING

        this.exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);
    }
}
