/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import au.com.bytecode.opencsv.CSVWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to export an ipstablecontents to an text-file (comma separated values).
 * 
 * @author Roman Grutza
 */
public class CSVTableExportOperation extends AbstractTableExportOperation {

    /**
     * @param typeToExport An <code>ITableContents</code> instance.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    public CSVTableExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {
        if (!(typeToExport instanceof ITableContents)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected ITableContents, but got '" + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        messageList = list;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        IProgressMonitor localMonitor = monitor;
        if (localMonitor == null) {
            localMonitor = new NullProgressMonitor();
        }

        ITableContents contents = getTableContents(typeToExport);

        ITableRows currentTableRows = contents.getTableRows();

        localMonitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 4 + currentTableRows.getNumOfRows());

        // first of all, check if the environment allows an export...
        ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
        if (structure == null) {
            String text = NLS.bind(Messages.TableExportOperation_errStructureNotFound, contents.getTableStructure());
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }
        localMonitor.worked(1);

        messageList.add(contents.validate(contents.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }
        localMonitor.worked(1);

        messageList.add(structure.validate(contents.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }

        // if we have reached here, the environment is valid, so try to export the data
        localMonitor.worked(1);

        FileOutputStream out = null;
        CSVWriter writer = null;
        try {
            if (!localMonitor.isCanceled()) {
                // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und
                // Zielordner
                out = new FileOutputStream(new File(filename));

                char fieldSeparatorChar = getFieldSeparatorCSV(format);
                writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(out)), fieldSeparatorChar);

                exportHeader(writer, structure.getColumns(), exportColumnHeaderRow);

                localMonitor.worked(1);

                exportDataCells(writer, contents, currentTableRows, structure, localMonitor, exportColumnHeaderRow);
                writer.close();
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        } finally {
            if (out != null) {
                try {

                    out.close();
                    // CSOFF: Empty Statement
                } catch (IOException ee) {
                    // ignore
                }
                // CSON: Empty Statement
            }
        }
    }

    private ITableContents getTableContents(IIpsObject typeToExport) {
        if (typeToExport instanceof ITableContents) {
            return (ITableContents)typeToExport;
        }
        return null;
    }

    /**
     * Writes the CSV header containing the names of the columns using the given CSV writer.
     * 
     * @param writer A CSV writer instance
     * @param columns The tablescontents` columns
     * @param exportColumnHeaderRow Flag to indicate whether to export the header
     */
    private void exportHeader(CSVWriter writer, IColumn[] columns, boolean exportColumnHeaderRow) {
        if (exportColumnHeaderRow) {
            String[] header = new String[columns.length];
            for (int i = 0; i < header.length; i++) {
                header[i] = columns[i].getName();
            }
            writer.writeNext(header);
        }
    }

    /**
     * Create the cells for the export
     * 
     * @param writer A CSV writer instance.
     * @param contents The tablecontents for export.
     * @param generation The generation of the content to get the values from.
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not.
     * 
     * @throws CoreException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(CSVWriter writer,
            ITableContents contents,
            ITableRows generation,
            ITableStructure structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreException {

        Datatype[] datatypes = new Datatype[contents.getNumOfColumns()];
        for (int i = 0; i < datatypes.length; i++) {
            String datatype = structure.getColumns()[i].getDatatype();
            datatypes[i] = structure.getIpsProject().findDatatype(datatype);
        }
        IRow[] contentRows = generation.getRows();
        for (int i = 0; i < contentRows.length; i++) {
            IRow row = generation.getRow(i);

            String[] fieldsToExport = new String[contents.getNumOfColumns()];
            for (int j = 0; j < contents.getNumOfColumns(); j++) {
                String ipsValue = row.getValue(j);
                Object obj = format.getExternalValue(ipsValue, datatypes[j], messageList);

                String csvField;
                try {
                    csvField = (obj == null) ? nullRepresentationString : obj.toString();
                } catch (NumberFormatException e) {
                    // Null Object for Decimal Datatype returned, see Null-Object Pattern
                    csvField = nullRepresentationString;
                }

                fieldsToExport[j] = csvField;
            }

            writer.writeNext(fieldsToExport);

            if (monitor.isCanceled()) {
                return;
            }

            monitor.worked(1);
        }
    }

}
