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

package org.faktorips.devtools.tableconversion;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Base class for all table import and export test cases. Contains factory methods to create valid and invalid
 * table contents.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableTest extends AbstractIpsPluginTest {

    private ITableStructure structure;
    
    private final String[] datatypes = new String[] { Datatype.BOOLEAN.getQualifiedName(), Datatype.DECIMAL.getQualifiedName(),
            Datatype.DOUBLE.getQualifiedName(), Datatype.GREGORIAN_CALENDAR_DATE.getQualifiedName(),
            Datatype.INTEGER.getQualifiedName(), Datatype.LONG.getQualifiedName(),
            Datatype.MONEY.getQualifiedName(), Datatype.STRING.getQualifiedName() };
    
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public ITableStructure getStructure() {
        return structure;
    }

    public String[] getColumnDatatypes() {
        return datatypes;
    }
    
    /**
     * Creates a valid source for export.
     */
    protected ITableContents createValidTableContents(IIpsProject ipsProject) throws Exception {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "ExportSource");
        ITableContentsGeneration exportSource = createExportSource(ipsProject, contents);
        
        IRow row1 = exportSource.newRow();
        row1.setValue(0, "true");
        row1.setValue(1, "12.3");
        row1.setValue(2, "" + Double.MAX_VALUE);
        row1.setValue(3, "2001-04-26");
        row1.setValue(4, "" + Integer.MAX_VALUE);
        row1.setValue(5, "" + Long.MAX_VALUE);
        row1.setValue(6, "10.23EUR");
        row1.setValue(7, "simple text");
    
        IRow row2 = exportSource.newRow();
        row2.setValue(0, "false");
        row2.setValue(1, "12.3");
        row2.setValue(2, "" + Double.MIN_VALUE);
        row2.setValue(3, "2001-04-26");
        row2.setValue(4, "" + Integer.MIN_VALUE);
        row2.setValue(5, "" + Long.MIN_VALUE);
        row2.setValue(6, "1 EUR");
        row2.setValue(7, "�������{[]}");
    
        IRow row3 = exportSource.newRow();
        row3.setValue(0, null);
        row3.setValue(1, null);
        row3.setValue(2, null);
        row3.setValue(3, null);
        row3.setValue(4, null);
        row3.setValue(5, null);
        row3.setValue(6, null);
        row3.setValue(7, null);
    
        exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);
        
        return contents;
    }

    /**
     * Creates an invalid source for export.
     */
    protected ITableContents createInvalidTableContents(IIpsProject ipsProject) throws Exception {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "ExportSource");
        ITableContentsGeneration exportSource = createExportSource(ipsProject, contents);

        IRow row1 = exportSource.newRow();
        row1.setValue(0, "INVALID"); //BOOLEAN
        row1.setValue(1, "INVALID"); //DECIMAL
        row1.setValue(2, "INVALID"); //DOUBLE
        row1.setValue(3, "INVALID"); //GREGORIAN_CALENDAR_DATE
        row1.setValue(4, "INVALID"); //INTEGER
        row1.setValue(5, "INVALID"); //LONG
        row1.setValue(6, "INVALID"); //MONEY
        row1.setValue(7, "invalid is impossible"); //STRING
    
        exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);
        
        return contents;
    }

    /**
     * Creates a test table structure based on the datatypes which are returned with a call to
     * {@link #getColumnDatatypes()}.
     * 
     * @param ipsProject The IPS project to create the table structure for.
     * @return The generated table structure.
     * @throws CoreException If the ipsProject is invalid or if this method is called more than once per test fixture.
     */
    public ITableStructure createTableStructure(IIpsProject ipsProject) throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
            "TestStructure");
        
        String[] datatypes = getColumnDatatypes();
        for (int i = 0; i < datatypes.length; i++) {
            IColumn col = structure.newColumn();
            col.setName("col" + i);
            col.setDatatype(datatypes[i]);
        }
        structure.getIpsSrcFile().save(true, null);
        
        return structure;
    }

    /**
     * Creates a valid table in the given table format stored on the filesystem. 
     * The export operation is used to create this file.
     * 
     * @param ipsProject The IPS project.
     * @param format The external table format used for export.
     * @param exportColumnHeaderRow Flag to indicate whether to create a header line in the generated file.
     */
    public void createValidExternalTable(IIpsProject ipsProject, 
            ITableFormat format, boolean exportColumnHeaderRow) throws Exception {
        ITableContents contents = createValidTableContents(ipsProject);
        
        format.executeTableExport(contents, new Path("table" + format.getDefaultExtension()), 
                "NULL", exportColumnHeaderRow, new MessageList());
    }

    /**
     * Creates an invalid table in the given table format stored on the filesystem. 
     * The export operation is used to create this file.
     * 
     * @param ipsProject The IPS project.
     * @param format The external table format used for export.
     * @param exportColumnHeaderRow Flag to indicate whether to create a header line in the generated file.
     */
    public void createInvalidExternalTable(IIpsProject ipsProject, 
            ITableFormat format, boolean exportColumnHeaderRow) throws Exception {
        ITableContents contents = createInvalidTableContents(ipsProject);
        
        format.executeTableExport(contents, new Path("table" + format.getDefaultExtension()),
                "NULL", exportColumnHeaderRow, new MessageList());
    }
    
    private ITableContentsGeneration createExportSource(IIpsProject ipsProject, ITableContents contents) throws CoreException {
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        
        structure = createTableStructure(ipsProject);
        contents.setTableStructure(structure.getQualifiedName());
        
        return (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());
    }

}
