/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.tableconversion;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.MessageList;

/**
 * Abstract operation to import <code>ITableContents</code> in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use the
 * <code>externalTableFormat</code> extension point to register the new table format.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableImportOperation implements ICoreRunnable {

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
        messageList = list;
        this.importIntoExisting = importIntoExisting;
    }

    @Override
    public abstract void run(IProgressMonitor monitor) throws IpsException;

}
