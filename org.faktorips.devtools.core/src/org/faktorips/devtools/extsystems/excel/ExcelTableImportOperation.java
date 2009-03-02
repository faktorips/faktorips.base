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

package org.faktorips.devtools.extsystems.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to import ipstablecontents from an excel-file.
 * 
 * @author Thorsten Guenther
 */
public class ExcelTableImportOperation extends AbstractTableImportOperation {

    public ExcelTableImportOperation(ITableStructure structure, String sourceFile,
            ITableContentsGeneration targetGeneration, AbstractExternalTableFormat format,
            String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList list) {
        this.sourceFile = sourceFile;
        this.structure = structure;
        this.targetGeneration = targetGeneration;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        this.messageList = list;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask(Messages.ExcelTableImportOperation_labelImportFile + sourceFile, targetGeneration.getNumOfRows() + 3);
            
            MessageList ml = structure.validate(structure.getIpsProject()); 
            if (ml.containsErrorMsg()) {
                messageList.add(ml);
                return;
            }

            monitor.worked(1);
            if (monitor.isCanceled()) {
                return;
            }
            
            File importFile = new File(sourceFile);
            FileInputStream fis = null;
            HSSFWorkbook workbook = null;
            try {
                fis = new FileInputStream(importFile);
                workbook = new HSSFWorkbook(fis);
            }
            finally {
                if (fis != null) {
                    fis.close();
                }
            }

            monitor.worked(1);

            if (monitor.isCanceled()) {
                return;
            }

            IColumn[] columns = structure.getColumns();
            datatypes = new Datatype[columns.length];
            for (int i = 0; i < columns.length; i++) {
                datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
            }

            HSSFSheet sheet = workbook.getSheetAt(0);
            fillGeneration(targetGeneration, sheet, monitor);
            if (!monitor.isCanceled()) {
                targetGeneration.getIpsObject().getIpsSrcFile().save(true, monitor);
                monitor.worked(1);
            } else {
                targetGeneration.getIpsObject().getIpsSrcFile().discardChanges();
            }
        }
        catch (IOException e) {
            throw new CoreException(new IpsStatus(NLS
                    .bind(Messages.AbstractXlsTableImportOperation_errRead, sourceFile), e));
        }
    }

    private void fillGeneration(ITableContentsGeneration generation, HSSFSheet sheet, IProgressMonitor monitor)
            throws CoreException {
        // row 0 is the header if ignoreColumnHeaderRow is true,
        // otherwise row 0 contains data
        int startRow = ignoreColumnHeaderRow?1:0;
        for (int i = startRow;; i++) {
            HSSFRow sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                // no more rows, we are finished whit this sheet.
                break;
            }
            IRow genRow = generation.newRow();
            for (short j = 0; j < structure.getNumOfColumns(); j++) {
                HSSFCell cell = sheetRow.getCell(j);
                if (cell == null) {
                    Object[] objects = new Object[3];
                    objects[0] = new Integer(i);
                    objects[1] = new Integer(j);
                    objects[2] = nullRepresentationString;
                    String msg = NLS.bind(Messages.ExcelTableImportOperation_msgImportEscapevalue, objects);
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                    genRow.setValue(j, nullRepresentationString);
                }
                else {
                    genRow.setValue(j, readCell(cell, datatypes[j]));
                } 
            }

            if (monitor.isCanceled()) {
                return;
            }

            monitor.worked(1);
        }
    }

    private String readCell(HSSFCell cell, Datatype datatype) {
        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return format.getIpsValue(cell.getDateCellValue(), datatype, messageList);
            }
            return format.getIpsValue(new Double(cell.getNumericCellValue()), datatype, messageList);
        }
        else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            return format.getIpsValue(Boolean.valueOf(cell.getBooleanCellValue()), datatype, messageList);
        }
        else {
            String value = cell.getStringCellValue();
            if (nullRepresentationString.equals(value)) {
                return null;
            }
            return format.getIpsValue(value, datatype, messageList);
        }
    }
}
