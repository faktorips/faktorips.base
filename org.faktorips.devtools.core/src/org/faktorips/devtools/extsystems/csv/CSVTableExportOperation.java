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

package org.faktorips.devtools.extsystems.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.devtools.extsystems.excel.AbstractTableExportOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to export an ipstablecontents to an text-file (comma separated values).
 * 
 * @author Roman Grutza
 */
public class CSVTableExportOperation extends AbstractTableExportOperation {

    /* The maximum number of rows allowed in an Excel sheet */
    private static final short MAX_ROWS = Short.MAX_VALUE;

    /**
     * @param contents The contents to export.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    public CSVTableExportOperation(ITableContents contents, String filename, AbstractExternalTableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {
        this.contents = contents;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        this.messageList = list;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        IIpsObjectGeneration[] gens = contents.getGenerationsOrderedByValidDate();
        if (gens.length == 0) {
            String text = NLS.bind(Messages.TableExportOperation_errNoGenerations, contents.getName());
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }

        // currently, there is only one generation per table contents
        ITableContentsGeneration currentGeneration = (ITableContentsGeneration)gens[0];

        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 5 + currentGeneration.getNumOfRows());

        // first of all, check if the environment allows an export...
        ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
        if (structure == null) {
            String text = NLS.bind(Messages.TableExportOperation_errStructureNotFound, contents.getTableStructure());
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }
        monitor.worked(1);

        messageList.add(contents.validate(contents.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }
        monitor.worked(1);

        messageList.add(structure.validate(contents.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }
        monitor.worked(1);

        if (structure.getNumOfColumns() > MAX_ROWS) {
            Object[] objects = new Object[3];
            objects[0] = new Integer(structure.getNumOfColumns());
            objects[1] = structure;
            objects[2] = new Short(MAX_ROWS);
            String text = NLS.bind(Messages.TableExportOperation_errStructureTooMuchColumns, objects);
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }

        // if we have reached here, the environment is valid, so try to export the data
        monitor.worked(1);

        FileOutputStream out = null;
        try {
            if (!monitor.isCanceled()) {
                // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und Zielordner
                out = new FileOutputStream(new File(filename));
                exportHeader(out, structure.getColumns(), exportColumnHeaderRow);

                monitor.worked(1);

                exportDataCells(out, contents, currentGeneration, structure, monitor, exportColumnHeaderRow);
                out.close();
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ee) {
                    // ignore
                }
            }
        }
    }

    /**
     * Exports a header containing the names of the columns.
     * 
     * @param out Output stream to write to
     * @param columns The tablescontents` columns
     * @param exportColumnHeaderRow Flag to indicate whether to export the header
     */
    private void exportHeader(FileOutputStream out, IColumn[] columns, boolean exportColumnHeaderRow)
            throws IOException {
        if (!exportColumnHeaderRow) {
            return;
        }
        for (int i = 0; i < columns.length; i++) {
            out.write(columns[i].getName().getBytes());
            out.write(",".getBytes()); // FIXME: hard coded field delimiter
        }
        out.write("\n".getBytes());
    }

    /**
     * Create the cells for the export
     * 
     * @param out Output stream to write to
     * @param contents The tablecontents for export
     * @param generation The generation of the content to get the values from
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not
     * 
     * @throws CoreException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(FileOutputStream out,
            ITableContents contents,
            ITableContentsGeneration generation,
            ITableStructure structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreException, IOException {

        Datatype[] datatypes = new Datatype[contents.getNumOfColumns()];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(structure.getColumns()[i].getDatatype());
        }
        IRow[] contentRows = generation.getRows();
        for (int i = 0; i < contentRows.length; i++) {
            IRow row = generation.getRow(i);
            
            for (int j = 0; j < contents.getNumOfColumns(); j++) {
                String ipsValue = row.getValue(j);
                Object obj = format.getExternalValue(ipsValue, datatypes[j], messageList);
                
                String csvField;
                try {
                    csvField = (obj == null) ? nullRepresentationString
                            : format.getIpsValue(obj, datatypes[j], messageList);
                } catch (NumberFormatException e) {
                    // Null Object for Decimal Datatype returned, see Null-Object Pattern
                    csvField = nullRepresentationString; 
                }
                
             //TODO: text/field delimiters !
                out.write(csvField.getBytes());
                if (j  < contents.getNumOfColumns() - 1) {
                    out.write(",".getBytes());  // field separator
                }
            }

            out.write("\n".getBytes());
            if (monitor.isCanceled()) {
                return;
            }

            monitor.worked(1);
        }
    }

}
