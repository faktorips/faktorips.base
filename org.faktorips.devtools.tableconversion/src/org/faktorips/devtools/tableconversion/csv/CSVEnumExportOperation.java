/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.opencsv.CSVWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Operation to export an Enum types or contents to an text-file (comma separated values).
 * 
 * @author Roman Grutza
 */
public class CSVEnumExportOperation extends AbstractTableExportOperation {

    /**
     * @param typeToExport An <code>IEnumValueContainer</code> instance.
     * @param filename The name of the file to export to.
     * @param format The format to use for transforming the data.
     * @param nullRepresentationString The string to use as replacement for <code>null</code>.
     * @param exportColumnHeaderRow <code>true</code> if the header names will be included in the
     *            exported format
     * @param list A MessageList to store errors and warnings which happened during the export
     */
    public CSVEnumExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {

        if (!(typeToExport instanceof IEnumValueContainer)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" //$NON-NLS-1$
                            + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        messageList = list;
    }

    @Override
    public void run(IProgressMonitor monitor) {
        IProgressMonitor localMonitor = monitor;
        if (localMonitor == null) {
            localMonitor = new NullProgressMonitor();
        }

        IEnumValueContainer enumContainer = getEnum(typeToExport);

        localMonitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 4 + enumContainer.getEnumValuesCount());

        // first of all, check if the environment allows an export...
        IEnumType structure = enumContainer.findEnumType(enumContainer.getIpsProject());
        if (structure == null) {
            String text = Messages.TableExportOperation_errStructureNotFound;
            messageList.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }
        localMonitor.worked(1);

        messageList.add(enumContainer.validate(enumContainer.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }
        localMonitor.worked(1);

        messageList.add(structure.validate(structure.getIpsProject()));
        if (messageList.containsErrorMsg()) {
            return;
        }

        // if we have reached here, the environment is valid, so try to export the data
        localMonitor.worked(1);

        FileOutputStream out = null;
        CSVWriter writer = null;
        try {
            if (!localMonitor.isCanceled()) {
                // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und
                // Zielordner
                out = new FileOutputStream(new File(filename));

                char fieldSeparatorChar = getFieldSeparatorCSV(format);
                writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(out)), fieldSeparatorChar);

                exportHeader(writer, structure.getEnumAttributesIncludeSupertypeCopies(true), exportColumnHeaderRow);

                localMonitor.worked(1);

                exportDataCells(writer, enumContainer.getEnumValues(), structure, localMonitor, exportColumnHeaderRow);
                writer.close();
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        } finally {
            if (out != null) {
                try {
                    out.close();
                    // CSOFF: Empty Statement
                } catch (IOException ee) {
                    // ignore
                }
                // CSON: Empty Statement
            }
        }
    }

    private IEnumValueContainer getEnum(IIpsObject typeToExport) {
        if (typeToExport instanceof IEnumValueContainer) {
            return (IEnumValueContainer)typeToExport;
        }
        return null;
    }

    /**
     * Writes the CSV header containing the names of the columns using the given CSV writer.
     * 
     * @param writer A CSV writer instance
     * @param list The enum type's attributes as a list
     * @param exportColumnHeaderRow Flag to indicate whether to export the header
     */
    private void exportHeader(CSVWriter writer, List<IEnumAttribute> list, boolean exportColumnHeaderRow) {
        if (exportColumnHeaderRow) {
            String[] header = new String[list.size()];
            for (int i = 0; i < header.length; i++) {
                header[i] = list.get(i).getName();
            }
            writer.writeNext(header);
        }
    }

    /**
     * Create the cells for the export
     * 
     * @param writer A CSV writer instance.
     * @param values The enum's valeus as a list.
     * @param structure The structure the content is bound to.
     * @param monitor The monitor to display the progress.
     * @param exportColumnHeaderRow column header names included or not.
     * 
     * @throws IpsException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(CSVWriter writer,
            List<IEnumValue> values,
            IEnumType structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) {

        List<IEnumAttribute> enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(true);

        Datatype[] datatypes = new Datatype[structure.getEnumAttributesCountIncludeSupertypeCopies(true)];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = enumAttributes.get(i).findDatatype(structure.getIpsProject());
        }

        for (IEnumValue value : values) {
            int numberOfAttributes = value.getEnumAttributeValuesCount();
            String[] fieldsToExport = new String[numberOfAttributes];
            for (int j = 0; j < numberOfAttributes; j++) {
                IEnumAttributeValue attributeValue = value.getEnumAttributeValues().get(j);
                Object obj = format.getExternalValue(
                        attributeValue.getValue().getDefaultLocalizedContent(attributeValue.getIpsProject()),
                        datatypes[j], messageList);

                String csvField;
                try {
                    csvField = (obj == null) ? nullRepresentationString : (String)obj;
                } catch (NumberFormatException e) {
                    // Null Object for Decimal Datatype returned, see Null-Object Pattern
                    csvField = nullRepresentationString;
                }

                fieldsToExport[j] = csvField;
            }

            writer.writeNext(fieldsToExport);

            if (monitor.isCanceled()) {
                return;
            }

            monitor.worked(1);
        }
    }

}
