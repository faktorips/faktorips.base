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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import com.opencsv.CSVWriter;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CSVTableImportOperationTest extends AbstractTableTest {

    private ITableRows importTarget;
    private AbstractExternalTableFormat format;

    private File file;

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
        createValidExternalTable(ipsProject, format, true);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    @Test
    public void testImportValidRowMismatch() throws Exception {
        createValidExternalTable(ipsProject, format, true);

        // too many columns
        IColumn col = getStructure().newColumn();

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml, true);
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
        op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format, "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    @Test
    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        createValidExternalTable(ipsProject, format, false);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", false, ml, true);
        op.run(new NullProgressMonitor());
        assertFalse(ml.containsErrorMsg());
        assertEquals(3, importTarget.getNumOfRows());
    }

    @Test
    public void testImportFirstRowContainsColumnHeader() throws Exception {
        createValidExternalTable(ipsProject, format, true);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml, true);
        op.run(new NullProgressMonitor());
        assertFalse(ml.containsErrorMsg());
        assertEquals(3, importTarget.getNumOfRows());
    }

    @Test
    public void testImportInvalid() throws Exception {
        createInvalidCsvFile();

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(createTableStructure(ipsProject), file.getName(),
                importTarget, format, "NULL", true, ml, true);

        op.run(new NullProgressMonitor());
        assertEquals(6, ml.size());
    }

    private void createInvalidCsvFile() throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));

        String[] invalidLine = new String[] { "invalid is impossible", "INVALID", "INVALID", "INVALID", "INVALID",
                "INVALID", "INVALID", "invalid is impossible" };

        writer.writeNext(new String[] { "This", "is", "the", "header." });
        writer.writeNext(invalidLine);
        writer.close();
    }

}
