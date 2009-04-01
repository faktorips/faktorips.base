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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

/**
 * Abstract Operation to import ipstablecontents in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use
 * the <code>org.faktorips.devtools.core.externalTableFormat</code> extension point
 * to register the new table format.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableImportOperation implements IWorkspaceRunnable {

    /**
     * Qualified name of the file to import from
     */
    protected String sourceFile;
    
    /**
     * The table structure the imported table content is bound to
     */
    protected ITableStructure structure;
    
    /**
     * Generation of the table contents the import has to be inserted.
     */
    protected ITableContentsGeneration targetGeneration;
    
    /**
     * Datatypes for the columns. The datatype at index 1 is the datatype defined in the structure
     * for column at index 1.
     */
    protected Datatype[] datatypes;
    
    /**
     * The format which handles data conversion
     */
    protected ITableFormat format;
    
    /**
     * String representing <code>null</code>.
     */
    protected String nullRepresentationString;
    
    /**
     * List of messages describing problems occurred during import.
     */
    protected MessageList messageList;
    
    /**
     * <code>true</code> if the first row contains column header and should be ignored
     * <code>false</code> if the to be imported content contains no column header row.
     */
    protected boolean ignoreColumnHeaderRow;

    
    public AbstractTableImportOperation(ITableStructure structure, String sourceFile,
            ITableContentsGeneration targetGeneration, ITableFormat format,
            String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList list) {

        this.sourceFile = sourceFile;
        this.structure = structure;
        this.targetGeneration = targetGeneration;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        this.messageList = list;
        
        initDatatypes(structure);
    }

    protected void initDatatypes(ITableStructure structure) {
        try {
            IColumn[] columns = structure.getColumns();
            datatypes = new Datatype[columns.length];
            for (int i = 0; i < columns.length; i++) {
                datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public abstract void run(IProgressMonitor monitor) throws CoreException;

}
