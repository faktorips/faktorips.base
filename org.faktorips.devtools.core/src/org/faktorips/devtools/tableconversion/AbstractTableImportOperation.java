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

package org.faktorips.devtools.tableconversion;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * Abstract operation to import <tt>ITableContents</tt> in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use the
 * <code>externalTableFormat</code> extension point to register the new table format.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableImportOperation implements IWorkspaceRunnable {

    /**
     * The qualified name of the file to import from.
     */
    protected String sourceFile;

    /**
     * Data types for the columns. The data type at index 1 is the data type defined in the
     * structure for column at index 1.
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

    protected boolean importIntoExisting;

    /**
     * <code>true</code> if the first row contains column header and should be ignored
     * <code>false</code> if the to be imported content contains no column header row.
     */
    protected boolean ignoreColumnHeaderRow;

    public AbstractTableImportOperation(String sourceFile, ITableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList list, boolean importIntoExisting) {

        this.sourceFile = sourceFile;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        this.messageList = list;
        this.importIntoExisting = importIntoExisting;
    }

    @Override
    public abstract void run(IProgressMonitor monitor) throws CoreException;

}
