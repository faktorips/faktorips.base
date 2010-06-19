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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractTableImportOperation;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
abstract class AbstractExcelImportOperation extends AbstractTableImportOperation {

    protected HSSFWorkbook workbook;

    protected HSSFSheet sheet;

    protected AbstractExcelImportOperation(String filename, ExcelTableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList messageList, boolean importIntoExisting) {

        super(filename, format, nullRepresentationString, ignoreColumnHeaderRow, messageList, importIntoExisting);
    }

    protected abstract void initDatatypes();

    private HSSFWorkbook getWorkbook() throws IOException {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        HSSFWorkbook workbook = null;
        fis = new FileInputStream(importFile);
        workbook = new HSSFWorkbook(fis);
        fis.close();
        return workbook;
    }

    protected String readCell(HSSFCell cell, Datatype datatype) {
        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return format.getIpsValue(cell.getDateCellValue(), datatype, messageList);
            }
            return format.getIpsValue(new Double(cell.getNumericCellValue()), datatype, messageList);
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            return format.getIpsValue(Boolean.valueOf(cell.getBooleanCellValue()), datatype, messageList);
        } else {
            String value = cell.getStringCellValue();
            if (nullRepresentationString.equals(value)) {
                return null;
            }
            return format.getIpsValue(value, datatype, messageList);
        }
    }

    protected void initWorkbookAndSheet() throws IOException {
        workbook = getWorkbook();
        sheet = workbook.getSheetAt(0);
    }

}
