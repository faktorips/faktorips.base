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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

public class ExcelEnumExportOperationTest extends AbstractTableTest {

    private ITableFormat format;
    private String filename;
    private IIpsProject ipsProject;
    
    protected void setUp() throws Exception {
        super.setUp();

        this.ipsProject = newIpsProject("test");
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
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        this.filename = "enum" + format.getDefaultExtension();
    }

    protected void tearDownExtension() throws Exception {
        new File(filename).delete();
    }

    public void testExportValid() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.toString(), ml.isEmpty());
    }
    
    public void testExportValidRowMismatch() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        // too many columns
        enumType.newEnumAttribute().setName("AddedColumn");

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml );
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // invalid structure
        ml.clear();
        enumType.getEnumAttribute("AddedColumn").delete();
        enumType.getEnumAttributes().get(0).setDatatype("");
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // too less columns
        ml.clear();
        enumType.getEnumAttributes().get(0).delete();
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    public void testExportInvalid() throws Exception {
        IEnumType enumType = createInvalidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertEquals(8, ml.getNoOfMessages());
    }
    
}
