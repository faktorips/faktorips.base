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
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to import IPS enum types or contents from an Excel file.
 * 
 * @author Roman Grutza
 */
public class ExcelEnumImportOperation implements IWorkspaceRunnable {

    private final IEnumValueContainer valueContainer;
    private final String sourceFile;
    private final ExcelTableFormat format;
    private final String nullRepresentationString;
    private final boolean ignoreColumnHeaderRow;
    private final MessageList messageList;
    private Datatype[] datatypes;

    public ExcelEnumImportOperation(IEnumValueContainer valueContainer, String filename, ExcelTableFormat format,
            String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList messageList) {

        this.valueContainer = valueContainer;
        this.sourceFile = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        this.messageList = messageList;

        initDatatypes(valueContainer);
    }

    private void initDatatypes(IEnumValueContainer valueContainer) {
        try {
            List<IEnumAttribute> enumAttributes = valueContainer.findEnumType(valueContainer.getIpsProject())
                    .getEnumAttributesIncludeSupertypeCopies();
            datatypes = new Datatype[enumAttributes.size()];

            for (int i = 0; i < datatypes.length; i++) {
                IEnumAttribute enumAttribute = (IEnumAttribute)enumAttributes.get(i);
                ValueDatatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
                datatypes[i] = datatype;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private HSSFWorkbook getWorkbook(String sourcefile) {
        File importFile = new File(sourceFile);
        FileInputStream fis = null;
        HSSFWorkbook workbook = null;
        try {
            fis = new FileInputStream(importFile);
            workbook = new HSSFWorkbook(fis);
            if (fis != null) {
                fis.close();
            }
        } catch (FileNotFoundException e) {
            IpsPlugin.log(e);
        } catch (IOException e) {
            IpsPlugin.log(e);
        }
        return workbook;
    }

    public void run(IProgressMonitor monitor) throws CoreException {
        // TODO AW: monitor is not shown to the user somehow
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        HSSFWorkbook workbook = getWorkbook(null);
        HSSFSheet sheet = workbook.getSheetAt(0);
        monitor.beginTask("Import file " + sourceFile, 2 + getNumberOfExcelRows(sheet));

        // Update datatypes because the structure might be altered if this operation is reused.
        initDatatypes(valueContainer);
        monitor.worked(1);
        fillEnum(valueContainer, sheet, monitor);
        if (monitor.isCanceled()) {
            valueContainer.getIpsObject().getIpsSrcFile().discardChanges();
        }

        valueContainer.getIpsObject().getIpsSrcFile().save(true, monitor);
        monitor.worked(1);
        monitor.done();
    }

    private int getNumberOfExcelRows(HSSFSheet sheet) {
        int numberRows = 0;
        // Row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0 contains data.
        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        for (int i = startRow;; i++) {
            HSSFRow sheetRow = sheet.getRow(i);
            if (sheetRow == null) { // No more rows.
                break;
            }
            numberRows++;
        }
        return numberRows;
    }

    private void fillEnum(IEnumValueContainer valueContainer, HSSFSheet sheet, IProgressMonitor monitor)
            throws CoreException {

        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        int expectedFields = valueContainer.findEnumType(valueContainer.getIpsProject()).getEnumAttributesCount(true);

        for (int i = startRow;; i++) {
            HSSFRow sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                // No more rows, we are finished whit this sheet.
                break;
            }
            IEnumValue enumValue = valueContainer.newEnumValue();

            for (short j = 0; j < expectedFields; j++) {
                HSSFCell cell = sheetRow.getCell(j);
                IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(j);
                if (cell == null) {
                    Object[] objects = new Object[3];
                    objects[0] = new Integer(i);
                    objects[1] = new Integer(j);
                    objects[2] = nullRepresentationString;
                    String msg = NLS.bind("In row {0}, column {1} no value is set - imported {2} instead.", objects);
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                    enumAttributeValue.setValue(nullRepresentationString);
                } else {
                    enumAttributeValue.setValue(readCell(cell, datatypes[j]));
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
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            return format.getIpsValue(Boolean.valueOf(cell.getBooleanCellValue()), datatype, messageList);
        } else {
            String value = cell.getStringCellValue();
            if (nullRepresentationString.equals(value)) {
                return null;
            }
            return format.getIpsValue(value, datatype, messageList);
        }
    }

}
