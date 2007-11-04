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
import java.io.FileOutputStream;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

public class ExcelTableImportOperationTest extends AbstractIpsPluginTest {

    ITableContentsGeneration importTarget;
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

        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "importTarget");
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        importTarget = (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());

        String filename = "excel.xls";
        file = new File(filename);
        file.delete();
        assertTrue(file.createNewFile());

    }

    protected void tearDown() throws Exception {
        file.delete();
        super.tearDown();
    }

    public void testImportValid() throws Exception {
        MessageList ml = new MessageList();
        createValid();
        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    public void testImportValidRowMismatch() throws Exception {
        MessageList ml = new MessageList();
        createValid();

        // too many columns
        IColumn col = structure.newColumn();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml);
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

    public void testImportFirstRowContainsNoColumnHeader() throws Exception{
        MessageList ml = new MessageList();
        createValid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", false, ml);
        op.run(new NullProgressMonitor());
        
        assertEquals(4, importTarget.getRows().length);
    }
    
    public void testImportFirstRowContainsColumnHeader() throws Exception{
        MessageList ml = new MessageList();
        createValid();
        
        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        
        assertEquals(3, importTarget.getRows().length);
    }
    
    public void testImportInvalid() throws Exception {
        MessageList ml = new MessageList();
        createInvalid();
        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(6, ml.getNoOfMessages());
    }

    private void createValid() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        sheet.createRow(0); // header
        HSSFRow row1 = sheet.createRow(1);
        HSSFRow row2 = sheet.createRow(2);
        HSSFRow row3 = sheet.createRow(3);

        HSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat((short)27);

        row1.createCell((short)0).setCellValue(true);
        row1.createCell((short)1).setCellValue(12.3);
        row1.createCell((short)2).setCellValue(Double.MAX_VALUE);
        HSSFCell cell = row1.createCell((short)3);
        cell.setCellValue(new GregorianCalendar(2001, 03, 26).getTime());
        cell.setCellStyle(dateStyle);
        row1.createCell((short)4).setCellValue(Integer.MAX_VALUE);
        row1.createCell((short)5).setCellValue(Long.MAX_VALUE);
        row1.createCell((short)6).setCellValue("123.45 EUR");
        row1.createCell((short)7).setCellValue("einfacher text");

        row2.createCell((short)0).setCellValue(false);
        row2.createCell((short)1).setCellValue(12.3);
        row2.createCell((short)2).setCellValue(Double.MIN_VALUE);
        cell = row2.createCell((short)3);
        cell.setCellValue(new GregorianCalendar(2001, 03, 26).getTime());
        cell.setCellStyle(dateStyle);
        row2.createCell((short)4).setCellValue(Integer.MIN_VALUE);
        row2.createCell((short)5).setCellValue(Long.MIN_VALUE);
        row2.createCell((short)6).setCellValue("1EUR");
        row2.createCell((short)7).setCellValue("öäüÖÄÜß{[]}");

        row3.createCell((short)0).setCellValue("NULL");
        row3.createCell((short)1).setCellValue("NULL");
        row3.createCell((short)2).setCellValue("NULL");
        cell = row3.createCell((short)3);
        cell.setCellValue("NULL");
        cell.setCellStyle(dateStyle);
        row3.createCell((short)4).setCellValue("NULL");
        row3.createCell((short)5).setCellValue("NULL");
        row3.createCell((short)6).setCellValue("NULL");
        row3.createCell((short)7).setCellValue("NULL");

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }

    private void createInvalid() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        sheet.createRow(0); // header
        HSSFRow row1 = sheet.createRow(1);
        HSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat((short)27);

        row1.createCell((short)0).setCellValue("invalid is impossible");
        row1.createCell((short)1).setCellValue("INVALID");
        row1.createCell((short)2).setCellValue("INVALID");
        HSSFCell cell = row1.createCell((short)3);
        cell.setCellValue("INVALID");
        cell.setCellStyle(dateStyle);
        row1.createCell((short)4).setCellValue("INVALID");
        row1.createCell((short)5).setCellValue("INVALID");
        row1.createCell((short)6).setCellValue("INVALID");
        row1.createCell((short)7).setCellValue("invalid is impossible");

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }
}
