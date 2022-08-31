/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

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
                    "The given IPS object is not supported. Expected ITableContents, but got '" //$NON-NLS-1$
                            + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        messageList = list;
    }

    @Override
    public void run(IProgressMonitor monitor) {
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

        char fieldSeparatorChar = getFieldSeparatorCSV(format);
        try (FileOutputStream out = new FileOutputStream(new File(filename));
                ICSVWriter writer = new CSVWriterBuilder(new BufferedWriter(new OutputStreamWriter(out)))
                        .withSeparator(fieldSeparatorChar).build()) {
            if (!localMonitor.isCanceled()) {
                // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und
                // Zielordner
                exportHeader(writer, structure.getColumns(), exportColumnHeaderRow);

                localMonitor.worked(1);

                exportDataCells(writer, contents, currentTableRows, structure, localMonitor, exportColumnHeaderRow);
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
    }

    /**
     * Create the cells for the export
     * 
     * @param writer A CSV writer instance.
     * @param contents The table contents for export.
     * @param tableRows The rows of the content to get the values from.
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not.
     * 
     * @throws IpsException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(ICSVWriter writer,
            ITableContents contents,
            ITableRows tableRows,
            ITableStructure structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) {

        List<Datatype> datatypes = new ArrayList<>(contents.getNumOfColumns());
        for (IColumn column : structure.getColumns()) {
            Datatype datatype = structure.getIpsProject().findDatatype(column.getDatatype());
            datatypes.add(datatype);
        }

        int numOfColumns = structure.getNumOfColumns();
        String[] fieldsToExport = new String[numOfColumns];
        for (IRow row : tableRows.getRows()) {
            for (int i = 0; i < numOfColumns; i++) {
                fieldsToExport[i] = valueToCsv(row.getValue(i), datatypes.get(i), nullRepresentationString,
                        messageList);
            }
            writer.writeNext(fieldsToExport);

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private String valueToCsv(String ipsValue,
            Datatype datatype,
            String nullRepresentationString,
            MessageList messageList) {
        Object obj = format.getExternalValue(ipsValue, datatype, messageList);

        String csvField;
        try {
            csvField = (obj == null) ? nullRepresentationString : obj.toString();
        } catch (NumberFormatException e) {
            // Null Object for Decimal Datatype returned, see Null-Object Pattern
            csvField = nullRepresentationString;
        }
        return csvField;
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
     * @param columns The table content's columns
     * @param exportColumnHeaderRow Flag to indicate whether to export the header
     */
    private void exportHeader(ICSVWriter writer, IColumn[] columns, boolean exportColumnHeaderRow) {
        if (exportColumnHeaderRow) {
            String[] header = new String[columns.length];
            for (int i = 0; i < header.length; i++) {
                header[i] = columns[i].getName();
            }
            writer.writeNext(header);
        }
    }

}
