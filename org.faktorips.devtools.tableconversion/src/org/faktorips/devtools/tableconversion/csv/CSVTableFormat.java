package org.faktorips.devtools.tableconversion.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Table format for comma separated values (CSV).
 * 
 * @author Roman Grutza
 */
public class CSVTableFormat extends AbstractExternalTableFormat {

    private boolean ignoreColumnHeaderRow;

    // property constants following the JavaBeans standard
    public final static String PROPERTY_FIELD_DELIMITER = "fieldDelimiter";
    public final static String PROPERTY_DOT_REPRESENTATION = "dotRepresentation";
    public final static String PROPERTY_DATE_FORMAT = "dateFormat";

    public CSVTableFormat() {
        // initialize table format specific properties
        properties.put(PROPERTY_FIELD_DELIMITER, ",");
        properties.put(PROPERTY_DOT_REPRESENTATION, ".");
        properties.put(PROPERTY_DATE_FORMAT, "yyyy-MM-dd");
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public boolean executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list) {

        try {
            CSVTableImportOperation tableImportOperation = new CSVTableImportOperation(structure,
                    filename.toOSString(), targetGeneration, this, nullRepresentationString, ignoreColumnHeaderRow,
                    list);
            tableImportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list) {

        try {
            CSVEnumImportOperation enumImportOperation = new CSVEnumImportOperation(valueContainer, filename
                    .toOSString(), this, nullRepresentationString, ignoreColumnHeaderRow, list);
            enumImportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {

        try {
            CSVEnumExportOperation enumExportOperation = new CSVEnumExportOperation(valueContainer, filename
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

    private boolean hasConstantNumberOfFieldsPerLine(CSVReader reader) throws IOException {
        String[] row = reader.readNext();
        int expectedNumberOfFields = row.length;
        while ((row = reader.readNext()) != null) {
            if (expectedNumberOfFields != row.length) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List getImportTablePreview(ITableStructure structure, IPath filename, int maxNumberOfRows) {
        if (structure == null || filename == null || !isValidImportSource(filename.toOSString())) {
            return Collections.EMPTY_LIST;
        }

        List result = new ArrayList();
        MessageList ml = new MessageList();
        CSVReader reader = null;
        try {
            Datatype[] datatypes = getDatatypes(structure);

            reader = new CSVReader(new FileReader(filename.toOSString()));
            String[] line = (ignoreColumnHeaderRow == true) ? reader.readNext() : null;
            int linesLeft = maxNumberOfRows;
            while ((line = reader.readNext()) != null) {
                if (linesLeft-- <= 0) {
                    break;
                }
                String[] convertedLine = new String[line.length];
                for (int i = 0; i < line.length; i++) {
                    convertedLine[i] = getIpsValue(line[i], datatypes[i], ml);
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

    @SuppressWarnings("unchecked")
    public List getImportEnumPreview(IEnumType structure, IPath filename, int maxNumberOfRows) {
        Datatype[] datatypes;
        try {
            datatypes = getDatatypes(structure);
            return getPreviewInternal(datatypes, filename, maxNumberOfRows);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return Collections.EMPTY_LIST;
        }
    }

    @SuppressWarnings("unchecked")
    private List getPreviewInternal(Datatype[] datatypes, IPath filename, int maxNumberOfRows) {
        if (datatypes == null || filename == null || !isValidImportSource(filename.toOSString())) {
            return Collections.EMPTY_LIST;
        }

        List result = new ArrayList();
        MessageList ml = new MessageList();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(filename.toOSString()));
            String[] line = (ignoreColumnHeaderRow == true) ? reader.readNext() : null;
            int linesLeft = maxNumberOfRows;
            while ((line = reader.readNext()) != null) {
                if (linesLeft-- <= 0) {
                    break;
                }
                String[] convertedLine = new String[line.length];
                for (int i = 0; i < line.length; i++) {
                    convertedLine[i] = getIpsValue(line[i], datatypes[i], ml);
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

    // TODO rg: Duplicated code in CSVTableImportOperation
    private Datatype[] getDatatypes(ITableStructure structure) throws CoreException {
        IColumn[] columns = structure.getColumns();
        Datatype[] datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
        return datatypes;
    }

    private Datatype[] getDatatypes(IEnumType structure) throws CoreException {
        List<IEnumAttribute> enumAttributes = structure.getEnumAttributes();
        Datatype[] datatypes = new Datatype[enumAttributes.size()];
        for (int i = 0; i < datatypes.length; i++) {
            IEnumAttribute enumAttribute = enumAttributes.get(i);
            datatypes[i] = enumAttribute.findDatatype(enumAttribute.getIpsProject());
        }
        return datatypes;
    }
}
