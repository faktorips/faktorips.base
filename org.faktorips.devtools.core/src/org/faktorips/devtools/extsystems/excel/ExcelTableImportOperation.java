/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IWorkspaceRunnable;
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
public class ExcelTableImportOperation implements IWorkspaceRunnable {

    /**
     * Qualified name of the file to import from
     */
    private String sourceFile;

    /**
     * The table structure the imported table content is bound to
     */
    private ITableStructure structure;

    /**
     * Generation of the table contents the import has to be inserted.
     */
    private ITableContentsGeneration targetGeneration;

    /**
     * Datatypes for the columns. The datatype at index 1 is the datatype defined in the structure
     * for column at index 1.
     */
    private Datatype[] datatypes;

    /**
     * The format which handles data conversion
     */
    private AbstractExternalTableFormat format;

    /**
     * String representing <code>null</code> in excel (because it is not capable of
     * <code>null</code>-values).
     */
    private String nullRepresentationString;

    /**
     * List of messages describing problems occured during export.
     */
    private MessageList messageList;

    public ExcelTableImportOperation(ITableStructure structure, String sourceFile,
            ITableContentsGeneration targetGeneration, AbstractExternalTableFormat format,
            String nullRepresentationString, MessageList list) {
        this.sourceFile = sourceFile;
        this.structure = structure;
        this.targetGeneration = targetGeneration;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.messageList = list;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("Import file " + sourceFile, targetGeneration.getNumOfRows() + 3);
            
            MessageList ml = structure.validate(); 
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
            }
            else {
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
        for (int i = 1;; i++) { // row 0 is the header
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
                    String msg = NLS.bind("In row {0}, column {1} no value is set - imported {2} instead.", objects);
                    messageList.add(new Message("", msg, Message.WARNING));
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
