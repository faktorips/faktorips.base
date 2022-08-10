/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.runtime.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
abstract class AbstractExcelExportOperation extends AbstractTableExportOperation {

    private static final String XLSX_FILE_EXTENSION = ".xlsx"; //$NON-NLS-1$

    /**
     * Type to be used for cells with a date. Dates a treated as numbers by excel, so the only way
     * to display a date as a date and not as a stupid number is to format the cell :-(
     */
    private CellStyle dateStyle;

    private Workbook workbook;

    private Sheet sheet;

    /**
     * 
     * 
     * @param typeToExport An <code>IIpsObject</code> instance.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    protected AbstractExcelExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {

        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        this.messageList = list;
    }

    protected Workbook getWorkbook() {
        return workbook;
    }

    protected Sheet getSheet() {
        return sheet;
    }

    protected void initWorkbookAndSheet() {
        createWorkbook();
        sheet = workbook.createSheet();
        /*
         * Create style for cells which represent a date - excel represents date as a number and has
         * no internal type for dates, so this has to be done by styles :-(
         */
        dateStyle = workbook.createCellStyle();
        // User defined style dd.MM.yyyy, hopefully works on all excel installations ...
        dateStyle.setDataFormat((short)27);
    }

    private void createWorkbook() {
        if (filename.endsWith(XLSX_FILE_EXTENSION)) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();
        }
    }

    /**
     * Fill the content of the cell.
     * 
     * @param cell The cell to set the value.
     * @param ipsValue The ips-string representing the value.
     * @param datatype The datatype defined for the value.
     */
    protected void fillCell(Cell cell, String ipsValue, Datatype datatype) {
        Object obj = format.getExternalValue(ipsValue, datatype, messageList);
        if (obj instanceof Date) {
            cell.setCellValue((Date)obj);
            cell.setCellStyle(dateStyle);
            return;
        }
        if (obj instanceof Number) {
            try {
                cell.setCellValue(((Number)obj).doubleValue());
            } catch (NullPointerException npe) {
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
        } else {
            cell.setCellValue(nullRepresentationString);
        }
    }

    protected IProgressMonitor initProgressMonitor(IProgressMonitor monitor) {
        if (monitor == null) {
            return new NullProgressMonitor();
        } else {
            return monitor;
        }
    }

}
