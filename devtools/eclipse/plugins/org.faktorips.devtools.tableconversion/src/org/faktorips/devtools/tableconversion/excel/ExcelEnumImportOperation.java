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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.tableconversion.DatatypesHelper;
import org.faktorips.devtools.tableconversion.MultilingualEnumHelper;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

/**
 * Operation to import IPS enum types or contents from an Excel file with support for multilingual
 * attributes.
 */
public class ExcelEnumImportOperation extends AbstractExcelImportOperation {

    private static final Pattern LOCALE_CELL_PATTERN = Pattern
            .compile("\\[[A-Za-z]{2,}([-_][A-Za-z0-9]+)?\\]");

    private final IEnumValueContainer valueContainer;
    private final SequencedSet<Locale> locales;
    private List<IEnumAttribute> enumAttributes;

    public ExcelEnumImportOperation(IEnumValueContainer valueContainer, String filename, ExcelTableFormat format,
            String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList messageList,
            boolean importIntoExisting) {

        super(filename, format, nullRepresentationString, ignoreColumnHeaderRow, messageList, importIntoExisting);
        this.valueContainer = valueContainer;

        locales = valueContainer.getIpsProject().getReadOnlyProperties().getSupportedLanguages().stream()
                .map(ISupportedLanguage::getLocale)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        initDatatypes();
    }

    @Override
    protected void initDatatypes() {
        try {
            setDatatypes(DatatypesHelper.findEnumAttributeDatatypes(valueContainer,
                    valueContainer instanceof IEnumType));
            IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
            if (enumType == null) {
                return;
            }
            boolean includeLiteralName = valueContainer instanceof IEnumType;
            enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        } catch (IpsException e) {
            throw new IpsException(new IpsStatus("Failed to initialize datatypes for enum import.", e)); //$NON-NLS-1$
        }
    }

