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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.SequencedSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.tableconversion.DatatypesHelper;
import org.faktorips.devtools.tableconversion.MultilingualEnumHelper;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

/**
 * Operation to export Enum types or contents to an Excel file with support for multilingual
 * attributes.
 */
public class ExcelEnumExportOperation extends AbstractExcelExportOperation {

    private final IEnumValueContainer enumValueContainer;
    private final SequencedSet<Locale> locales;
    private List<IEnumAttribute> enumAttributes;
    private Datatype[] datatypes;

    public ExcelEnumExportOperation(IIpsObject typeToExport, String filename, ITableFormat format,
            String nullRepresentationString, boolean exportColumnHeaderRow, MessageList list) {

        super(typeToExport, filename, format, nullRepresentationString, exportColumnHeaderRow, list);
        if (!(typeToExport instanceof IEnumValueContainer)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" //$NON-NLS-1$
                            + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }
        enumValueContainer = (IEnumValueContainer)typeToExport;

        locales = enumValueContainer.getIpsProject().getReadOnlyProperties().getSupportedLanguages().stream()
                .map(ISupportedLanguage::getLocale)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void run(IProgressMonitor monitor) {
        IProgressMonitor progressMonitor = initProgressMonitor(monitor);
        progressMonitor.beginTask(Messages.TableExportOperation_labelMonitorTitle,
                2 + enumValueContainer.getEnumValuesCount());

        IEnumType structure = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
        if (structure == null) {
            getMessageList().add(new Message("", NLS.bind(Messages.TableExportOperation_errStructureNotFound, //$NON-NLS-1$
                    enumValueContainer.getQualifiedName()), Message.ERROR));
            return;
        }

        boolean includeLiteralName = enumValueContainer instanceof IEnumType;
        enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        datatypes = DatatypesHelper.findEnumAttributeDatatypes(enumValueContainer, includeLiteralName);

        initWorkbookAndSheet();
        progressMonitor.worked(1);

        int dataStartRow = exportHeader(getSheet(), isExportColumnHeaderRow());
        progressMonitor.worked(1);
        if (progressMonitor.isCanceled()) {
            return;
        }

        exportDataCells(getSheet(), enumValueContainer.getEnumValues(), progressMonitor, dataStartRow);
        if (progressMonitor.isCanceled()) {
            return;
        }

        try (FileOutputStream out = new FileOutputStream(new File(getFilename()))) {
            getWorkbook().write(out);
        } catch (IOException e) {
            IpsPlugin.log(e);
            getMessageList().add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
        }
        progressMonitor.done();
    }

    private int exportHeader(Sheet sheet, boolean exportColumnHeaderRow) {
        if (!exportColumnHeaderRow) {
            return 0;
        }

        boolean hasMultilingual = enumAttributes.stream().anyMatch(IEnumAttribute::isMultilingual);

        Row headerRow = sheet.createRow(0);
        int columnIndex = 0;

        for (IEnumAttribute attribute : enumAttributes) {
            if (attribute.isMultilingual()) {
                Cell headerCell = headerRow.createCell(columnIndex);
                headerCell.setCellValue(attribute.getName());
                int startCol = columnIndex;
                int endCol = columnIndex + locales.size() - 1;
                if (startCol < endCol) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, endCol));
                }
                columnIndex += locales.size();
            } else {
                Cell headerCell = headerRow.createCell(columnIndex);
                headerCell.setCellValue(attribute.getName());
                if (hasMultilingual) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 1, columnIndex, columnIndex));
                }
                columnIndex++;
            }
        }

        if (hasMultilingual) {
            Row localeRow = sheet.createRow(1);
            columnIndex = 0;
            for (IEnumAttribute attribute : enumAttributes) {
                if (attribute.isMultilingual()) {
                    for (Locale locale : locales) {
                        Cell localeCell = localeRow.createCell(columnIndex++);
                        localeCell.setCellValue(MultilingualEnumHelper.formatLocaleTag(locale));
                    }
                } else {
                    columnIndex++;
                }
            }
            return 2;
        }

        return 1;
    }

    private void exportDataCells(Sheet sheet, List<IEnumValue> values, IProgressMonitor monitor, int startRow) {
        for (int i = 0; i < values.size(); i++) {
            Row sheetRow = sheet.createRow(i + startRow);
            IEnumValue value = values.get(i);
            int mlOffset = 0;

            for (int j = 0; j < enumAttributes.size(); j++) {
                IEnumAttribute attribute = enumAttributes.get(j);

                if (attribute.isMultilingual()) {
                    int localeIndex = 0;
                    for (Locale locale : locales) {
                        String cellValue = getMultilingualCellValue(attribute, value, locale);
                        Cell cell = sheetRow.createCell(j + mlOffset + localeIndex);
                        fillCell(cell, cellValue, datatypes[j]);
                        localeIndex++;
                    }
                    mlOffset += locales.size() - 1;
                } else {
                    IEnumAttributeValue attributeValue = value.getEnumAttributeValue(attribute);
                    if (attributeValue == null) {
                        Cell cell = sheetRow.createCell(j + mlOffset);
                        fillCell(cell, null, datatypes[j]);
                    } else {
                        IValue<?> attrValue = attributeValue.getValue();
                        String cellValue = attrValue != null
                                ? attrValue.getDefaultLocalizedContent(enumValueContainer.getIpsProject())
                                : null;
                        Cell cell = sheetRow.createCell(j + mlOffset);
                        fillCell(cell, cellValue, datatypes[j]);
                    }
                }
            }

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private String getMultilingualCellValue(IEnumAttribute attribute, IEnumValue enumValue, Locale locale) {
        IEnumAttributeValue attributeValue = enumValue.getEnumAttributeValue(attribute);
        if (attributeValue == null) {
            return null;
        }
        IValue<?> value = attributeValue.getValue();
        if (value != null && value.getContent() instanceof IInternationalString intString) {
            LocalizedString localizedString = intString.get(locale);
            return localizedString != null ? localizedString.getValue() : null;
        }
        return null;
    }

}
