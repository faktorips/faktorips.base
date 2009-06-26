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
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Operation to export Enum types or contents to an Excel file.
 * 
 * @author Roman Grutza
 */
public class ExcelEnumExportOperation extends AbstractTableExportOperation {

    /* The maximum number of rows allowed in an Excel sheet */
    private static final short MAX_ROWS = Short.MAX_VALUE;

    /*
     * Type to be used for cells with a date. Dates a treated as numbers by excel, so the only way
     * to display a date as a date and not as a stupid number is to format the cell :-(
     */
    private HSSFCellStyle dateStyle = null;

    /**
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

        if (!(typeToExport instanceof IEnumValueContainer)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" + typeToExport == null ? "null"
                            : typeToExport.getClass().toString() + "'");
        }
        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        this.messageList = list;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        IEnumValueContainer enumContainer = getEnum(typeToExport);

        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 5 + enumContainer.getEnumValuesCount());

        // first of all, check if the environment allows an export...
        IEnumType structure = enumContainer.findEnumType(enumContainer.getIpsProject());
        if (structure == null) {
            String text = Messages.TableExportOperation_errStructureNotFound;
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }
        monitor.worked(1);

        messageList.add(enumContainer.validate(enumContainer.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }
        monitor.worked(1);

        messageList.add(structure.validate(structure.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }

        // if we have reached here, the environment is valid, so try to export the data
        monitor.worked(1);

        if (structure.getEnumAttributesCount(false) > MAX_ROWS) {
            Object[] objects = new Object[3];
            objects[0] = new Integer(structure.getEnumAttributesCount(false));
            objects[1] = structure;
            objects[2] = new Short(MAX_ROWS);
            String text = NLS.bind(Messages.TableExportOperation_errStructureTooMuchColumns, objects);
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }

        // if we have reached here, the environment is valid, so try to export the data
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        // create style for cells which represent a date - excel represents date as
        // a number and has no internal type for dates, so this has to be done by styles :-(
        dateStyle = workbook.createCellStyle();
        // user defined style dd.MM.yyyy, hopefully works on all excel installations...
        dateStyle.setDataFormat((short)27);

        monitor.worked(1);

        // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und Zielordner
        exportHeader(sheet, structure.getEnumAttributes(), exportColumnHeaderRow);

        monitor.worked(1);

        exportDataCells(sheet, enumContainer.getEnumValues(), structure, monitor, exportColumnHeaderRow);

        try {
            if (!monitor.isCanceled()) {
                FileOutputStream out = new FileOutputStream(new File(filename));
                workbook.write(out);
                out.close();
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
    }

    private IEnumValueContainer getEnum(IIpsObject typeToExport) {
        if (typeToExport instanceof IEnumValueContainer) {
            return (IEnumValueContainer)typeToExport;
        }
        return null;
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

        List<IEnumAttribute> enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies();

        Datatype[] datatypes = new Datatype[structure.getEnumAttributesCount(true)];
        for (int i = 0; i < datatypes.length; i++) {
            String datatype = enumAttributes.get(i).getDatatype();
            datatypes[i] = structure.getIpsProject().findDatatype(datatype);
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
        int numberOfAttributes = value.getEnumAttributeValuesCount();
        String[] fieldsToExport = new String[numberOfAttributes];
        for (int j = 0; j < numberOfAttributes; j++) {
            IEnumAttributeValue attributeValue = value.getEnumAttributeValues().get(j);
            String obj = attributeValue.getValue();
            fieldsToExport[j] = obj;
        }
        return fieldsToExport;
    }

    /**
     * Fill the content of the cell.
     * 
     * @param cell The cell to set the value.
     * @param ipsValue The ips-string representing the value.
     * @param datatype The datatype defined for the value.
     */
    private void fillCell(HSSFCell cell, String ipsValue, Datatype datatype) {
        Object obj = format.getExternalValue(ipsValue, datatype, messageList);

        if (obj instanceof Date) {
            cell.setCellValue((Date)obj);
            cell.setCellStyle(dateStyle);
            return;
        }
        if (obj instanceof Number) {
            try {
                cell.setCellValue(((Number)obj).doubleValue());
            } catch (NullPointerException npe) {
                cell.setCellValue(nullRepresentationString);
            }
            return;
        }
        if (obj instanceof Boolean) {
            cell.setCellValue(((Boolean)obj).booleanValue());
            return;
        }

        if (obj != null) {
            cell.setCellValue(obj.toString());
        } else {
            cell.setCellValue(nullRepresentationString);
        }

    }

}
