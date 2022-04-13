/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;
import org.faktorips.runtime.MessageList;

/**
 * Abstract Operation to export ipstablecontents in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use the
 * <code>org.faktorips.devtools.core.externalTableFormat</code> extension point to register the new
 * table format.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableExportOperation implements ICoreRunnable {

    /**
     * The IPS object to export
     */
    protected IIpsObject typeToExport;

    /**
     * The qualified name of the target-file for export.
     */
    protected String filename;

    /**
     * The format to use to convert data.
     */
    protected ITableFormat format;

    /**
     * The string to use if the value is null.
     */
    protected String nullRepresentationString;

    /**
     * Column header names are included or not.
     */
    protected boolean exportColumnHeaderRow;

    /**
     * List of messages describing problems occurred during export.
     */
    protected MessageList messageList;

    @Override
    public abstract void run(IProgressMonitor monitor) throws IpsException;

    protected char getFieldSeparatorCSV(ITableFormat tableFormat) {
        String fieldSeparator = tableFormat.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        char fieldSeparatorChar = ',';
        if (fieldSeparator != null && fieldSeparator.length() == 1) {
            fieldSeparatorChar = fieldSeparator.charAt(0);
        }
        return fieldSeparatorChar;
    }

}
