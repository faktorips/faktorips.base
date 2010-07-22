/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.util.message.MessageList;

public class ExcelTableImportOperationTest extends AbstractTableTest {

    ITableContentsGeneration importTarget;
    ExcelTableFormat format;
    ITableStructure structure;

    File file;

    private IIpsProject ipsProject;
    private ITableContents contents;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("test");
        IIpsProjectProperties props = ipsProject.getProperties();
        String[] datatypes = getColumnDatatypes();
        props.setPredefinedDatatypesUsed(datatypes);
        ipsProject.setProperties(props);

        format = new ExcelTableFormat();
        format.setName("Excel");
        format.setDefaultExtension(".xls");
        format.addValueConverter(new BooleanValueConverter());
        format.addValueConverter(new DecimalValueConverter());
        format.addValueConverter(new DoubleValueConverter());
        format.addValueConverter(new DateValueConverter());
        format.addValueConverter(new GregorianCalendarValueConverter());
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "importTarget");
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        importTarget = (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());

        file = new File("table" + format.getDefaultExtension());
        file.delete();
        assertTrue(file.createNewFile());
    }

    @Override
    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    public void testImportValid() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();
        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", false, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(4, importTarget.getRows().length);
    }

    public void testImportFirstRowContainsColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(3, importTarget.getRows().length);
    }

    public void testImportInvalid() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createInvalid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
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
        row2.createCell((short)7).setCellValue("�������{[]}");

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
