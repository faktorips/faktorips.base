/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to export an ipstablecontents to an excel-file.
 * 
 * @author Thorsten Waertel, Thorsten Guenther, Alexander Weickmann
 */
public class ExcelTableExportOperation extends AbstractExcelExportOperation {

    /**
     * 
     * 
     * @param typeToExport An <code>ITableContents</code> instance.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    public ExcelTableExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {

        super(typeToExport, filename, format, nullRepresentationString, exportColumnHeaderRow, list);
        if (!(typeToExport instanceof ITableContents)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected ITableContents, but got '" + typeToExport == null ? "null" //$NON-NLS-1$ //$NON-NLS-2$
                            : typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        // Currently, there is only one generation per table contents
        ITableContents contents = getTableContents(typeToExport);
        IIpsObjectGeneration[] gens = contents.getGenerationsOrderedByValidDate();
        ITableContentsGeneration currentGeneration = (ITableContentsGeneration)gens[0];

        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 2 + currentGeneration.getNumOfRows());

        initWorkbookAndSheet();
        monitor.worked(1);

        ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
        exportHeader(sheet, structure.getColumns(), exportColumnHeaderRow);
        monitor.worked(1);
        if (monitor.isCanceled()) {
            return;
        }

        exportDataCells(sheet, currentGeneration, structure, monitor, exportColumnHeaderRow);
        if (monitor.isCanceled()) {
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(filename));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
        monitor.done();
    }

    private ITableContents getTableContents(IIpsObject ipsObject) {
        if (ipsObject instanceof ITableContents) {
            return (ITableContents)ipsObject;
        }
        return null;
    }

    /**
     * Create the header as first row.
     * 
     * @param sheet The sheet where to create the header.
     * @param columns The columns defined by the structure.
     * @param exportColumnHeaderRow column header names included or not.
     */
    private void exportHeader(HSSFSheet sheet, IColumn[] columns, boolean exportColumnHeaderRow) {
        if (!exportColumnHeaderRow) {
            return;
        }
        HSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell((short)i).setCellValue(columns[i].getName());
        }
    }

    /**
     * Create the cells for the export
     * 
     * @param sheet The sheet to create the cells within.
     * @param generation The generation of the content to get the values from.
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not.
     * 
     * @throws CoreException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(HSSFSheet sheet,
            ITableContentsGeneration generation,
            ITableStructure structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreException {

        ITableContents contents = getTableContents(typeToExport);

        // init datatypes
        Datatype[] datatypes = new Datatype[contents.getNumOfColumns()];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(structure.getColumns()[i].getDatatype());
        }

        IRow[] contentRows = generation.getRows();
        int offset = exportColumnHeaderRow ? 1 : 0;
        for (int i = 0; i < contentRows.length; i++) {
            HSSFRow sheetRow = sheet.createRow(i + offset);
            for (int j = 0; j < contents.getNumOfColumns(); j++) {
                HSSFCell cell = sheetRow.createCell((short)j);
                fillCell(cell, contentRows[i].getValue(j), datatypes[j]);
            }
            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

}
