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
