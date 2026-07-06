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

import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ExcelEnumImportOperationTest extends AbstractTableTest {

    private ExcelTableFormat format;
    private IIpsProject ipsProject;

    private File file;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = initializeIpsProject("test");

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
        assertThat(ml, isEmpty());
    }

    @Test
    public void testImportValid_EnumContent() throws Exception {
        MessageList ml = new MessageList();
        executeImportEnumContent(ml, true);
        assertThat(ml, isEmpty());
    }

    @Test
    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, false);
        assertThat(enumType.getEnumValuesCount(), is(4));
    }

    @Test
    public void testImportFirstRowContainsColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, true);
        assertThat(enumType.getEnumValuesCount(), is(3));
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
        assertThat(ml.size(), is(8));
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
        // Row with no cells is correctly identified as blank and skipped
        assertThat(enumType.getEnumValuesCount(), is(0));
    }

    private void createEmpty() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            sheet.createRow(0); // header
            sheet.createRow(1);

            wb.write(fos);
        }
    }

    @Test
    public void testImportWithLiteralName() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        ExcelEnumExportOperation excelEnumExportOperation = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, new MessageList());
        excelEnumExportOperation.run(null);

        enumType.clear();

        ExcelEnumImportOperation op = new ExcelEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml,
                true);
        op.run(new NullProgressMonitor());

        assertThat(ml.isEmpty(), is(true));
        assertThat(enumType.getEnumValuesCount(), is(3));

        IEnumValue firstValue = enumType.getEnumValues().get(0);
        List<IEnumAttributeValue> attributeValues = firstValue.getEnumAttributeValues();
        assertThat((attributeValues.get(0).isEnumLiteralNameAttributeValue()), is(true));
    }

    @Test
    public void testRoundtripMultilingual_AllLanguagesPreserved() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);

        MessageList exportMl = new MessageList();
        ExcelEnumExportOperation exportOp = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, exportMl);
        exportOp.run(new NullProgressMonitor());
        assertThat(exportMl.isEmpty(), is(true));

        enumType.clear();

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumType, file.getName(),
                format, "NULL", true, importMl, true);
        importOp.run(new NullProgressMonitor());
        assertThat(importMl, isEmpty());

        assertThat(enumType.getEnumValuesCount(), is(2));

        IEnumValue value1 = enumType.getEnumValues().get(0);
        // attr 0 = literalName, attr 1 = id, attr 2 = description (multilingual)
        IEnumAttributeValue descAttr1 = value1.getEnumAttributeValues().get(2);
        IInternationalString intString1 = (IInternationalString)descAttr1.getValue().getContent();
        assertThat(intString1.get(Locale.GERMAN).getValue(), is("Beschreibung1"));
        assertThat(intString1.get(Locale.ENGLISH).getValue(), is("Description1"));

        IEnumValue value2 = enumType.getEnumValues().get(1);
        IEnumAttributeValue descAttr2 = value2.getEnumAttributeValues().get(2);
        IInternationalString intString2 = (IInternationalString)descAttr2.getValue().getContent();
        assertThat(intString2.get(Locale.GERMAN).getValue(), is("Beschreibung2"));
        assertThat(intString2.get(Locale.ENGLISH).getValue(), is("Description2"));
    }

    @Test
    public void testRoundtripMultilingual_EnumContent() throws Exception {
        IEnumContent enumContent = createMultilingualEnumContentWithValues(ipsProject);

        MessageList exportMl = new MessageList();
        ExcelEnumExportOperation exportOp = new ExcelEnumExportOperation(enumContent, file.getName(),
                format, "NULL", true, exportMl);
        exportOp.run(new NullProgressMonitor());
        assertThat(exportMl.isEmpty(), is(true));

        enumContent.clear();

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumContent, file.getName(),
                format, "NULL", true, importMl, true);
        importOp.run(new NullProgressMonitor());
        assertThat(importMl, isEmpty());

        assertThat(enumContent.getEnumValuesCount(), is(2));

        IEnumValue value1 = enumContent.getEnumValues().get(0);
        // attr 0 = id, attr 1 = description (multilingual) for enum content (no literalName)
        IEnumAttributeValue descAttr1 = value1.getEnumAttributeValues().get(1);
        IInternationalString intString1 = (IInternationalString)descAttr1.getValue().getContent();
        assertThat(intString1.get(Locale.GERMAN).getValue(), is("Deutsch1"));
        assertThat(intString1.get(Locale.ENGLISH).getValue(), is("English1"));

        IEnumValue value2 = enumContent.getEnumValues().get(1);
        IEnumAttributeValue descAttr2 = value2.getEnumAttributeValues().get(1);
        IInternationalString intString2 = (IInternationalString)descAttr2.getValue().getContent();
        assertThat(intString2.get(Locale.GERMAN).getValue(), is("Deutsch2"));
        assertThat(intString2.get(Locale.ENGLISH).getValue(), is("English2"));
    }

    @Test
    public void testImportMultilingual_WithoutHeader() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);

        MessageList exportMl = new MessageList();
        ExcelEnumExportOperation exportOp = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", false, exportMl);
        exportOp.run(new NullProgressMonitor());
        assertThat(exportMl.isEmpty(), is(true));

        enumType.clear();

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumType, file.getName(),
                format, "NULL", false, importMl, true);
        importOp.run(new NullProgressMonitor());

        assertThat(enumType.getEnumValuesCount(), is(2));

        IEnumValue value1 = enumType.getEnumValues().get(0);
        IEnumAttributeValue descAttr1 = value1.getEnumAttributeValues().get(2);
        IInternationalString intString1 = (IInternationalString)descAttr1.getValue().getContent();
        assertThat(intString1.get(Locale.GERMAN).getValue(), is("Beschreibung1"));
        assertThat(intString1.get(Locale.ENGLISH).getValue(), is("Description1"));
    }

    @Test
    public void testImportMultilingual_NullLocaleValues() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);

        // Export, then manually create an Excel with null locale cells
        MessageList exportMl = new MessageList();
        ExcelEnumExportOperation exportOp = new ExcelEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, exportMl);
        exportOp.run(new NullProgressMonitor());

        enumType.clear();

        // Overwrite with a file that has null in one locale cell
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            // Header row 0 with merged region for description
            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("literalName");
            headerRow.createCell(1).setCellValue("id");
            headerRow.createCell(2).setCellValue("description");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 2, 3));

            // Header row 1 with locale tags
            HSSFRow localeRow = sheet.createRow(1);
            localeRow.createCell(2).setCellValue("[de]");
            localeRow.createCell(3).setCellValue("[en]");

            // Data row with null English value
            HSSFRow dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue("NULL_TEST");
            dataRow.createCell(1).setCellValue("nullId");
            dataRow.createCell(2).setCellValue("NurDeutsch");
            // Cell 3 intentionally left empty (null English value)

            wb.write(fos);
        }

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumType, file.getName(),
                format, "NULL", true, importMl, true);
        importOp.run(new NullProgressMonitor());

        assertThat(enumType.getEnumValuesCount(), is(1));
        IEnumValue value = enumType.getEnumValues().get(0);
        IEnumAttributeValue descAttr = value.getEnumAttributeValues().get(2);
        IInternationalString intString = (IInternationalString)descAttr.getValue().getContent();
        assertThat(intString.get(Locale.GERMAN).getValue(), is("NurDeutsch"));
        // English cell was left empty — readCell returns empty string, not null
        // setInternationalStringValue filters out null values but not empty strings
        assertThat(intString.get(Locale.ENGLISH).getValue(), is(""));
    }

    @Test
    public void testImportMultilingual_MalformedLocaleHeaders_ProducesError() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);
        enumType.clear();

        // Create Excel with horizontal merge but invalid row 1 content (not [locale] format)
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("LITERAL_NAME");
            headerRow.createCell(1).setCellValue("id");
            headerRow.createCell(2).setCellValue("description");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 2, 3));

            // Row 1 with INVALID locale tags
            HSSFRow localeRow = sheet.createRow(1);
            localeRow.createCell(2).setCellValue("NOT_A_LOCALE");
            localeRow.createCell(3).setCellValue("ALSO_INVALID");

            // Data row
            HSSFRow dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue("VAL");
            dataRow.createCell(1).setCellValue("x");
            dataRow.createCell(2).setCellValue("foo");
            dataRow.createCell(3).setCellValue("bar");

            wb.write(fos);
        }

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumType, file.getName(),
                format, "NULL", true, importMl, true);
        importOp.run(new NullProgressMonitor());

        // Should produce an error about malformed locale headers
        assertThat(importMl.containsErrorMsg(), is(true));
        assertThat(enumType.getEnumValuesCount(), is(0));
    }

    @Test
    public void testImportMultilingual_EmptyRowsSkipped() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);
        enumType.clear();

        // Create Excel with valid headers but an empty row between data rows
        try (HSSFWorkbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            HSSFSheet sheet = wb.createSheet();

            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("LITERAL_NAME");
            headerRow.createCell(1).setCellValue("id");
            headerRow.createCell(2).setCellValue("description");
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 2, 3));

            HSSFRow localeRow = sheet.createRow(1);
            localeRow.createCell(2).setCellValue("[de]");
            localeRow.createCell(3).setCellValue("[en]");

            // Data row 1
            HSSFRow dataRow1 = sheet.createRow(2);
            dataRow1.createCell(0).setCellValue("A");
            dataRow1.createCell(1).setCellValue("id1");
            dataRow1.createCell(2).setCellValue("Hallo");
            dataRow1.createCell(3).setCellValue("Hello");

            // Blank row 3 (cells exist but are empty/whitespace)
            HSSFRow blankRow = sheet.createRow(3);
            blankRow.createCell(0).setCellValue("");
            blankRow.createCell(1).setCellValue("  ");
            blankRow.createCell(2).setCellValue("");
            blankRow.createCell(3).setCellValue("");

            // Data row 2
            HSSFRow dataRow2 = sheet.createRow(4);
            dataRow2.createCell(0).setCellValue("B");
            dataRow2.createCell(1).setCellValue("id2");
            dataRow2.createCell(2).setCellValue("Welt");
            dataRow2.createCell(3).setCellValue("World");

            wb.write(fos);
        }

        MessageList importMl = new MessageList();
        ExcelEnumImportOperation importOp = new ExcelEnumImportOperation(enumType, file.getName(),
                format, "NULL", true, importMl, true);
        importOp.run(new NullProgressMonitor());

        // Empty row should be skipped — only 2 values imported
        assertThat(enumType.getEnumValuesCount(), is(2));

        IEnumValue value1 = enumType.getEnumValues().get(0);
        IEnumAttributeValue descAttr1 = value1.getEnumAttributeValues().get(2);
        IInternationalString intString1 = (IInternationalString)descAttr1.getValue().getContent();
        assertThat(intString1.get(Locale.GERMAN).getValue(), is("Hallo"));
        assertThat(intString1.get(Locale.ENGLISH).getValue(), is("Hello"));

        IEnumValue value2 = enumType.getEnumValues().get(1);
        IEnumAttributeValue descAttr2 = value2.getEnumAttributeValues().get(2);
        IInternationalString intString2 = (IInternationalString)descAttr2.getValue().getContent();
        assertThat(intString2.get(Locale.GERMAN).getValue(), is("Welt"));
        assertThat(intString2.get(Locale.ENGLISH).getValue(), is("World"));
    }
}
