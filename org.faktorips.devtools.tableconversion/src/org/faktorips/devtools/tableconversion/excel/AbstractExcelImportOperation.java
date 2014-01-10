/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.AbstractTableImportOperation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
abstract class AbstractExcelImportOperation extends AbstractTableImportOperation {

    public static final String MSG_CODE_FIXED_OPEN_OFFICE_DATE = "fixedOpenOfficeDate"; //$NON-NLS-1$

    private static final Date FIRST_OF_MARCH_1900 = new GregorianCalendar(1900, 2, 1).getTime();

    protected Workbook workbook;
    private boolean mightBeOpenOffice = false;

    protected Sheet sheet;

    private boolean addedMessageForOffsetCorrection;

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
                || StringUtils.isBlank(((POIDocument)workbook).getSummaryInformation().getApplicationName())) {
            mightBeOpenOffice = true;
        }
        return workbook;
    }

    protected String readCell(Cell cell, Datatype datatype) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date dateCellValue = cell.getDateCellValue();
                if (mightBeOpenOffice) {
                    dateCellValue = correctOffset(cell, messageList);
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
    private Date correctOffset(final Cell cell, MessageList messageList) {
        double numericCellValue = cell.getNumericCellValue();
        if (numericCellValue < 61) {
            cell.setCellValue(numericCellValue - 1);
            if (!addedMessageForOffsetCorrection) {
                messageList.add(new Message(MSG_CODE_FIXED_OPEN_OFFICE_DATE,
                        Messages.AbstractExcelImportOperation_FixedOpenOfficeDate, Message.INFO));
                addedMessageForOffsetCorrection = true;
            }
        }
        return cell.getDateCellValue();
    }

    protected void initWorkbookAndSheet() throws IOException {
        workbook = getWorkbook();
        sheet = workbook.getSheetAt(0);
    }

}
