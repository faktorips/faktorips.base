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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Abstract Operation to export ipstablecontents in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use the
 * <code>org.faktorips.devtools.core.externalTableFormat</code> extension point to register the new
 * table format.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableExportOperation implements IWorkspaceRunnable {

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
    public abstract void run(IProgressMonitor monitor) throws CoreException;

    protected char getFieldSeparatorCSV(ITableFormat tableFormat) {
        String fieldSeparator = tableFormat.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        char fieldSeparatorChar = ',';
        if (fieldSeparator != null && fieldSeparator.length() == 1) {
            fieldSeparatorChar = fieldSeparator.charAt(0);
        }
        return fieldSeparatorChar;
    }

}
