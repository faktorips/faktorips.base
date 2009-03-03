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

package org.faktorips.devtools.extsystems.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import org.faktorips.devtools.extsystems.AbstractTableExportOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to export an ipstablecontents to an excel-file.
 * 
 * @author Thorsten Waertel, Thorsten Guenther
 */
public class ExcelTableExportOperation extends AbstractTableExportOperation {

    /* The maximum number of rows allowed in an Excel sheet */
    private static final short MAX_ROWS = Short.MAX_VALUE;

    /* Type to be used for cells with a date. Dates a treated as numbers by excel, so the only way
     * to display a date as a date and not as a stupid number is to format the cell :-( */
    private HSSFCellStyle dateStyle = null;

    /**
     * datatypes for the columns. The datatype at index 1 is the datatype defined in the structure
     * for column at index 1.
     */
    private Datatype[] datatypes;

    /**
     * @param contents The contents to export.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the exported format
     * @param list A MessageList to store errors and warnings which happened during the export 
     */
    public ExcelTableExportOperation(ITableContents contents, String filename, AbstractExternalTableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {
        this.contents = contents;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        this.messageList = list;
    }

    /**
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
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        // create style for cells which represent a date - excel represents date as
        // a number and has no internal type for dates, so this has to be done by styles :-(
        dateStyle = workbook.createCellStyle();
        // user defined style dd.MM.yyyy, hopefully works on all excel installations...
        dateStyle.setDataFormat((short)27);

        monitor.worked(1);

        // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und Zielordner
        createHeader(sheet, structure.getColumns(), exportColumnHeaderRow);

        monitor.worked(1);

        createDataCells(sheet, currentGeneration, structure, monitor, exportColumnHeaderRow);

        try {
            if (!monitor.isCanceled()) {
                FileOutputStream out = new FileOutputStream(new File(filename));
                workbook.write(out);
                out.close();
            }
        }
        catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
    }

    /**
     * Create the header as first row.
     * 
     * @param sheet The sheet where to create the header
     * @param columns The columsn defined by the Structure.
     * @param exportColumnHeaderRow2 
     * 
     * @throws CoreException
     */
    private void createHeader(HSSFSheet sheet, IColumn[] columns, boolean exportColumnHeaderRow) throws CoreException {
        if (!exportColumnHeaderRow){
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
     * @param sheet The sheet to create the cells within
     * @param generation The generation of the content to get the values from
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not
     * 
     * @throws CoreException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void createDataCells(HSSFSheet sheet,
            ITableContentsGeneration generation,
            ITableStructure structure,
            IProgressMonitor monitor, boolean exportColumnHeaderRow) throws CoreException {

        datatypes = new Datatype[contents.getNumOfColumns()];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(structure.getColumns()[i].getDatatype());
        }

        IRow[] contentRows = generation.getRows();
        int offest = exportColumnHeaderRow ? 1 : 0;
        for (int i = 0; i < generation.getRows().length; i++) {
            HSSFRow sheetRow = sheet.createRow(i + offest);

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

    /**
     * Fill the content of the cell.
     * 
     * @param cell The cell to set the value.
     * @param ipsValue The ips-string representing the value.
     * @param datatype The datatype defined for the value.
     */
    private void fillCell(HSSFCell cell, String ipsValue, Datatype datatype) {
        Object obj = format.getExternalValue(ipsValue, datatype, messageList);

        if (obj instanceof Date) {
            cell.setCellValue((Date)obj);
            cell.setCellStyle(dateStyle);
            return;
        }
        if (obj instanceof Number) {
            try {
            cell.setCellValue(((Number)obj).doubleValue());
            }
            catch (NullPointerException npe) {
                cell.setCellValue(nullRepresentationString);
            }
            return;
        }
        if (obj instanceof Boolean) {
            cell.setCellValue(((Boolean)obj).booleanValue());
            return;
        }

        if (obj != null) {
            cell.setCellValue(obj.toString());
        }
        else {
            cell.setCellValue(nullRepresentationString);
        }

    }
}
