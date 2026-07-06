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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.SequencedSet;
import java.util.stream.Collectors;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
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
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.devtools.tableconversion.DatatypesHelper;
import org.faktorips.devtools.tableconversion.MultilingualEnumHelper;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

/**
 * Operation to export an Enum types or contents to a text-file (comma separated values) with
 * support for multilingual attributes.
 */
public class CSVEnumExportOperation extends AbstractTableExportOperation {

    private final boolean includeLiteralName;
    private final SequencedSet<Locale> locales;
    private List<IEnumAttribute> enumAttributes;

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

        super(typeToExport, filename, format, nullRepresentationString, exportColumnHeaderRow, list);
        if (!(typeToExport instanceof IEnumValueContainer enumContainer)) {
            throw new IllegalArgumentException(
                    "The given IPS object is not supported. Expected IEnumValueContainer, but got '" //$NON-NLS-1$
                            + typeToExport.getClass().toString() + "'"); //$NON-NLS-1$
        }

        includeLiteralName = enumContainer instanceof IEnumType;

        locales = enumContainer.getIpsProject().getReadOnlyProperties().getSupportedLanguages().stream()
                .map(ISupportedLanguage::getLocale)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        IEnumType structure = enumContainer.findEnumType(enumContainer.getIpsProject());
        if (structure != null) {
            enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) {
        IProgressMonitor localMonitor = monitor;
        if (localMonitor == null) {
            localMonitor = new NullProgressMonitor();
        }

        IEnumValueContainer enumContainer = (IEnumValueContainer)getTypeToExport();

        localMonitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 4 + enumContainer.getEnumValuesCount());

        // first of all, check if the environment allows an export..
        IEnumType structure = enumContainer.findEnumType(enumContainer.getIpsProject());
        if (structure == null) {
            String text = NLS.bind(Messages.TableExportOperation_errStructureNotFound,
                    enumContainer.getQualifiedName());
            getMessageList().add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return;
        }
        enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
        localMonitor.worked(1);

        getMessageList().add(enumContainer.validate(enumContainer.getIpsProject()));
        if (getMessageList().containsErrorMsg()) {
            return;
        }
        // if we have reached here, the environment is valid, so try to export the data
        localMonitor.worked(1);

        getMessageList().add(structure.validate(structure.getIpsProject()));
        if (getMessageList().containsErrorMsg()) {
            return;
        }

        localMonitor.worked(1);

        if (!localMonitor.isCanceled()) {
            char fieldSeparatorChar = getFieldSeparatorCSV(getFormat());
            try (FileOutputStream out = new FileOutputStream(new File(getFilename()));
                    ICSVWriter writer = new CSVWriterBuilder(new BufferedWriter(new OutputStreamWriter(out)))
                            .withSeparator(fieldSeparatorChar).build()) {

                exportHeader(writer, isExportColumnHeaderRow());
                localMonitor.worked(1);
                exportDataCells(writer, enumContainer, localMonitor);
            } catch (IOException e) {
                IpsPlugin.log(e);
                getMessageList().add(new Message("", Messages.TableExportOperation_errWrite, Message.ERROR)); //$NON-NLS-1$
            }
        }
    }

    /**
     * Writes the CSV header containing the names of the columns using the given CSV writer.
     *
     * @param writer A CSV writer instance
     * @param exportColumnHeaderRow Flag to indicate whether to export the header
     */
    private void exportHeader(ICSVWriter writer, boolean exportColumnHeaderRow) {
        if (!exportColumnHeaderRow) {
            return;
        }

        List<String> headerFields = new ArrayList<>();
        for (IEnumAttribute attribute : enumAttributes) {
            if (attribute.isMultilingual()) {
                for (Locale locale : locales) {
                    headerFields.add(attribute.getName() + MultilingualEnumHelper.formatLocaleTag(locale));
                }
            } else {
                headerFields.add(attribute.getName());
            }
        }
        writer.writeNext(headerFields.toArray(new String[0]));
    }

    /**
     * Create the cells for the export
     *
     * @param writer A CSV writer instance.
     * @param monitor The monitor to display the progress.
     *
     * @throws IpsException thrown if an error occurs during the search for the data types of the
     *             structure.
     */
    // CSOFF: CyclomaticComplexity
    private void exportDataCells(ICSVWriter writer, IEnumValueContainer enumContainer, IProgressMonitor monitor) {
        Datatype[] datatypes = DatatypesHelper.findEnumAttributeDatatypes(enumContainer, includeLiteralName);

        for (IEnumValue value : enumContainer.getEnumValues()) {
            List<String> fieldsToExport = new ArrayList<>();

            for (int j = 0; j < enumAttributes.size(); j++) {
                IEnumAttribute attribute = enumAttributes.get(j);
                IEnumAttributeValue attributeValue = value.getEnumAttributeValue(attribute);

                if (attributeValue == null) {
                    if (attribute.isMultilingual()) {
                        for (@SuppressWarnings("unused")
                        Locale locale : locales) {
                            fieldsToExport.add(getNullRepresentationString());
                        }
                    } else {
                        fieldsToExport.add(getNullRepresentationString());
                    }
                    continue;
                }

                if (attribute.isMultilingual()) {
                    IValue<?> attrValue = attributeValue.getValue();
                    for (Locale locale : locales) {
                        String cellValue = null;
                        if (attrValue != null && attrValue.getContent() instanceof IInternationalString intString) {
                            LocalizedString localizedString = intString.get(locale);
                            cellValue = localizedString != null ? localizedString.getValue() : null;
                        }
                        fieldsToExport.add(toCsvField(
                                getFormat().getExternalValue(cellValue, datatypes[j], getMessageList())));
                    }
                } else {
                    IValue<?> attrValue = attributeValue.getValue();
                    String content = attrValue != null
                            ? attrValue.getDefaultLocalizedContent(attributeValue.getIpsProject())
                            : null;
                    fieldsToExport.add(toCsvField(
                            getFormat().getExternalValue(content, datatypes[j], getMessageList())));
                }
            }

            writer.writeNext(fieldsToExport.toArray(new String[0]));

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }
    // CSON: CyclomaticComplexity

    private String toCsvField(Object obj) {
        try {
            return (obj == null) ? getNullRepresentationString() : (String)obj;
        } catch (ClassCastException e) {
            return getNullRepresentationString();
        }
    }

}
