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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import com.opencsv.CSVReader;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

/**
 * Operation to import IPS enum types or contents from a CSV file.
 * 
 * @author Roman Grutza
 */
public class CSVEnumImportOperation implements ICoreRunnable {

    private final IEnumValueContainer valueContainer;
    private final String sourceFile;
    private final AbstractExternalTableFormat format;
    private final String nullRepresentationString;
    private final boolean ignoreColumnHeaderRow;
    private final MessageList messageList;
    private Datatype[] datatypes;
    private boolean includeLiteralName;

    public CSVEnumImportOperation(IEnumValueContainer valueContainer, String filename,
            AbstractExternalTableFormat format, String nullRepresentationString, boolean ignoreColumnHeaderRow,
            MessageList messageList) {

        this.valueContainer = valueContainer;
        sourceFile = filename;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        this.messageList = messageList;
        includeLiteralName = valueContainer instanceof IEnumType;

        initDatatypes(valueContainer);
    }

    private void initDatatypes(IEnumValueContainer valueContainer) {
        try {
            List<IEnumAttribute> enumAttributes = valueContainer.findEnumType(valueContainer.getIpsProject())
                    .getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
            datatypes = new Datatype[enumAttributes.size()];

            for (int i = 0; i < datatypes.length; i++) {
                IEnumAttribute enumAttribute = enumAttributes.get(i);
                ValueDatatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
                datatypes[i] = datatype;
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) {
        try {
            monitor.beginTask("Import file " + sourceFile, IProgressMonitor.UNKNOWN); //$NON-NLS-1$

            monitor.worked(1);
            if (monitor.isCanceled()) {
                return;
            }

            File importFile = new File(sourceFile);
            try (FileInputStream fis = new FileInputStream(importFile)) {
                fillEnum(valueContainer, fis);
            }

            monitor.worked(1);

            if (monitor.isCanceled()) {
                valueContainer.getIpsObject().getIpsSrcFile().discardChanges();
            }
            monitor.done();
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(
                    NLS.bind(Messages.getString("CSVImportOperation_errRead"), sourceFile), e)); //$NON-NLS-1$
        }
    }

    private void fillEnum(IEnumValueContainer valueContainer, FileInputStream fis) throws IOException {
        char fieldSeparator = getFieldSeparator();
        CSVReader reader = new CSVReader(new InputStreamReader(fis), fieldSeparator);

        try {
            // row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0
            // contains data. thus read over header if necessary
            if (ignoreColumnHeaderRow) {
                reader.readNext();
            }

            int expectedFields = valueContainer.findEnumType(valueContainer.getIpsProject())
                    .getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName);

            String[] readLine;
            int rowNumber = ignoreColumnHeaderRow ? 2 : 1;

            while ((readLine = reader.readNext()) != null) {
                if (isEmptyRow(readLine)) {
                    rowNumber++;
                    continue;
                }
                if (readLine.length != expectedFields) {
                    String msg = NLS.bind("Row {0} did not match the expected format.", rowNumber); //$NON-NLS-1$
                    messageList.add(new Message("", msg, Message.ERROR)); //$NON-NLS-1$
                }

                IEnumValue genRow = valueContainer.newEnumValue();
                for (short j = 0; j < Math.min(expectedFields, readLine.length); j++) {
                    String ipsValue;

                    IEnumAttributeValue column = genRow.getEnumAttributeValues().get(j);

                    String enumField = readLine[j];
                    if (nullRepresentationString.equals(enumField)) {
                        ipsValue = null;
                    } else {
                        ipsValue = getIpsValue(enumField, datatypes[j]);
                    }

                    if (enumField == null) {
                        Object[] objects = new Object[3];
                        objects[0] = Integer.valueOf(rowNumber);
                        objects[1] = Integer.valueOf(j);
                        objects[2] = nullRepresentationString;
                        String msg = NLS
                                .bind("In row {0}, column {1} no value is set - imported {2} instead.", objects); //$NON-NLS-1$
                        messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$

                    }
                    if (column.getValueType().equals(ValueType.STRING)) {
                        column.setValue(ValueFactory.createStringValue(ipsValue));
                    } else if (column.getValueType().equals(ValueType.INTERNATIONAL_STRING)) {
                        IValue<?> value = column.getValue();
                        IInternationalString content = (IInternationalString)value.getContent();
                        content.add(new LocalizedString(getDefaultLanguage(column.getIpsProject()), ipsValue));
                    }
                }
                ++rowNumber;
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // this is a serious problem, so report it.
                IpsPlugin.log(e);
            }
        }
    }

    private boolean isEmptyRow(String[] row) {
        return row.length == 1 && row[0].length() == 0;
    }

    private String getIpsValue(Object rawValue, Datatype datatype) {
        return format.getIpsValue(rawValue, datatype, messageList);
    }

    private char getFieldSeparator() {
        String fieldSeparator = format.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        if (fieldSeparator == null || fieldSeparator.length() != 1) {
            return ',';
        }

        return fieldSeparator.charAt(0);
    }

    private Locale getDefaultLanguage(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }
}
