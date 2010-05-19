package org.faktorips.devtools.tableconversion.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.tableconversion.AbstractTableExportOperation;
import org.faktorips.util.message.MessageList;

/**
 * Table format for comma separated values (CSV).
 * 
 * @author Roman Grutza
 */
public class CSVTableFormat extends AbstractExternalTableFormat {

    // property constants following the JavaBeans standard
    public final static String PROPERTY_FIELD_DELIMITER = "fieldDelimiter"; //$NON-NLS-1$
    public final static String PROPERTY_DATE_FORMAT = "dateFormat"; //$NON-NLS-1$
    public static final String PROPERTY_DECIMAL_SEPARATOR_CHAR = "decimalFormat"; //$NON-NLS-1$
    public static final String PROPERTY_DECIMAL_GROUPING_CHAR = "decimalSeparatorChar"; //$NON-NLS-1$

    public CSVTableFormat() {
        // initialize table format specific properties
        properties.put(PROPERTY_FIELD_DELIMITER, ","); //$NON-NLS-1$
        properties.put(PROPERTY_DECIMAL_SEPARATOR_CHAR, "."); //$NON-NLS-1$

        Locale locale = IpsPlugin.getDefault().getUsedLanguagePackLocale();
        if (locale.equals(Locale.GERMAN)) {
            properties.put(PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        } else {
            properties.put(PROPERTY_DATE_FORMAT, "yyyy-MM-dd");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        try {
            CSVTableExportOperation tableExportOperation = new CSVTableExportOperation(contents, filename.toOSString(),
                    this, nullRepresentationString, exportColumnHeaderRow, list);
            tableExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public void executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) throws CoreException {

        CSVTableImportOperation tableImportOperation = new CSVTableImportOperation(structure, filename.toOSString(),
                targetGeneration, this, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);
        tableImportOperation.run(new NullProgressMonitor());
    }

    @Override
    public void executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) throws CoreException {

        CSVEnumImportOperation enumImportOperation = new CSVEnumImportOperation(valueContainer, filename.toOSString(),
                this, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);
        enumImportOperation.run(new NullProgressMonitor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {

        try {
            AbstractTableExportOperation enumExportOperation = new CSVEnumExportOperation(valueContainer, filename
                    .toOSString(), this, nullRepresentationString, exportColumnHeaderRow, list);
            enumExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidImportSource(String source) {
        File file = new File(source);

        if (!file.canRead()) {
            return false;
        }

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file));
            return hasConstantNumberOfFieldsPerLine(reader);
        } catch (Exception e) {
            // ignore FileNotFoundException, IOException
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // this is a serious problem, so report it.
                    IpsPlugin.log(e);
                }
            }
        }
    }

    /**
     * Examines if the number of fields stays the same throughout the whole file. Empty lines are
     * omitted.
     */
    private boolean hasConstantNumberOfFieldsPerLine(CSVReader reader) throws IOException {
        String[] row = reader.readNext();
        int expectedNumberOfFields = row.length;
        while ((row = reader.readNext()) != null) {
            if (isEmptyRow(row)) {
                // Handle empty lines as being valid. They can be skipped without information loss
                // appearing somewhere in the in the file. In case a CSV file ends with a newline,
                // which is totally reasonable, the file would be unintentionally marked as invalid
                // when not handling it here.
                continue;
            }

            if (expectedNumberOfFields != row.length) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmptyRow(String[] row) {
        return row.length == 1 && row[0].equals("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List getImportTablePreview(ITableStructure structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return getImportPreview(structure, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List getImportEnumPreview(IEnumType structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        return getImportPreview(structure, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
    }

    /**
     * @return A preview of the imported data to be imported using the given structure which can be
     *         an {@link ITableStructure} or an {@link IEnumType}.
     */
    @SuppressWarnings("unchecked")
    private List getImportPreview(IIpsObject structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        Datatype[] datatypes;
        try {
            if (structure instanceof ITableStructure) {
                datatypes = getDatatypes((ITableStructure)structure);
            } else if (structure instanceof IEnumType) {
                datatypes = getDatatypes((IEnumType)structure);
            } else {
                return Collections.EMPTY_LIST;
            }

            return getPreviewInternal(datatypes, filename, maxNumberOfRows, ignoreColumnHeaderRow, nullRepresentation);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return Collections.EMPTY_LIST;
        }
    }

    @SuppressWarnings("unchecked")
    private List getPreviewInternal(Datatype[] datatypes,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentation) {
        if (datatypes == null || filename == null || !isValidImportSource(filename.toOSString())) {
            return Collections.EMPTY_LIST;
        }

        List result = new ArrayList();
        MessageList ml = new MessageList();
        CSVReader reader = null;
        try {
            char fieldDelimiter = ',';
            if (getProperty(PROPERTY_FIELD_DELIMITER).length() == 1) {
                fieldDelimiter = getProperty(PROPERTY_FIELD_DELIMITER).charAt(0);
            }

            reader = new CSVReader(new FileReader(filename.toOSString()), fieldDelimiter);
            String[] line = (ignoreColumnHeaderRow == true) ? reader.readNext() : null;
            int linesLeft = maxNumberOfRows;
            while ((line = reader.readNext()) != null) {
                if (linesLeft-- <= 0) {
                    break;
                }
                if (isEmptyRow(line)) {
                    continue;
                }
                String[] convertedLine = new String[line.length];
                for (int i = 0; i < line.length; i++) {
                    if (nullRepresentation.equals(line[i])) {
                        convertedLine[i] = nullRepresentation;
                    } else {
                        convertedLine[i] = getIpsValue(line[i], datatypes[i], ml);
                    }
                }

                result.add(convertedLine);
            }
        } catch (Exception e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ee) {
                    // serious problem, report
                    IpsPlugin.log(ee);
                }
            }
        }
        return result;
    }

}
