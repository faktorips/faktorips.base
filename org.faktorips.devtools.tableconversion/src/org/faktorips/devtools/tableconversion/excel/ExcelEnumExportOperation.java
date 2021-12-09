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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Operation to export Enum types or contents to an Excel file.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class ExcelEnumExportOperation extends AbstractExcelExportOperation {

    /** The enumeration to export. */
    private IEnumValueContainer enumValueContainer;

    /**
     * 
     * 
     * @param typeToExport An <code>IEnumValueContainer</code> instance.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    public ExcelEnumExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {

        super(typeToExport, filename, format, nullRepresentationString, exportColumnHeaderRow, list);
        if (!(typeToExport instanceof IEnumValueContainer)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" //$NON-NLS-1$
                            + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
        enumValueContainer = (IEnumValueContainer)typeToExport;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreRuntimeException {
        IProgressMonitor progressMonitor = initProgressMonitor(monitor);
        progressMonitor.beginTask(Messages.TableExportOperation_labelMonitorTitle,
                2 + enumValueContainer.getEnumValuesCount());

        initWorkbookAndSheet();
        progressMonitor.worked(1);

        IEnumType structure = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
        List<IEnumAttribute> attributes = structure.getEnumAttributesIncludeSupertypeCopies(true);
        exportHeader(getSheet(), attributes, exportColumnHeaderRow);
        progressMonitor.worked(1);
        if (progressMonitor.isCanceled()) {
            return;
        }

        exportDataCells(getSheet(), enumValueContainer.getEnumValues(), structure, progressMonitor,
                exportColumnHeaderRow);
        if (progressMonitor.isCanceled()) {
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(filename));
            getWorkbook().write(out);
            out.close();
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
        progressMonitor.done();
    }

    /**
     * Create the header as first row.
     * 
     * @param sheet The sheet where to create the header
     * @param enumAttributes The column names defined by the structure.
     * @param exportColumnHeaderRow <code>true</code> if a header line containing the column names
     *            should be exported
     */
    private void exportHeader(Sheet sheet, List<IEnumAttribute> enumAttributes, boolean exportColumnHeaderRow) {
        if (!exportColumnHeaderRow) {
            return;
        }
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < enumAttributes.size(); i++) {
            headerRow.createCell(i).setCellValue(enumAttributes.get(i).getName());
        }

    }

    private void exportDataCells(Sheet sheet,
            List<IEnumValue> values,
            IEnumType structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreRuntimeException {

        boolean exportingEnumType = enumValueContainer instanceof IEnumType;
        List<IEnumAttribute> enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(exportingEnumType);
        Datatype[] datatypes = new Datatype[structure.getEnumAttributesCountIncludeSupertypeCopies(exportingEnumType)];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = enumAttributes.get(i).findDatatype(structure.getIpsProject());
        }

        int offest = exportColumnHeaderRow ? 1 : 0;
        for (int i = 0; i < values.size(); i++) {
            Row sheetRow = sheet.createRow(i + offest);
            IEnumValue value = values.get(i);
            String[] fieldsToExport = getFieldsForEnumValue(value);
            for (int j = 0; j < value.getEnumAttributeValuesCount(); j++) {
                Cell cell = sheetRow.createCell(j);
                fillCell(cell, fieldsToExport[j], datatypes[j]);
            }
            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private String[] getFieldsForEnumValue(IEnumValue value) {
        int numberOfFields = value.getEnumAttributeValuesCount();
        String[] fieldsToExport = new String[numberOfFields];
        for (int j = 0; j < numberOfFields; j++) {
            IEnumAttributeValue attributeValue = value.getEnumAttributeValues().get(j);
            String obj = attributeValue.getValue().getDefaultLocalizedContent(attributeValue.getIpsProject());
            fieldsToExport[j] = obj;
        }
        return fieldsToExport;
    }

}
