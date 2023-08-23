/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ExcelTableImportOperationTest extends AbstractTableTest {

    ITableRows importTarget;
    ExcelTableFormat format;
    ITableStructure structure;

    File file;

    private IIpsProject ipsProject;
    private ITableContents contents;

    @Override
    @Before
    public void setUp() throws Exception {
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
        ITableStructure structure2 = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable2");
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        importTarget = contents.newTableRows();
        contents.setTableStructure(structure2.getQualifiedName());
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");
        contents.newColumn(null, "");

        file = new File("table" + format.getDefaultExtension());
        file.delete();
        assertTrue(file.createNewFile());
    }

    @Override
    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    @Test
    public void testImportValid() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();
        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
        String[] row0 = new String[] { "true", "12.3", "1.79769313486231E308", "2001-04-26", "2147483647",
                "922337203685477000", "123.45 EUR", "einfacher text" };
        assertRow(row0, importTarget.getRow(0));
        String[] row1 = new String[] { "false", "12.3", "4.9E-324", "2001-04-26", "-2147483648", "-922337203685477000",
                "1.00 EUR", "�������{[]}" };
        assertRow(row1, importTarget.getRow(1));
        String[] row2 = new String[] { null, null, null, null, null, null, null, null };
        assertRow(row2, importTarget.getRow(2));
    }

    @Test
    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", false, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(4, importTarget.getRows().length);
    }

    @Test
    public void testImportFirstRowContainsColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createValid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(3, importTarget.getRows().length);
    }

    @Test
    public void testImportInvalid() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createInvalid();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertEquals(7, ml.size());
    }

    @Test
    public void testImportNullCell() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createEmptyRow();

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertEquals(8, ml.size());
        String[] row2 = new String[] { null, null, null, null, null, null, null, null };
        assertRow(row2, importTarget.getRow(0));
    }

    @Test
    public void testImportDateProblemOO() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createProblemDate("");

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", false, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(4, importTarget.getRows().length);
        assertEquals("1900-01-01", importTarget.getRow(0).getValue(3));
        assertEquals("1900-03-01", importTarget.getRow(1).getValue(3));
        assertEquals("1900-02-27", importTarget.getRow(2).getValue(3));
        assertEquals("1900-02-28", importTarget.getRow(3).getValue(3));
        assertEquals(1, ml.size());
        assertTrue(ml.getMessageByCode(AbstractExcelImportOperation.MSG_CODE_FIXED_OPEN_OFFICE_DATE) != null);
    }

    @Test
    public void testImportDateProblemExcel() throws Exception {
        MessageList ml = new MessageList();
        structure = createTableStructure(ipsProject);
        createProblemDate("Microsoft Excel");

        ExcelTableImportOperation op = new ExcelTableImportOperation(structure, file.getName(), importTarget, format,
                "NULL", false, ml, true);
        op.run(new NullProgressMonitor());

        assertEquals(4, importTarget.getRows().length);
        assertEquals("1900-01-02", importTarget.getRow(0).getValue(3));
        assertEquals("1900-03-01", importTarget.getRow(1).getValue(3));
        assertEquals("1900-02-28", importTarget.getRow(2).getValue(3));
        assertEquals("1900-03-01", importTarget.getRow(3).getValue(3));
        assertTrue(ml.isEmpty());
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

        row1.createCell(0).setCellValue(true);
        row1.createCell(1).setCellValue(12.3);
        row1.createCell(2).setCellValue(1.79769313486231E308);
        HSSFCell cell = row1.createCell(3);
        cell.setCellValue(new GregorianCalendar(2001, 03, 26).getTime());
        cell.setCellStyle(dateStyle);
        row1.createCell(4).setCellValue(Integer.MAX_VALUE);
        // In oder to avoid floating Point problems a value of type Big Decimal is used
        row1.createCell(5).setCellValue(922337203685477000.0);
        row1.createCell(6).setCellValue("123.45 EUR");
        row1.createCell(7).setCellValue("einfacher text");

        row2.createCell(0).setCellValue(false);
        row2.createCell(1).setCellValue(12.3);
        row2.createCell(2).setCellValue(Double.MIN_VALUE);
        cell = row2.createCell(3);
        cell.setCellValue(new GregorianCalendar(2001, 03, 26).getTime());
        cell.setCellStyle(dateStyle);
        row2.createCell(4).setCellValue(Integer.MIN_VALUE);
        // In oder to avoid floating Point problems a value of type Big Decimal is used
        row2.createCell(5).setCellValue(-922337203685477000.0);
        row2.createCell(6).setCellValue("1EUR");
        row2.createCell(7).setCellValue("�������{[]}");

        row3.createCell(0).setCellValue("NULL");
        row3.createCell(1).setCellValue("NULL");
        row3.createCell(2).setCellValue("NULL");
        cell = row3.createCell(3);
        cell.setCellValue("NULL");
        cell.setCellStyle(dateStyle);
        row3.createCell(4).setCellValue("NULL");
        row3.createCell(5).setCellValue("NULL");
        row3.createCell(6).setCellValue("NULL");
        row3.createCell(7).setCellValue("NULL");

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

        row1.createCell(0).setCellValue("invalid is impossible");
        row1.createCell(1).setCellValue("INVALID");
        row1.createCell(2).setCellValue("INVALID");
        HSSFCell cell = row1.createCell(3);
        cell.setCellValue("INVALID");
        cell.setCellStyle(dateStyle);
        row1.createCell(4).setCellValue("INVALID");
        row1.createCell(5).setCellValue("INVALID");
        row1.createCell(6).setCellValue("INVALID");
        row1.createCell(7).setCellValue("invalid is impossible");

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }

    private void createEmptyRow() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        sheet.createRow(0); // header
        sheet.createRow(1);

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }

    private void createProblemDate(String applicationName) throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        wb.createInformationProperties();
        wb.getSummaryInformation().setApplicationName(applicationName);

        HSSFRow row0 = sheet.createRow(0);
        HSSFRow row1 = sheet.createRow(1);
        HSSFRow row2 = sheet.createRow(2);
        HSSFRow row3 = sheet.createRow(3);

        HSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat((short)0x15);

        row0.createCell(0).setCellValue("NULL");
        row0.createCell(1).setCellValue("NULL");
        row0.createCell(2).setCellValue("NULL");
        HSSFCell cell = row0.createCell(3);
        cell.setCellValue(2.0);// 2.1.1900 in Excel, 1.1.1900 in OO
        cell.setCellStyle(dateStyle);
        row0.createCell(4).setCellValue("NULL");
        row0.createCell(5).setCellValue("NULL");
        row0.createCell(6).setCellValue("NULL");
        row0.createCell(7).setCellValue("NULL");

        row1.createCell(0).setCellValue("NULL");
        row1.createCell(1).setCellValue("NULL");
        row1.createCell(2).setCellValue("NULL");
        cell = row1.createCell(3);
        cell.setCellValue(61.0);// 1.3.1900
        cell.setCellStyle(dateStyle);
        row1.createCell(4).setCellValue("NULL");
        row1.createCell(5).setCellValue("NULL");
        row1.createCell(6).setCellValue("NULL");
        row1.createCell(7).setCellValue("NULL");

        row2.createCell(0).setCellValue("NULL");
        row2.createCell(1).setCellValue("NULL");
        row2.createCell(2).setCellValue("NULL");
        cell = row2.createCell(3);
        cell.setCellValue(59.0);// 28.2.1900 in Excel, 27.2.1900 in OO
        cell.setCellStyle(dateStyle);
        row2.createCell(4).setCellValue("NULL");
        row2.createCell(5).setCellValue("NULL");
        row2.createCell(6).setCellValue("NULL");
        row2.createCell(7).setCellValue("NULL");

        row3.createCell(0).setCellValue("NULL");
        row3.createCell(1).setCellValue("NULL");
        row3.createCell(2).setCellValue("NULL");
        cell = row3.createCell(3);
        cell.setCellValue(60.0);// 29.2.1900 in Excel, 28.2.1900 in OO
        cell.setCellStyle(dateStyle);
        row3.createCell(4).setCellValue("NULL");
        row3.createCell(5).setCellValue("NULL");
        row3.createCell(6).setCellValue("NULL");
        row3.createCell(7).setCellValue("NULL");

        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
    }

}
