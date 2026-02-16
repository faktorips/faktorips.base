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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.testsupport.IpsMatchers;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ExcelTableExportOperationTest extends AbstractTableTest {

    private ExcelTableFormat format;
    private String filename;
    private IIpsProject ipsProject;

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
        format.setDefaultExtension(".xls");
        format.setName("Excel");
        format.addValueConverter(new BooleanValueConverter());
        format.addValueConverter(new DecimalValueConverter());
        format.addValueConverter(new DoubleValueConverter());
        format.addValueConverter(new DateValueConverter());
        format.addValueConverter(new GregorianCalendarValueConverter());
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        filename = "table" + format.getDefaultExtension();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        new File(filename).delete();
    }

    @Test
    public void testExportValid() throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertThat(ml, IpsMatchers.isEmpty());
    }

    @Test
    public void testExportInvalid() throws Exception {
        ITableContents contents = createInvalidTableContents(ipsProject);

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(6, ml.size());
    }

    @Test
    public void testExportEnumWithoutNameAndIdFormat() throws Exception {
        ITableContents contents = createTableContentsWithEnum(ipsProject);

        format.setProperty(ExcelTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "false");

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        String cellValue = readExcelCell(filename, 1, 1);
        assertThat(cellValue, is("1"));
    }

    @Test
    public void testExportEnumWithNameAndIdFormat() throws Exception {
        ITableContents contents = createTableContentsWithEnum(ipsProject);

        format.setProperty(ExcelTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "true");

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        String cellValue = readExcelCell(filename, 1, 1);
        assertThat(cellValue.contains("Jährlich (1)"), is(true));
    }

    private String readExcelCell(String filepath, int rowIndex, int cellIndex) throws Exception {
        try (FileInputStream fis = new FileInputStream(filepath);
                Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(rowIndex);
            return row.getCell(cellIndex).getStringCellValue();
        }
    }

}
