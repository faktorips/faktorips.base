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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Helper class for the Excel POI library.
 * 
 * @author Roman Grutza
 */
public class ExcelHelper {

    /**
     * @param sourceFile A platform-dependant String which denotes the full path to an Excel file.
     * @return A workbook constructed from the given source file.
     */
    public static HSSFWorkbook getWorkbook(String sourceFile) throws FileNotFoundException, IOException {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        HSSFWorkbook workbook = null;
        fis = new FileInputStream(importFile);
        workbook = new HSSFWorkbook(fis);
        fis.close();
        return workbook;
    }

    /**
     * @param sourceFile A platform-dependant String which denotes the full path to an Excel file.
     * @param indexOfWorkbook The zero-based index of the desired worksheet.
     */
    public static HSSFSheet getWorksheetFromWorkbook(String sourceFile, int indexOfWorkbook)
            throws FileNotFoundException, IOException {
        HSSFWorkbook workbook = getWorkbook(sourceFile);
        if (workbook.getNumberOfSheets() < indexOfWorkbook + 1) {
            throw new IllegalArgumentException("The excel file '" + sourceFile //$NON-NLS-1$
                    + "' does not contain the sheet with index " + indexOfWorkbook); //$NON-NLS-1$
        }
        return workbook.getSheetAt(indexOfWorkbook);
    }

}
