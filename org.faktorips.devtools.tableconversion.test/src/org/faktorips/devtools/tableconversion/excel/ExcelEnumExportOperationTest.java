/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;
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

    @Override
    protected void tearDownExtension() throws Exception {
        new File(filename).delete();
    }

    @Test
    public void testExportValid() throws Exception {
        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        MessageList ml = new MessageList();
        ExcelEnumExportOperation op = new ExcelEnumExportOperation(enumType, filename, format, "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.toString(), ml.isEmpty());
    }

}
