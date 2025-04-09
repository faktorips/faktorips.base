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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ExcelEnumImportOperationTest extends AbstractTableTest {

    ExcelTableFormat format;
    IIpsProject ipsProject;

    File file;

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

        file = new File("enum" + format.getDefaultExtension());
        file.delete();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    @Test
    public void testImportValid_EnumType() throws Exception {
        MessageList ml = new MessageList();
        executeImportEnumType(ml, true);
        assertTrue(ml.toString(), ml.isEmpty());
    }

    @Test
    public void testImportValid_EnumContent() throws Exception {
        MessageList ml = new MessageList();
        executeImportEnumContent(ml, true);
        assertTrue(ml.toString(), ml.isEmpty());
    }

    @Test
    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, false);
        assertEquals(5, enumType.getEnumValuesCount());
    }

    @Test
    public void testImportFirstRowContainsColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, true);
        assertEquals(4, enumType.getEnumValuesCount());
    }

    @Test
    public void testImportInvalid() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = createExternalEnumType();
        enumType.clear();

        createInvalid();

        ExcelEnumImportOperation op = new ExcelEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml,
                true);
        op.run(new NullProgressMonitor());
        assertEquals(8, ml.size());
    }

    private IEnumType createExternalEnumType() throws Exception {
        // create ips src file
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        // create enum.xls
        ExcelEnumExportOperation excelEnumExportOperation = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, new MessageList());
        excelEnumExportOperation.run(null);
        return enumType;
    }

    private IEnumType executeImportEnumType(MessageList ml, boolean containsHeader) throws Exception, CoreException {
        IEnumType enumType = createExternalEnumType();

        // clear the exported file for reimport (keeping the attributes)
        enumType.clear();

        ExcelEnumImportOperation op = new ExcelEnumImportOperation(enumType, file.getName(), format, "NULL",
                containsHeader, ml, true);
        op.run(new NullProgressMonitor());
        return enumType;
    }

    private IEnumContent createExternalEnumContent() throws Exception {
        // create ips src file
        IEnumContent enumContent = createValidEnumContentWithValues(ipsProject);

        // create enum.xls
        ExcelEnumExportOperation excelEnumExportOperation = new ExcelEnumExportOperation(enumContent, file.getName(),
                format, "NULL", true, new MessageList());
        excelEnumExportOperation.run(null);
        return enumContent;
    }

    private IEnumContent executeImportEnumContent(MessageList ml, boolean containsHeader)
            throws Exception, CoreException {
        IEnumContent enumContent = createExternalEnumContent();

        // clear the exported file for reimport (keeping the attributes)
        enumContent.clear();

        ExcelEnumImportOperation op = new ExcelEnumImportOperation(enumContent, file.getName(), format, "NULL",
                containsHeader, ml, true);
        op.run(new NullProgressMonitor());
        return enumContent;
    }

    private void createInvalid() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            sheet.createRow(0); // header
            HSSFRow row1 = sheet.createRow(1);
            HSSFCellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat((short)27);

            row1.createCell(0).setCellValue("INVALID BOOLEAN");
            row1.createCell(1).setCellValue("INVALID");
            row1.createCell(2).setCellValue("INVALID");
            HSSFCell cell = row1.createCell(3);
            cell.setCellValue("INVALID");
            cell.setCellStyle(dateStyle);
            row1.createCell(4).setCellValue("INVALID");
            row1.createCell(5).setCellValue("INVALID");
            row1.createCell(6).setCellValue("INVALID");
            row1.createCell(7).setCellValue("INVALID not a legal java identifier");

            wb.write(fos);
        }
    }

    @Test
    public void testImportNullCell() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        // create enum.xls
        ExcelEnumExportOperation excelEnumExportOperation = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, new MessageList());
        excelEnumExportOperation.run(null);
        enumType.clear();

        createEmpty();

        ExcelEnumImportOperation op = new ExcelEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml,
                true);
        op.run(new NullProgressMonitor());
        assertEquals(9, ml.size());
        String[] row2 = { null, null, null, null, null, null, null, null };
        assertEnumAttributeValues(row2, enumType.getEnumValues().get(0).getEnumAttributeValues());
    }

    private void assertEnumAttributeValues(String[] stringRow, List<IEnumAttributeValue> enumAttributeValues) {
        for (int i = 0; i < stringRow.length; i++) {
            assertEquals(stringRow[i], enumAttributeValues.get(i).getValue().getContentAsString());
        }

    }

    private void createEmpty() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            sheet.createRow(0); // header
            sheet.createRow(1);

            wb.write(fos);
        }
    }

}
