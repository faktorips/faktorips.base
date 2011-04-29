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

package org.faktorips.devtools.tableconversion.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

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
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" + typeToExport == null ? "null" //$NON-NLS-1$ //$NON-NLS-2$
                            : typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
        this.typeToExport = typeToExport;
        this.filename = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.exportColumnHeaderRow = exportColumnHeaderRow;
        messageList = list;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        IEnumValueContainer enumContainer = getEnum(typeToExport);

        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 4 + enumContainer.getEnumValuesCount());

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

        FileOutputStream out = null;
        CSVWriter writer = null;
        try {
            if (!monitor.isCanceled()) {
                // FS#1188 Tabelleninhalte exportieren: Checkbox "mit Spaltenueberschrift" und
                // Zielordner
                out = new FileOutputStream(new File(filename));

                char fieldSeparatorChar = getFieldSeparatorCSV(format);
                writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(out)), fieldSeparatorChar);

                exportHeader(writer, structure.getEnumAttributesIncludeSupertypeCopies(true), exportColumnHeaderRow);

                monitor.worked(1);

                exportDataCells(writer, enumContainer.getEnumValues(), structure, monitor, exportColumnHeaderRow);
                writer.close();
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ee) {
                    // ignore
                }
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
     * @throws CoreException thrown if an error occurs during the search for the datatypes of the
     *             structure.
     */
    private void exportDataCells(CSVWriter writer,
            List<IEnumValue> values,
            IEnumType structure,
            IProgressMonitor monitor,
            boolean exportColumnHeaderRow) throws CoreException {

        List<IEnumAttribute> enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(true);

        Datatype[] datatypes = new Datatype[structure.getEnumAttributesCountIncludeSupertypeCopies(true)];
        for (int i = 0; i < datatypes.length; i++) {
            datatypes[i] = enumAttributes.get(i).findDatatype(structure.getIpsProject());
        }

        for (int i = 0; i < values.size(); i++) {
            IEnumValue value = values.get(i);
            int numberOfAttributes = value.getEnumAttributeValuesCount();
            String[] fieldsToExport = new String[numberOfAttributes];
            for (int j = 0; j < numberOfAttributes; j++) {
                IEnumAttributeValue attributeValue = value.getEnumAttributeValues().get(j);
                Object obj = format.getExternalValue(attributeValue.getValue(), datatypes[j], messageList);

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
