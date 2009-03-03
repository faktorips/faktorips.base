/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.extsystems.csv;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.extsystems.AbstractTableTest;
import org.faktorips.devtools.extsystems.excel.BooleanValueConverter;
import org.faktorips.devtools.extsystems.excel.DateValueConverter;
import org.faktorips.devtools.extsystems.excel.DecimalValueConverter;
import org.faktorips.devtools.extsystems.excel.DoubleValueConverter;
import org.faktorips.devtools.extsystems.excel.IntegerValueConverter;
import org.faktorips.devtools.extsystems.excel.LongValueConverter;
import org.faktorips.devtools.extsystems.excel.MoneyValueConverter;
import org.faktorips.devtools.extsystems.excel.StringValueConverter;
import org.faktorips.util.message.MessageList;


public class CSVTableExportOperationTest extends AbstractTableTest {
    
    private CSVTableFormat format;
    private String filename;
    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();

        this.ipsProject = newIpsProject("test");
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
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        this.filename = "table" + format.getDefaultExtension();
    }

    protected void tearDownExtension() throws Exception {
        new File(filename).delete();
    }

    public void testExportValid() throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);
        
        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        System.out.println(ml);
        assertTrue(ml.isEmpty());
    }

    public void testExportValidRowMismatch() throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);

        // too many columns
        IColumn col = getStructure().newColumn();

        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml );
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

    public void testExportInvalid() throws Exception {
        ITableContents contents = createInvalidTableContents(ipsProject);
        
        MessageList ml = new MessageList();
        CSVTableExportOperation op = new CSVTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(7, ml.getNoOfMessages()); // from 8 columns only the String column is valid
    }

}
