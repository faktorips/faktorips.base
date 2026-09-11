/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.tableconversion.excel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.io.FileOutputStream;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExcelTableFormatTest extends AbstractTableTest {

    private ExcelTableFormat format;
    private IIpsProject ipsProject;
    private ITableStructure structure;
    private File file;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = initializeIpsProject("test");
        structure = createTableStructure(ipsProject);

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

        file = new File("previewTable" + format.getDefaultExtension());
        file.delete();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    @Test
    public void testPreview_IntegerAndLongColumns_ConvertedWithoutError() throws Exception {    
        createExcelFile();

        IPath path = new Path(file.getAbsolutePath());
        List<String[]> preview = format.getImportTablePreview(structure, path, 10, true, "NULL");

        assertThat(preview, hasSize(1));
        String[] row = preview.get(0);

        // col2 = DOUBLE, col4 = INTEGER, col5 = LONG
        assertThat(row[2], equalTo("1.79769313486231E308"));
        assertThat(row[4], equalTo("" + Integer.MAX_VALUE));
        assertThat(row[5], equalTo("922337203685477000"));

        for (String cell : row) {
            assertThat(cell, not(startsWith("Error:")));
        }
    }

    @Test
    public void testPreview_RowWithFewerCellsThanColumns_MissingCellsFilledWithNullRepresentation() throws Exception {
        createExcelFileWithShortRow();

        IPath path = new Path(file.getAbsolutePath());
        List<String[]> preview = format.getImportTablePreview(structure, path, 10, true, "NULL");

        assertThat(preview, hasSize(1));
        String[] row = preview.get(0);

        // the row itself only has cells for col0..col5; col6 (MONEY) and col7 (STRING) were never created
        assertThat(row, arrayWithSize(getColumnDatatypes().length));
        assertThat(row[6], equalTo("NULL"));
        assertThat(row[7], equalTo("NULL"));
    }

    private void createExcelFileWithShortRow() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            sheet.createRow(0); // header
            HSSFRow row = sheet.createRow(1);

            row.createCell(0).setCellValue(true);
            row.createCell(1).setCellValue(12.3);
            row.createCell(2).setCellValue(1.79769313486231E308);
            row.createCell(3).setCellValue(new GregorianCalendar(2001, 3, 26).getTime());
            row.createCell(4).setCellValue(Integer.MAX_VALUE);
            row.createCell(5).setCellValue(922337203685477000.0);
            // no cells created for col6 (MONEY) and col7 (STRING) -> getLastCellNum() is shorter than
            // the structure's column count

            wb.write(fos);
        }
    }

    private void createExcelFile() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            sheet.createRow(0); // header
            HSSFRow row = sheet.createRow(1);

            HSSFCellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat((short)27);

            row.createCell(0).setCellValue(true);
            row.createCell(1).setCellValue(12.3);
            row.createCell(2).setCellValue(1.79769313486231E308);
            HSSFCell dateCell = row.createCell(3);
            dateCell.setCellValue(new GregorianCalendar(2001, 3, 26).getTime());
            dateCell.setCellStyle(dateStyle);
            row.createCell(4).setCellValue(Integer.MAX_VALUE);
            row.createCell(5).setCellValue(922337203685477000.0);
            row.createCell(6).setCellValue("123.45 EUR");
            row.createCell(7).setCellValue("plain text");

            wb.write(fos);
        }
    }

}
