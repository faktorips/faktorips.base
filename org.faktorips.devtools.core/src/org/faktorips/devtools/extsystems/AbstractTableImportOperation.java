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

package org.faktorips.devtools.extsystems;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
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
    protected AbstractExternalTableFormat format;
    
    /**
     * String representing <code>null</code>.
     */
    protected String nullRepresentationString;
    
    /**
     * List of messages describing problems occurred during export.
     */
    protected MessageList messageList;
    
    /**
     * <code>true</code> if the first row contains column header and should be ignored
     * <code>false</code> if the to be imported content contains no column header row.
     */
    protected boolean ignoreColumnHeaderRow;


    public abstract void run(IProgressMonitor monitor) throws CoreException;

}
