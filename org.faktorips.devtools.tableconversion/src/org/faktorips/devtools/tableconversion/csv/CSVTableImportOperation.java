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
import java.util.Arrays;
import java.util.Optional;

import com.opencsv.CSVReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.AbstractTableImportOperation;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.internal.tablecontents.TableRows;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Operation to import ipstablecontents from a comma separated values (CSV) file.
 * 
 * @author Roman Grutza
 */
public class CSVTableImportOperation extends AbstractTableImportOperation {

    /**
     * The table structure the imported table content is bound to
     */
    private ITableStructure structure;

    /**
     * Generation of the table contents the import has to be inserted.
     */
    private ITableRows tableRows;

    // CSOFF: ParameterNumberCheck
    public CSVTableImportOperation(ITableStructure structure, String sourceFile, ITableRows targetGeneration,
            ITableFormat format, String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList list,
            boolean importIntoExisting) {

        super(sourceFile, format, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);

        this.structure = structure;
        tableRows = targetGeneration;

        initDatatypes();
    }
    // CSON: ParameterNumberCheck

    private void initDatatypes() {
        IColumn[] columns = structure.getColumns();
        datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
    }

    @Override
    public void run(IProgressMonitor monitor) {
        try {
            monitor.beginTask("Import file " + sourceFile, IProgressMonitor.UNKNOWN); //$NON-NLS-1$

            MessageList ml = structure.validate(structure.getIpsProject());
            if (ml.containsErrorMsg()) {
                messageList.add(ml);
                return;
            }

            monitor.worked(1);
            if (monitor.isCanceled()) {
                return;
            }

            File importFile = new File(sourceFile);
            try (FileInputStream fis = new FileInputStream(importFile)) {
                // update datatypes because the structure might be altered if this operation is
                // reused
                initDatatypes();
                messageList.add(fillGeneration(fis));
            }

            monitor.worked(1);

            if (monitor.isCanceled()) {
                tableRows.getIpsObject().getIpsSrcFile().discardChanges();
            }

            monitor.done();
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(
                    NLS.bind(Messages.getString("CSVImportOperation_errRead"), sourceFile), e)); //$NON-NLS-1$
        }
    }

    private MessageList fillGeneration(FileInputStream fis) throws IOException {
        char fieldSeparator = getFieldSeparator();
        CSVReader reader = new CSVReader(new InputStreamReader(fis), fieldSeparator);

        try {
            // row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0
            // contains data. thus read over header if necessary
            if (ignoreColumnHeaderRow) {
                reader.readNext();
            }

            return readFromCsv(reader);
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

    private char getFieldSeparator() {
        String fieldSeparator = format.getProperty(CSVTableFormat.PROPERTY_FIELD_DELIMITER);
        if (fieldSeparator == null || fieldSeparator.length() != 1) {
            return ',';
        }

        return fieldSeparator.charAt(0);
    }

    /**
     * Reads the values from the CVS reader and adds them to the table rows, reporting errors in a
     * message list.
     *
     * @param reader the CVS reader
     */
    private MessageList readFromCsv(CSVReader reader) throws IOException {
        MessageList msgList = new MessageList();
        int expectedFields = structure.getNumOfColumns();
        Datatype[] datatypes = new Datatype[expectedFields];
        for (int i = 0; i < datatypes.length; i++) {
            String datatype = structure.getColumns()[i].getDatatype();
            datatypes[i] = structure.getIpsProject().findDatatype(datatype);
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
                msgList.add(new Message("", msg, Message.ERROR)); //$NON-NLS-1$
            }

            int columns = Math.min(structure.getNumOfColumns(), readLine.length);
            String[] values = new String[columns];
            for (int i = 0; i < columns; i++) {
                values[i] = readValueFromCsv(i, readLine, datatypes, nullRepresentationString,
                        msgList, rowNumber);
            }
            tableRows.newRow(structure, Optional.empty(), Arrays.asList(values));
            ++rowNumber;
        }
        ((TableRows)tableRows).markAsChanged();
        return msgList;
    }

    private String readValueFromCsv(int i,
            String[] csvLine,
            Datatype[] datatypes,
            String nullRepresentationString,
            MessageList messageList,
            int rowNumber) {
        String ipsValue = null;
        String csvField = csvLine[i];
        if (i < csvLine.length) {
            if (!(nullRepresentationString.equals(csvField))) {
                ipsValue = format.getIpsValue(csvField, datatypes[i], messageList);
            }
        }

        if (csvField == null) {
            Object[] objects = new Object[3];
            objects[0] = Integer.valueOf(rowNumber);
            objects[1] = Integer.valueOf(i);
            objects[2] = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            String msg = NLS
                    .bind("In row {0}, column {1} no value is set - imported {2} instead.", objects); //$NON-NLS-1$
            messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$

        }
        return ipsValue;
    }
}
