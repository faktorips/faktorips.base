/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.POIDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractTableImportOperation;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
abstract class AbstractExcelImportOperation extends AbstractTableImportOperation {

    private static final Date FIRST_OF_MARCH_1900 = new GregorianCalendar(1900, 2, 1).getTime();
    private static final String MICROSOFT_EXCEL = "Microsoft Excel"; //$NON-NLS-1$

    protected Workbook workbook;
    private boolean mightBeOpenOffice = false;

    protected Sheet sheet;

    protected AbstractExcelImportOperation(String filename, ExcelTableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList messageList, boolean importIntoExisting) {

        super(filename, format, nullRepresentationString, ignoreColumnHeaderRow, messageList, importIntoExisting);
    }

    protected abstract void initDatatypes();

    private Workbook getWorkbook() throws IOException {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        Workbook workbook = null;
        fis = new FileInputStream(importFile);
        workbook = new HSSFWorkbook(fis);
        fis.close();
        if (((POIDocument)workbook).getSummaryInformation() == null
                || !MICROSOFT_EXCEL.equals(((POIDocument)workbook).getSummaryInformation().getApplicationName())) {
            mightBeOpenOffice = true;
        }
        return workbook;
    }

    protected String readCell(Cell cell, Datatype datatype) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date dateCellValue = cell.getDateCellValue();
                if (mightBeOpenOffice) {
                    dateCellValue = correctOffset(dateCellValue);
                }
                return format.getIpsValue(dateCellValue, datatype, messageList);
            }
            return format.getIpsValue(new Double(cell.getNumericCellValue()), datatype, messageList);
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return format.getIpsValue(Boolean.valueOf(cell.getBooleanCellValue()), datatype, messageList);
        } else {
            String value = cell.getStringCellValue();
            if (nullRepresentationString.equals(value)) {
                return null;
            }
            return format.getIpsValue(value, datatype, messageList);
        }
    }

    /**
     * Workaround for https://issues.apache.org/ooo/show_bug.cgi?id=80463
     */
    private Date correctOffset(final Date dateCellValue) {
        if (dateCellValue.before(FIRST_OF_MARCH_1900)) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateCellValue);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            return calendar.getTime();
        }
        return dateCellValue;
    }

    protected void initWorkbookAndSheet() throws IOException {
        workbook = getWorkbook();
        sheet = workbook.getSheetAt(0);
    }

}
