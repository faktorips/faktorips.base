/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.util.message.MessageList;

public class ExcelTableExportOperationTest extends AbstractTableTest {

    private ExcelTableFormat format;
    private String filename;
    private IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
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

    public void testExportValid() throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    public void testExportInvalid() throws Exception {
        ITableContents contents = createInvalidTableContents(ipsProject);

        MessageList ml = new MessageList();
        ExcelTableExportOperation op = new ExcelTableExportOperation(contents, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(6, ml.getNoOfMessages());
    }

}
