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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.tableconversion.DatatypesHelper;
import org.faktorips.devtools.tableconversion.MultilingualEnumHelper;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.LocalizedString;

/**
 * Operation to import IPS enum types or contents from a CSV file with support for multilingual
 * attributes.
 */
public class CSVEnumImportOperation implements ICoreRunnable {

    private static final Pattern LOCALE_HEADER_PATTERN = Pattern
            .compile("(.+)\\[([A-Za-z]{2,}(?:[-_][A-Za-z0-9]+)?)\\]");

    private final IEnumValueContainer valueContainer;
    private final String sourceFile;
    private final AbstractExternalTableFormat format;
    private final String nullRepresentationString;
    private final boolean ignoreColumnHeaderRow;
    private final MessageList messageList;
    private final boolean includeLiteralName;
    private final SequencedSet<Locale> locales;
    private List<IEnumAttribute> enumAttributes;
    private Datatype[] datatypes;

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

        locales = valueContainer.getIpsProject().getReadOnlyProperties().getSupportedLanguages().stream()
                .map(ISupportedLanguage::getLocale)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        initDatatypes(valueContainer);
    }

    private void initDatatypes(IEnumValueContainer valueContainer) {
        try {
            datatypes = DatatypesHelper.findEnumAttributeDatatypes(valueContainer, includeLiteralName);
            IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
            if (enumType == null) {
                return;
            }
            enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        } catch (IpsException e) {
            throw new IpsException(new IpsStatus("Failed to initialize datatypes for CSV enum import.", e)); //$NON-NLS-1$
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
        if (enumAttributes == null) {
            messageList.add(new Message("",
                    "Cannot import: enum type structure could not be resolved.", Message.ERROR)); //$NON-NLS-1$
            return;
        }
        char fieldSeparator = getFieldSeparator();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(fis))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(fieldSeparator)
                        .build())
                .build()) {

            SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn;
            int expectedFields;

            if (ignoreColumnHeaderRow) {
                String[] headerLine = reader.readNext();
                localesByColumn = parseLocaleHeadersFromCSV(headerLine);
                expectedFields = computeExpectedFields(localesByColumn);
            } else {
                localesByColumn = generateLocalesByColumn();
                expectedFields = computeExpectedFields(localesByColumn);
            }

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
                    rowNumber++;
                    continue;
                }

                IEnumValue genRow = valueContainer.newEnumValue();
                fillEnumValueFromCSV(genRow, readLine, rowNumber, localesByColumn);
                ++rowNumber;
            }
        } catch (CsvValidationException e) {
            IpsPlugin.log(e);
            messageList.add(new Message("", "CSV parsing error: " + e.getMessage(), Message.ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    // CSOFF: CyclomaticComplexity
    private SequencedMap<Integer, SequencedMap<Integer, Locale>> parseLocaleHeadersFromCSV(String[] headerLine) {
        LinkedHashMap<Integer, SequencedMap<Integer, Locale>> localesByColumn = new LinkedHashMap<>();

        if (headerLine == null) {
            return generateLocalesByColumn();
        }

        boolean hasLocaleHeaders = false;
        for (String header : headerLine) {
            if (LOCALE_HEADER_PATTERN.matcher(header).matches()) {
                hasLocaleHeaders = true;
                break;
            }
        }

        if (!hasLocaleHeaders) {
            return generateLocalesByColumn();
        }

        int columnIndex = 0;
        int attrIndex = 0;
        while (columnIndex < headerLine.length && attrIndex < enumAttributes.size()) {
            IEnumAttribute attribute = enumAttributes.get(attrIndex);
            if (attribute.isMultilingual()) {
                LinkedHashMap<Integer, Locale> localeColumns = new LinkedHashMap<>();
                int firstColumn = columnIndex;
                while (columnIndex < headerLine.length) {
                    Matcher matcher = LOCALE_HEADER_PATTERN.matcher(headerLine[columnIndex]);
                    if (matcher.matches()) {
                        String localeName = matcher.group(1).trim();
                        if (localeName.equals(attribute.getName())) {
                            String localeTag = matcher.group(2).replace('_', '-');
                            localeColumns.put(columnIndex, Locale.forLanguageTag(localeTag));
                            columnIndex++;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!localeColumns.isEmpty()) {
                    localesByColumn.put(firstColumn, localeColumns);
                }
            } else {
                columnIndex++;
            }
            attrIndex++;
        }

        return localesByColumn;
    }
    // CSON: CyclomaticComplexity

    private SequencedMap<Integer, SequencedMap<Integer, Locale>> generateLocalesByColumn() {
        return MultilingualEnumHelper.generateLocalesByColumn(enumAttributes, locales);
    }

    private int computeExpectedFields(SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn) {
        int count = 0;
        for (IEnumAttribute attribute : enumAttributes) {
            if (attribute.isMultilingual()) {
                SequencedMap<Integer, Locale> localeColumns = findLocaleColumnsForAttribute(localesByColumn, count);
                count += localeColumns != null ? localeColumns.size() : locales.size();
            } else {
                count++;
            }
        }
        return count;
    }

    private SequencedMap<Integer, Locale> findLocaleColumnsForAttribute(
            SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn,
            int currentColumnOffset) {
        for (Map.Entry<Integer, SequencedMap<Integer, Locale>> entry : localesByColumn.entrySet()) {
            if (entry.getKey() == currentColumnOffset) {
                return entry.getValue();
            }
        }
        return null;
    }

    // CSOFF: CyclomaticComplexity
    private void fillEnumValueFromCSV(IEnumValue genRow,
            String[] readLine,
            int rowNumber,
            SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn) {
        int fieldIndex = 0;

        for (int attrIndex = 0; attrIndex < enumAttributes.size(); attrIndex++) {
            IEnumAttribute attribute = enumAttributes.get(attrIndex);
            IEnumAttributeValue enumAttributeValue = genRow.getEnumAttributeValues().get(attrIndex);

            if (attribute.isMultilingual()) {
                SequencedMap<Integer, Locale> localeColumns = localesByColumn.get(fieldIndex);
                if (localeColumns != null) {
                    List<LocalizedString> localizedStrings = new ArrayList<>(localeColumns.size());
                    int lastColumnIndex = fieldIndex;
                    for (Map.Entry<Integer, Locale> entry : localeColumns.entrySet()) {
                        int columnIndex = entry.getKey();
                        Locale locale = entry.getValue();
                        String value = columnIndex < readLine.length ? readLine[columnIndex] : null;
                        String ipsValue = getIpsValue(value, datatypes[attrIndex]);

                        if (IpsStringUtils.isBlank(value)) {
                            String msg = NLS.bind(
                                    "In row {0}, column {1} no value is set - imported {2} instead.", //$NON-NLS-1$
                                    new Object[] { rowNumber,
                                            attribute.getName() + MultilingualEnumHelper.formatLocaleTag(locale),
                                            nullRepresentationString });
                            messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                        }

                        localizedStrings.add(new LocalizedString(locale, ipsValue));
                        lastColumnIndex = columnIndex;
                    }
                    fieldIndex = lastColumnIndex + 1;
                    MultilingualEnumHelper.setInternationalStringValue(enumAttributeValue, localizedStrings);
                } else {
                    fieldIndex += locales.size();
                    MultilingualEnumHelper.setInternationalStringValue(enumAttributeValue, List.of());
                }
            } else {
                String enumField = fieldIndex < readLine.length ? readLine[fieldIndex] : null;
                String ipsValue = getIpsValue(enumField, datatypes[attrIndex]);

                if (IpsStringUtils.isBlank(enumField)) {
                    String msg = NLS.bind(
                            "In row {0}, column {1} no value is set - imported {2} instead.", //$NON-NLS-1$
                            new Object[] { rowNumber, fieldIndex, nullRepresentationString });
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                }

                switch (enumAttributeValue.getValueType()) {
                    case STRING -> enumAttributeValue.setValue(ValueFactory.createStringValue(ipsValue));
                    case INTERNATIONAL_STRING -> {
                        IValue<?> value = enumAttributeValue.getValue();
                        if (value != null) {
                            IInternationalString content = (IInternationalString)value.getContent();
                            Locale defaultLocale = valueContainer.getIpsProject().getReadOnlyProperties()
                                    .getDefaultLanguage().getLocale();
                            content.add(new LocalizedString(defaultLocale, ipsValue));
                        }
                    }
                }
                fieldIndex++;
            }
        }
    }
    // CSON: CyclomaticComplexity

    private String getIpsValue(String rawValue, Datatype datatype) {
        if (rawValue == null || nullRepresentationString.equals(rawValue)) {
            return null;
        }
        return format.getIpsValue(rawValue, datatype, messageList);
    }

    private boolean isEmptyRow(String[] row) {
        return row.length == 1 && row[0].length() == 0;
    }

    private char getFieldSeparator() {
        String fieldSeparator = format.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        if (fieldSeparator == null || fieldSeparator.length() != 1) {
            return ',';
        }

        return fieldSeparator.charAt(0);
    }

}
