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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" + typeToExport == null ? "null"
                            : typeToExport.getClass().toString() + "'");
        }
        enumValueContainer = (IEnumValueContainer)typeToExport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 2 + enumValueContainer.getEnumValuesCount());

        initWorkbookAndSheet();
        monitor.worked(1);

        IEnumType structure = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
        boolean includeLiteralName = structure instanceof IEnumType;
        List<IEnumAttribute> attributes = structure.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        exportHeader(sheet, attributes, exportColumnHeaderRow);
        monitor.worked(1);
        if (monitor.isCanceled()) {
            return;
        }

        exportDataCells(sheet, enumValueContainer.getEnumValues(), structure, monitor, exportColumnHeaderRow);
        if (monitor.isCanceled()) {
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(filename));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
        monitor.done();
    }

    /**
     * Create the header as first row.
     * 
     * @param sheet The sheet where to create the header
     * @param enumAttributes The column names defined by the structure.
     * @param exportColumnHeaderRow <code>true</code> if a header line containing the column names
     *            should be exported
     */
    private void exportHeader(HSSFSheet sheet, List<IEnumAttribute> enumAttributes, boolean exportColumnHeaderRow) {
        if (!exportColumnHeaderRow) {
            return;
        }
        HSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < enumAttributes.size(); i++) {
            headerRow.createCell((short)i).setCellValue(enumAttributes.get(i).getName());
        }

    }

    private void exportDataCells(HSSFSheet sheet,
            List<IEnumValue> values,
            IEnumType structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreException {

        List<IEnumAttribute> enumAttributes = structure
                .getEnumAttributesIncludeSupertypeCopies(enumValueContainer instanceof IEnumType);
        Datatype[] datatypes = new Datatype[structure.getEnumAttributesCountIncludeSupertypeCopies(structure
                .isUsingEnumLiteralNameAttribute())];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = enumAttributes.get(i).findDatatype(structure.getIpsProject());
        }

        int offest = exportColumnHeaderRow ? 1 : 0;
        for (int i = 0; i < values.size(); i++) {
            HSSFRow sheetRow = sheet.createRow(i + offest);
            IEnumValue value = values.get(i);
            String[] fieldsToExport = getFieldsForEnumValue(datatypes, value);
            for (int j = 0; j < value.getEnumAttributeValuesCount(); j++) {
                HSSFCell cell = sheetRow.createCell((short)j);
                fillCell(cell, fieldsToExport[j], datatypes[j]);
            }
            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private String[] getFieldsForEnumValue(Datatype[] datatypes, IEnumValue value) {
        int numberOfFields = value.getEnumAttributeValuesCount();
        String[] fieldsToExport = new String[numberOfFields];
        for (int j = 0; j < numberOfFields; j++) {
            IEnumAttributeValue attributeValue = value.getEnumAttributeValues().get(j);
            String obj = attributeValue.getValue();
            fieldsToExport[j] = obj;
        }
        return fieldsToExport;
    }

}
