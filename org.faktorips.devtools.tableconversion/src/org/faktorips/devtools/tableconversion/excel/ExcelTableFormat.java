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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;

/**
 * Table format for Microsoft's Excel.
 * 
 * @author Thorsten Guenther
 */
public class ExcelTableFormat extends AbstractExternalTableFormat {

    @Override
    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        try {
            ExcelTableExportOperation excelTableExportOperation = new ExcelTableExportOperation(contents,
                    filename.toOSString(), this, nullRepresentationString, exportColumnHeaderRow, list);
            excelTableExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public void executeTableImport(ITableStructure structure,
            IPath filename,
            ITableRows targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {

        ExcelTableImportOperation excelTableImportOperation = new ExcelTableImportOperation(structure,
                filename.toOSString(), targetGeneration, this, nullRepresentationString, ignoreColumnHeaderRow, list,
                importIntoExisting);
        excelTableImportOperation.run(new NullProgressMonitor());
    }

    @Override
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {

        try {
            ExcelEnumExportOperation enumExportOperation = new ExcelEnumExportOperation(valueContainer,
                    filename.toOSString(), this, nullRepresentationString, exportColumnHeaderRow, list);
            enumExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public void executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {

        ExcelEnumImportOperation enumImportOperation = new ExcelEnumImportOperation(valueContainer,
                filename.toOSString(), this, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);
        enumImportOperation.run(new NullProgressMonitor());
    }

    @Override
    public boolean isValidImportSource(String source) {
        File file = new File(source);

        if (!file.canRead()) {
            return false;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            WorkbookFactory.create(file);
            return true;
        } catch (FileNotFoundException fe) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (InvalidFormatException e) {
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // this is a serious problem, so report it.
                    IpsPlugin.log(e);
                }
            }
        }
    }

    @Override
    public List<String[]> getImportTablePreview(ITableStructure structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return getImportPreview(structure, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
    }

    @Override
    public List<String[]> getImportEnumPreview(IEnumType structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return getImportPreview(structure, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
    }

    private List<String[]> getImportPreview(IIpsObject structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        Datatype[] datatypes;
        try {
            if (structure instanceof ITableStructure) {
                datatypes = getDatatypes((ITableStructure)structure);
            } else if (structure instanceof IEnumType) {
                datatypes = getDatatypes((IEnumType)structure);
            } else {
                return Collections.emptyList();
            }

            return getPreviewInternal(datatypes, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }
    }

    private List<String[]> getPreviewInternal(Datatype[] datatypes,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {

        Sheet sheet = null;
        try {
            sheet = ExcelHelper.getWorksheetFromWorkbook(filename.toOSString(), 0);
        } catch (FileNotFoundException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        } catch (IOException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }

        List<String[]> result = new ArrayList<>();
        MessageList ml = new MessageList();

        // row 0 is the header if ignoreColumnHeaderRow is true,
        // otherwise row 0 contains data
        int startRow = ignoreColumnHeaderRow ? 1 : 0;

        // the workbook can contain less rows than requested
        int linesLeft = Math.min(sheet.getLastRowNum(), maxNumberOfRows);
        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (linesLeft-- <= 0 || sheetRow == null) {
                // no more rows, we are finished with this sheet.
                break;
            }
            int numberOfCells = sheetRow.getLastCellNum();
            if (numberOfCells > 0) {
                String[] convertedLine = new String[numberOfCells];
                for (int j = 0; j < numberOfCells; j++) {
                    Cell cell = sheetRow.getCell(j);
                    String cellString = readCell(cell, datatypes[j], ml, nullRepresentation);
                    convertedLine[j] = cellString;
                }

                result.add(convertedLine);
            }
        }

        return result;
    }

    // TODO FIPS-7992: code duplication in AbstractExcelImportOperation
    private String readCell(Cell cell, Datatype datatype, MessageList messageList, String nullRepresentation) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return getIpsValue(cell.getDateCellValue(), datatype, messageList);
            }
            return getIpsValue(Double.valueOf(cell.getNumericCellValue()), datatype, messageList);
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return getIpsValue(Boolean.valueOf(cell.getBooleanCellValue()), datatype, messageList);
        } else {
            String value = cell.getStringCellValue();
            if (nullRepresentation.equals(value)) {
                return nullRepresentation;
            }
            return getIpsValue(value, datatype, messageList);
        }
    }
}
