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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.testsupport.IpsMatchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CSVTableExportOperationTest extends AbstractTableTest {

    private ITableFormat format;
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

        format = new CSVTableFormat();
        format.setDefaultExtension(".csv");
        format.setName("Text (CSV)");
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
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        System.out.println(ml.getText());
        assertThat(ml, IpsMatchers.isEmpty());
    }

    @Test
    public void testExportValidRowMismatch() throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);

        // too many columns
        IColumn col = getStructure().newColumn();

        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // invalid structure
        ml.clear();
        col.delete();
        getStructure().getColumn(0).setDatatype("");
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // too less columns
        ml.clear();
        getStructure().getColumn(0).delete();
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    @Test
    public void testExportInvalid() throws Exception {
        ITableContents contents = createInvalidTableContents(ipsProject);

        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(7, ml.size()); // from 8 columns only the String column is valid
    }

    @Test
    public void testExportEnumWithoutNameAndIdFormat() throws Exception {
        ITableContents contents = createTableContentsWithEnum(ipsProject);

        // Export without Name (ID) format option
        format.setProperty(CSVTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "false");

        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        String csvContent = readFile(filename);
        assertThat(csvContent.contains(",\"1\""), is(true));
        assertThat(csvContent.contains("Jährlich (1)"), is(false));
    }

    @Test
    public void testExportEnumWithNameAndIdFormat() throws Exception {
        ITableContents contents = createTableContentsWithEnum(ipsProject);
        // Export with Name (ID) format option enabled
        format.setProperty(CSVTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID, "true");

        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        String csvContent = readFile(filename);
        assertThat(csvContent.contains("Jährlich (1)"), is(true));
    }

    /**
     * Reads the entire content of a file.
     */
    private String readFile(String filepath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

}
