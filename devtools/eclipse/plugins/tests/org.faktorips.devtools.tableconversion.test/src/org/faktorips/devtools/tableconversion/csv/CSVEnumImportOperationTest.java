/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.tableconversion.csv;

import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.containsNoErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasMessageThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileWriter;

import com.opencsv.CSVWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CSVEnumImportOperationTest extends AbstractTableTest {

    // private ITableRows importTarget;
    private CSVTableFormat format;
    private IIpsProject ipsProject;

    private File file;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("test");
        IIpsProjectProperties props = ipsProject.getProperties();
        String[] datatypes = getColumnDatatypes();
        props.setPredefinedDatatypesUsed(datatypes);
        ipsProject.setProperties(props);

        format = new CSVTableFormat();
        format.setName("CSV");
        format.setDefaultExtension(".csv");
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

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testImportValid_EnumContent() throws Exception {
        MessageList ml = new MessageList();
        executeImportEnumContent(ml, true);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, false);

        assertThat(5, equalTo(enumType.getEnumValuesCount()));
    }

    @Test
    public void testImportFirstRowContainsColumnHeader() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = executeImportEnumType(ml, true);

        assertThat(4, equalTo(enumType.getEnumValuesCount()));
    }

    @Test
    public void testImportInvalidFormat() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = createExternalEnumType();
        enumType.clear();

        createInvalidCsvFile();
        CSVEnumImportOperation op = new CSVEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, containsErrorMessage());
        assertThat(6, equalTo(ml.size()));
        assertThat(ml,
                hasMessageThat(containsText("INVALID1").and(containsText("String")).and(containsText("Decimal"))));
        assertThat(ml,
                hasMessageThat(containsText("INVALID2").and(containsText("String")).and(containsText("Double"))));
        assertThat(ml,
                hasMessageThat(
                        containsText("INVALID3").and(containsText("String")).and(containsText("GregorianCalendar"))));
        assertThat(ml,
                hasMessageThat(containsText("INVALID4").and(containsText("String")).and(containsText("Integer"))));
        assertThat(ml,
                hasMessageThat(containsText("INVALID5").and(containsText("String")).and(containsText("Long"))));
        assertThat(ml,
                hasMessageThat(containsText("INVALID6").and(containsText("String")).and(containsText("Money"))));
    }

    @Test
    public void testImportEmptyCellCells() throws Exception {
        MessageList ml = new MessageList();
        IEnumType enumType = createExternalEnumTypeWithStrings();
        enumType.clear();

        createCsvFileWithEmptyCells();
        CSVEnumImportOperation op = new CSVEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(3, equalTo(ml.size()));
        assertThat(ml, containsNoErrorMessage());
        assertThat(ml.toString(), containsString("no value is set - imported NULL instead."));
    }

    @Test
    public void testImportEnumExpectedFieldsMismatch() throws Exception {
        IEnumType enumType = createExternalEnumTypeWithStrings();
        enumType.clear();

        createCsvFileExpectedFieldsMismatch();
        MessageList ml = new MessageList();
        CSVEnumImportOperation op = new CSVEnumImportOperation(enumType, file.getName(), format, "NULL", true, ml);
        op.run(new NullProgressMonitor());

        assertThat(ml, containsErrorMessage());
        assertThat(1, equalTo(ml.size()));
        assertThat(ml.toString(), containsString("Row 2 did not match the expected format."));
    }

    private IEnumType createExternalEnumType() throws Exception {
        // create ips src file
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        // create enum.xls
        CSVEnumExportOperation csvEnumExportOperation = new CSVEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, new MessageList());
        csvEnumExportOperation.run(null);
        return enumType;
    }

    private IEnumType createExternalEnumTypeWithStrings() throws Exception {
        // create ips src file
        IEnumType enumType = createValidEnumTypeWithStringValues(ipsProject);

        // create enum.xls
        CSVEnumExportOperation csvEnumExportOperation = new CSVEnumExportOperation(enumType, file.getName(),
                format, "NULL", true, new MessageList());
        csvEnumExportOperation.run(null);
        return enumType;
    }

    private IEnumType executeImportEnumType(MessageList ml, boolean containsHeader) throws Exception, CoreException {
        IEnumType enumType = createExternalEnumType();

        // clear the exported file for reimport (keeping the attributes)
        enumType.clear();

        CSVEnumImportOperation op = new CSVEnumImportOperation(enumType, file.getName(), format, "NULL",
                containsHeader, ml);
        op.run(new NullProgressMonitor());
        return enumType;
    }

    private IEnumContent createExternalEnumContent() throws Exception {
        // create ips src file
        IEnumContent enumContent = createValidEnumContentWithValues(ipsProject);

        // create enum.xls
        CSVEnumExportOperation CSVEnumExportOperation = new CSVEnumExportOperation(enumContent, file.getName(),
                format, "NULL", true, new MessageList());
        CSVEnumExportOperation.run(null);
        return enumContent;
    }

    private IEnumContent executeImportEnumContent(MessageList ml, boolean containsHeader)
            throws Exception, CoreException {
        IEnumContent enumContent = createExternalEnumContent();

        // clear the exported file for reimport (keeping the attributes)
        enumContent.clear();

        CSVEnumImportOperation op = new CSVEnumImportOperation(enumContent, file.getName(), format, "NULL",
                containsHeader, ml);
        op.run(new NullProgressMonitor());
        return enumContent;
    }

    private void createInvalidCsvFile() throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));

        String[] invalidLine = { "ValidLITERAL_NAME", "ValidBoolean", "INVALID1", "INVALID2", "INVALID3",
                "INVALID4", "INVALID5", "INVALID6", "ValidString" };

        writer.writeNext(new String[] { "This", "is", "the", "header." });
        writer.writeNext(invalidLine);
        writer.close();
    }

    private void createCsvFileWithEmptyCells() throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));

        String[] nullValueLine = { "LITERAL_NAME", null, null, null };

        writer.writeNext(new String[] { "This", "is", "the", "header." });
        writer.writeNext(nullValueLine);
        writer.close();
    }

    private void createCsvFileExpectedFieldsMismatch() throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));

        String[] nullValueLine = { "LITERAL_NAME", "String1", "String2", "String3", "String4" };

        writer.writeNext(new String[] { "This", "is", "the", "header." });
        writer.writeNext(nullValueLine);
        writer.close();
    }

}
