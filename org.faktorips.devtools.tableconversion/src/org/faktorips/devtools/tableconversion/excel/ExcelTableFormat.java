/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

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
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public void executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) throws CoreException {

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
        } catch (Exception e) {
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
            boolean importIntoExisting) throws CoreException {

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
            new HSSFWorkbook(fis);
            return true;
        } catch (Exception e) {
            // if an exception occurred, it is not a valid source, this exception can be ignored
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
        return false;
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
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }
    }

    private List<String[]> getPreviewInternal(Datatype[] datatypes,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {

        HSSFSheet sheet = null;
        try {
            sheet = ExcelHelper.getWorksheetFromWorkbook(filename.toOSString(), 0);
        } catch (Exception e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }

        List<String[]> result = new ArrayList<String[]>();
        MessageList ml = new MessageList();

        // row 0 is the header if ignoreColumnHeaderRow is true,
        // otherwise row 0 contains data
        int startRow = ignoreColumnHeaderRow ? 1 : 0;

        // the workbook can contain less rows than requested
        int linesLeft = Math.min(sheet.getLastRowNum(), maxNumberOfRows);
        for (int i = startRow;; i++) {
            HSSFRow sheetRow = sheet.getRow(i);
            if (linesLeft-- <= 0 || sheetRow == null) {
                // no more rows, we are finished with this sheet.
                break;
            }
            int numberOfCells = sheetRow.getLastCellNum();
            String[] convertedLine = new String[numberOfCells];
            for (short j = 0; j < numberOfCells; j++) {
                HSSFCell cell = sheetRow.getCell(j);
                String cellString = readCell(cell, datatypes[j], ml, nullRepresentation);
                convertedLine[j] = cellString;
            }

            result.add(convertedLine);
        }

        return result;
    }

    // TODO rg: code duplication in AbstractExcelImportOperation
    private String readCell(HSSFCell cell, Datatype datatype, MessageList messageList, String nullRepresentation) {
        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return getIpsValue(cell.getDateCellValue(), datatype, messageList);
            }
            return getIpsValue(new Double(cell.getNumericCellValue()), datatype, messageList);
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
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
