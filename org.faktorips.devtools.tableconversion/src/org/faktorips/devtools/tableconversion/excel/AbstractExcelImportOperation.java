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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.AbstractTableImportOperation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractExcelImportOperation extends AbstractTableImportOperation {

    public static final String MSG_CODE_FIXED_OPEN_OFFICE_DATE = "fixedOpenOfficeDate"; //$NON-NLS-1$

    private Workbook workbook;

    private boolean mightBeOpenOffice = false;

    private Sheet sheet;

    private boolean addedMessageForOffsetCorrection;

    protected AbstractExcelImportOperation(String filename, ExcelTableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList messageList, boolean importIntoExisting) {

        super(filename, format, nullRepresentationString, ignoreColumnHeaderRow, messageList, importIntoExisting);
    }

    protected abstract void initDatatypes();

    protected Sheet getSheet() {
        return sheet;
    }

    private void checkForOpenOfficeFormat(Workbook workbook) {
        if (workbook instanceof POIDocument) {
            if (((POIDocument)workbook).getSummaryInformation() == null
                    || StringUtils.isBlank(((POIDocument)workbook).getSummaryInformation().getApplicationName())) {
                mightBeOpenOffice = true;
            }
        }
    }

    protected String readCell(Cell cell, Datatype datatype) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell) || isReserved(cell.getCellStyle().getDataFormat())) {
                Date dateCellValue = cell.getDateCellValue();
                if (mightBeOpenOffice) {
                    dateCellValue = correctOffset(cell, messageList);
                }
                return format.getIpsValue(dateCellValue, datatype, messageList);
            }
            double numericCellValue = cell.getNumericCellValue();
            BigDecimal roundedResult = roundNumericCellValue(numericCellValue);
            return format.getIpsValue(roundedResult, datatype, messageList);
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

    private boolean isReserved(short dataFormatIndex) {
        String builtinFormat = BuiltinFormats.getBuiltinFormat(dataFormatIndex);
        return builtinFormat != null && builtinFormat.startsWith("reserved"); //$NON-NLS-1$
    }

    /**
     * Correct rounding errors in Floating-point arithmetic caused by Excel import.
     * <p>
     * Floating-point numbers that are stored in excel have a precision of 15 digits. Because excel
     * stores these numbers in floating-point representation, the imported value may differ at the
     * 16th position. To avoid different representations, Excel always round every number after the
     * 15th digit. Hence we need to do it like Excel in order to get the same number
     * representations.
     * 
     * @see <a href="https://support.microsoft.com/kb/78113">Excel-FloatingPoints</a>
     * @see <a href="http://support.microsoft.com/kb/269370">Excel-LongDigits</a>
     * 
     * @param numericCellValue the potential inaccurate cell content
     * @return the correct rounded numericCellValue
     */
    BigDecimal roundNumericCellValue(double numericCellValue) {
        BigDecimal bigDecimal = new BigDecimal(numericCellValue, new MathContext(15, RoundingMode.HALF_UP));
        BigDecimal formattedBigDecimal = bigDecimal.stripTrailingZeros();
        if (formattedBigDecimal.scale() < 0) {
            formattedBigDecimal = formattedBigDecimal.setScale(0);
        }
        return formattedBigDecimal;
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
        workbook = newWorkbook();
        sheet = workbook.getSheetAt(0);
    }

    private Workbook newWorkbook() throws IOException {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        fis = new FileInputStream(importFile);
        try {
            workbook = WorkbookFactory.create(fis);
            checkForOpenOfficeFormat(workbook);
        } catch (InvalidFormatException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } finally {
            fis.close();
        }
        return workbook;
    }

}
