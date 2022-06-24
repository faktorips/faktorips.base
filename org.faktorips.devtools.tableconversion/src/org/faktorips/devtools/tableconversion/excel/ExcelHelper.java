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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Helper class for the Excel POI library.
 * 
 * @author Roman Grutza
 */
public class ExcelHelper {

    private ExcelHelper() {
        // do not instantiate utility class
    }

    /**
     * @param sourceFile A platform-dependent String which denotes the full path to an Excel file.
     * @return A workbook constructed from the given source file.
     * 
     * @throws FileNotFoundException if the file provided by sourceFile does not exist
     * @throws IOException if the file is not readable or accessible
     */
    public static Workbook getWorkbook(String sourceFile) throws IOException {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        Workbook workbook = null;
        fis = new FileInputStream(importFile);
        try {
            workbook = WorkbookFactory.create(fis);
        } catch (InvalidFormatException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } finally {
            fis.close();
        }
        return workbook;
    }

    /**
     * @param sourceFile A platform-dependent String which denotes the full path to an Excel file.
     * @param indexOfWorkbook The zero-based index of the desired worksheet.
     * 
     * @throws FileNotFoundException if the file provided by sourceFile does not exist
     * @throws IOException if the file is not readable or accessible
     */
    public static Sheet getWorksheetFromWorkbook(String sourceFile, int indexOfWorkbook) throws IOException {
        Workbook workbook = getWorkbook(sourceFile);
        if (workbook.getNumberOfSheets() < indexOfWorkbook + 1) {
            throw new IllegalArgumentException("The excel file '" + sourceFile //$NON-NLS-1$
                    + "' does not contain the sheet with index " + indexOfWorkbook); //$NON-NLS-1$
        }
        return workbook.getSheetAt(indexOfWorkbook);
    }

}
