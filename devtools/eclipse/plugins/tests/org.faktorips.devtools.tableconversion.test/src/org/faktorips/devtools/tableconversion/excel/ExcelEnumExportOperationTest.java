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

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class ExcelEnumExportOperationTest extends AbstractTableTest {

    private ITableFormat format;
    private String filename;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = initializeIpsProject("test");

        format = new ExcelTableFormat();
        format.setDefaultExtension(".xls");
        format.setName("Excel");
        format.addValueConverter(new BooleanValueConverter());
        format.addValueConverter(new DecimalValueConverter());
        format.addValueConverter(new DoubleValueConverter());
        format.addValueConverter(new DateValueConverter());
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        filename = "enum" + format.getDefaultExtension();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        new File(filename).delete();
    }

    @Test
    public void testExportValid_EnumType() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());
    }

    @Test
    public void testExportValid_EnumContent() throws Exception {
        IEnumContent enumContent = createValidEnumContentWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumContent, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());
    }

    @Test
    public void testExportWithIdAndName_EnumType() throws Exception {
        IEnumType enumReferencingEnum = createEnumReferencingEnum(ipsProject);
        format.setProperty(ExcelTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "true");
        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumReferencingEnum, filename, format, "NULL", true,
                ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        String cellValue = readExcelCell(filename, 1, 2);

        assertThat(cellValue, Matchers.containsString("Jährlich (1)"));
    }

    @Test
    public void testExportWithIdAndName_EnumContent() throws Exception {
        var modelProject = initializeIpsProject("model");
        setProjectProperty(ipsProject, p -> {
            var ipsObjectPath = p.getIpsObjectPath();
            ipsObjectPath.newIpsProjectRefEntry(modelProject);
            p.setIpsObjectPath(ipsObjectPath);
        });
        IEnumContent enumReferencingEnum = createEnumReferencingEnumInSeparateProjects(modelProject, ipsProject);
        format.setProperty(ExcelTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "true");
        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumReferencingEnum, filename, format, "NULL", true,
                ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        String cellValue = readExcelCell(filename, 1, 1);

        assertThat(cellValue, Matchers.containsString("Jährlich (1)"));
    }

    @Test
    public void testExportValid_ExtensibleEnum() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);
        enumType.setExtensible(true);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());
    }

    @Test
    public void testExportMultilingual_HeaderContainsMergedLocaleColumns() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        try (FileInputStream fis = new FileInputStream(filename);
                Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            // LITERAL_NAME (col 0), id (col 1), description (col 2-3 merged)
            assertThat(headerRow.getCell(0).getStringCellValue(), is("LITERAL_NAME"));
            assertThat(headerRow.getCell(1).getStringCellValue(), is("id"));
            assertThat(headerRow.getCell(2).getStringCellValue(), is("description"));

            // description should be merged across cols 2-3 (horizontal merge in row 0)
            boolean foundDescriptionMerge = false;
            for (CellRangeAddress region : sheet.getMergedRegions()) {
                if (region.getFirstRow() == 0 && region.getLastRow() == 0
                        && region.getFirstColumn() == 2 && region.getLastColumn() == 3) {
                    foundDescriptionMerge = true;
                    break;
                }
            }
            assertThat(foundDescriptionMerge, is(true));

            // Row 1: locale sub-headers
            Row localeRow = sheet.getRow(1);
            assertThat(localeRow.getCell(2).getStringCellValue(), is("[de]"));
            assertThat(localeRow.getCell(3).getStringCellValue(), is("[en]"));
        }
    }

    @Test
    public void testExportMultilingual_DataContainsAllLocales() throws Exception {
        IEnumType enumType = createMultilingualEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        try (FileInputStream fis = new FileInputStream(filename);
                Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Data starts at row 2 (after 2-row header)
            Row dataRow1 = sheet.getRow(2);
            // col 0 = literalName, col 1 = id, col 2 = description[de], col 3 = description[en]
            assertThat(dataRow1.getCell(1).getStringCellValue(), is("id1"));
            assertThat(dataRow1.getCell(2).getStringCellValue(), is("Beschreibung1"));
            assertThat(dataRow1.getCell(3).getStringCellValue(), is("Description1"));

            Row dataRow2 = sheet.getRow(3);
            assertThat(dataRow2.getCell(1).getStringCellValue(), is("id2"));
            assertThat(dataRow2.getCell(2).getStringCellValue(), is("Beschreibung2"));
            assertThat(dataRow2.getCell(3).getStringCellValue(), is("Description2"));
        }
    }

    @Test
    public void testExportMultilingual_EnumContent() throws Exception {
        IEnumContent enumContent = createMultilingualEnumContentWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumContent, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        try (FileInputStream fis = new FileInputStream(filename);
                Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            // EnumContent does NOT include literalName, so: id (col 0), description (col 1-2 merged)
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue(), is("id"));
            assertThat(headerRow.getCell(1).getStringCellValue(), is("description"));

            Row localeRow = sheet.getRow(1);
            assertThat(localeRow.getCell(1).getStringCellValue(), is("[de]"));
            assertThat(localeRow.getCell(2).getStringCellValue(), is("[en]"));

            // Data starts at row 2
            Row dataRow1 = sheet.getRow(2);
            assertThat(dataRow1.getCell(0).getStringCellValue(), is("id1"));
            assertThat(dataRow1.getCell(1).getStringCellValue(), is("Deutsch1"));
            assertThat(dataRow1.getCell(2).getStringCellValue(), is("English1"));
        }
    }

    @Test
    public void testExportNonMultilingual_SingleRowHeader() throws Exception {
        // Existing non-multilingual enum should still produce single-row header
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, isEmpty());

        try (FileInputStream fis = new FileInputStream(filename);
                Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);

            // No horizontal merged regions should exist (only non-multilingual attrs)
            boolean hasHorizontalMerge = false;
            for (CellRangeAddress region : sheet.getMergedRegions()) {
                if (region.getFirstRow() == 0 && region.getLastRow() == 0
                        && region.getLastColumn() > region.getFirstColumn()) {
                    hasHorizontalMerge = true;
                    break;
                }
            }
            assertThat(hasHorizontalMerge, is(false));

            // Data should start at row 1 (single header row)
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue(), is("SIMPLE_TEXT"));
        }
    }

}
