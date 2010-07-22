/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
abstract class AbstractExcelExportOperation extends AbstractTableExportOperation {

    /**
     * Type to be used for cells with a date. Dates a treated as numbers by excel, so the only way
     * to display a date as a date and not as a stupid number is to format the cell :-(
     */
    protected HSSFCellStyle dateStyle;

    protected HSSFWorkbook workbook;

    protected HSSFSheet sheet;

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

    protected void initWorkbookAndSheet() {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet();
        /*
         * Create style for cells which represent a date - excel represents date as a number and has
         * no internal type for dates, so this has to be done by styles :-(
         */
        dateStyle = workbook.createCellStyle();
        // User defined style dd.MM.yyyy, hopefully works on all excel installations ...
        dateStyle.setDataFormat((short)27);
    }

    /**
     * Fill the content of the cell.
     * 
     * @param cell The cell to set the value.
     * @param ipsValue The ips-string representing the value.
     * @param datatype The datatype defined for the value.
     */
    protected void fillCell(HSSFCell cell, String ipsValue, Datatype datatype) {
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

}
