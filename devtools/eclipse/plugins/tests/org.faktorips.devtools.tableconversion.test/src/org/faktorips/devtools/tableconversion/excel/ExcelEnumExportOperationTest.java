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

import java.io.File;

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

}
