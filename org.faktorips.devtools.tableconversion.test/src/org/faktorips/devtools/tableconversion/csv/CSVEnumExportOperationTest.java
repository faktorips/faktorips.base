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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CSVEnumExportOperationTest extends AbstractTableTest {

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
        AbstractTableExportOperation op = new CSVEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.toString(), ml.isEmpty());
    }

    @Test
    public void testExportValid_EnumContent() throws Exception {
        IEnumContent enumContent = createValidEnumContentWithValues(ipsProject);

        MessageList ml = new MessageList();
        AbstractTableExportOperation op = new CSVEnumExportOperation(enumContent, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.toString(), ml.isEmpty());
    }

    @Test
    public void testExportValidRowMismatch() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        // too many columns
        enumType.newEnumAttribute().setName("AddedColumn");

        MessageList ml = new MessageList();
        AbstractTableExportOperation op = new CSVEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // invalid structure
        ml.clear();
        enumType.getEnumAttribute("AddedColumn").delete();
        enumType.getEnumAttributes(true).get(0).setDatatype("");
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // too less columns
        ml.clear();
        enumType.getEnumAttributes(true).get(0).delete();
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    @Test
    public void testExportInvalid() throws Exception {
        IEnumType enumType = createInvalidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        AbstractTableExportOperation op = new CSVEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(8, ml.size());
    }

}