    @Override
    public void run(IProgressMonitor monitor) {
        IProgressMonitor progressMonitor;
        if (monitor == null) {
            progressMonitor = new NullProgressMonitor();
        } else {
            progressMonitor = monitor;
        }
        try {
            initWorkbookAndSheet();
            progressMonitor.beginTask("Import file " + getSourceFile(), 2 + getNumberOfExcelRows(getSheet())); //$NON-NLS-1$

            progressMonitor.worked(1);
            fillEnum(valueContainer, getSheet(), progressMonitor);

            if (progressMonitor.isCanceled()) {
                valueContainer.getIpsObject().getIpsSrcFile().discardChanges();
            }
            progressMonitor.worked(1);
            progressMonitor.done();
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(
                    NLS.bind(Messages.AbstractXlsTableImportOperation_errRead, getSourceFile()), e));
        }
    }

    private int getNumberOfExcelRows(Sheet sheet) {
        int numberRows = 0;
        boolean hasMultilingualAttributes = enumAttributes != null
                && enumAttributes.stream().anyMatch(IEnumAttribute::isMultilingual);
        int headerRows = isIgnoreColumnHeaderRow() ? (hasMultilingualAttributes ? 2 : 1) : 0;
        for (int i = headerRows;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                break;
            }
            numberRows++;
        }
        return numberRows;
    }

    // CSOFF: CyclomaticComplexity
    private void fillEnum(IEnumValueContainer valueContainer, Sheet sheet, IProgressMonitor monitor) {
        if (enumAttributes == null) {
            getMessageList().add(new Message("",
                    "Cannot import: enum type structure could not be resolved.", Message.ERROR)); //$NON-NLS-1$
            return;
        }
        boolean hasMultilingualAttributes = enumAttributes.stream().anyMatch(IEnumAttribute::isMultilingual);
        boolean hasLocaleHeaders = false;

        SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn;
        if (isIgnoreColumnHeaderRow() && hasMultilingualAttributes && hasMergedLocaleRegions(sheet)) {
            localesByColumn = readLocalesFromColumnHeaders(sheet);
            if (localesByColumn.isEmpty()) {
                getMessageList().add(new Message("",
                        "Multilingual attributes detected but locale headers are missing or malformed.", //$NON-NLS-1$
                        Message.ERROR));
                return;
            }
            hasLocaleHeaders = true;
        } else {
            localesByColumn = MultilingualEnumHelper.generateLocalesByColumn(enumAttributes, locales);
        }

        int headerRows = hasLocaleHeaders ? 2 : 1;
        int startRow = isIgnoreColumnHeaderRow() ? headerRows : 0;

        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                break;
            }
            if (isRowBlank(sheetRow)) {
                continue;
            }

            IEnumValue enumValue = valueContainer.newEnumValue();
            fillEnumValueAttributes(sheetRow, enumValue, i, localesByColumn);

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }
    // CSON: CyclomaticComplexity

    private boolean isRowBlank(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String stringValue = cell.toString();
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasMergedLocaleRegions(Sheet sheet) {
        return sheet.getMergedRegions().stream()
                .anyMatch(region -> region.getFirstRow() == 0
                        && region.getLastRow() == 0
                        && region.getLastColumn() > region.getFirstColumn());
    }

    // CSOFF: CyclomaticComplexity
    private LinkedHashMap<Integer, SequencedMap<Integer, Locale>> readLocalesFromColumnHeaders(Sheet sheet) {
        LinkedHashMap<Integer, SequencedMap<Integer, Locale>> result = new LinkedHashMap<>();

        Row localeHeaderRow = sheet.getRow(1);
        if (localeHeaderRow == null) {
            return result;
        }

        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.getFirstRow() == 0
                    && region.getLastRow() == 0
                    && region.getLastColumn() > region.getFirstColumn()) {

                LinkedHashMap<Integer, Locale> localesForColumns = new LinkedHashMap<>();

                for (int columnIndex = region.getFirstColumn(); columnIndex <= region.getLastColumn(); columnIndex++) {
                    Cell localeCell = localeHeaderRow.getCell(columnIndex);
                    if (localeCell == null || localeCell.getCellType() != CellType.STRING) {
                        return new LinkedHashMap<>();
                    }

                    String cellValue = localeCell.getStringCellValue();
                    if (cellValue == null || !LOCALE_CELL_PATTERN.matcher(cellValue).matches()) {
                        return new LinkedHashMap<>();
                    }

                    localesForColumns.put(columnIndex,
                            Locale.forLanguageTag(cellValue.substring(1, cellValue.length() - 1)
                                    .replace('_', '-')));
                }

                result.put(region.getFirstColumn(), localesForColumns);
            }
        }

        return result;
    }
    // CSON: CyclomaticComplexity

    private void fillEnumValueAttributes(Row sheetRow,
            IEnumValue enumValue,
            int rowIndex,
            SequencedMap<Integer, SequencedMap<Integer, Locale>> localesByColumn) {
        int mlOffset = 0;

        for (int i = 0; i < enumAttributes.size(); i++) {
            IEnumAttribute attribute = enumAttributes.get(i);
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(i);

            if (attribute.isMultilingual()) {
                SequencedMap<Integer, Locale> localeColumns = localesByColumn.get(i + mlOffset);

                if (localeColumns != null) {
                    List<LocalizedString> localizedStrings = new ArrayList<>(localeColumns.size());

                    for (Map.Entry<Integer, Locale> entry : localeColumns.entrySet()) {
                        int columnIndex = entry.getKey();
                        Locale locale = entry.getValue();
                        Cell localeCell = sheetRow.getCell(columnIndex);

                        if (localeCell == null) {
                            String msg = NLS.bind(
                                    "In row {0}, column {1} no value is set - imported {2} instead.", //$NON-NLS-1$
                                    new Object[] { rowIndex,
                                            attribute.getName() + MultilingualEnumHelper.formatLocaleTag(locale),
                                            IpsPlugin.getDefault().getIpsPreferences().getNullPresentation() });
                            getMessageList().add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                            localizedStrings.add(new LocalizedString(locale, null));
                        } else {
                            String cellValue = readCell(localeCell, getDatatypes()[i]);
                            localizedStrings.add(new LocalizedString(locale, cellValue));
                        }
                    }

                    mlOffset += localeColumns.size() - 1;
                    MultilingualEnumHelper.setInternationalStringValue(enumAttributeValue, localizedStrings);
                } else {
                    mlOffset += locales.size() - 1;
                    MultilingualEnumHelper.setInternationalStringValue(enumAttributeValue, List.of());
                }
            } else {
                Cell cell = sheetRow.getCell(i + mlOffset);
                if (cell == null) {
                    String msg = NLS.bind(
                            "In row {0}, column {1} no value is set - imported {2} instead.", //$NON-NLS-1$
                            new Object[] { rowIndex, i + mlOffset,
                                    IpsPlugin.getDefault().getIpsPreferences().getNullPresentation() });
                    getMessageList().add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                    setValueAttribute(enumAttributeValue, null);
                } else {
                    setValueAttribute(enumAttributeValue, readCell(cell, getDatatypes()[i]));
                }
            }
        }
    }

    private void setValueAttribute(IEnumAttributeValue enumAttribute, String value) {
        switch (enumAttribute.getValueType()) {
            case STRING -> enumAttribute.setValue(ValueFactory.createStringValue(value));
            case INTERNATIONAL_STRING -> {
                IValue<?> internationalStringValue = enumAttribute.getValue();
                IInternationalString content = (IInternationalString)internationalStringValue.getContent();
                Locale defaultLocale = valueContainer.getIpsProject().getReadOnlyProperties()
                        .getDefaultLanguage().getLocale();
                content.add(new LocalizedString(defaultLocale, value));
            }
        }
    }

}
