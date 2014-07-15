/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import au.com.bytecode.opencsv.CSVReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableImportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    private ITableRows targetGeneration;

    public CSVTableImportOperation(ITableStructure structure, String sourceFile, ITableRows targetGeneration,
            ITableFormat format, String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList list,
            boolean importIntoExisting) {

        super(sourceFile, format, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);

        this.structure = structure;
        this.targetGeneration = targetGeneration;

        initDatatypes();
    }

    private void initDatatypes() {
        try {
            IColumn[] columns = structure.getColumns();
            datatypes = new Datatype[columns.length];
            for (int i = 0; i < columns.length; i++) {
                datatypes[i] = columns[i].findValueDatatype(structure.getIpsProject());
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
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
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(importFile);

                // update datatypes because the structure might be altered if this operation is
                // reused
                initDatatypes();
                fillGeneration(targetGeneration, fis);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }

            monitor.worked(1);

            if (monitor.isCanceled()) {
                targetGeneration.getIpsObject().getIpsSrcFile().discardChanges();
            }

            monitor.done();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(
                    NLS.bind(Messages.getString("CSVImportOperation_errRead"), sourceFile), e)); //$NON-NLS-1$
        }
    }

    private void fillGeneration(ITableRows targetGeneration, FileInputStream fis) throws IOException {
        char fieldSeparator = getFieldSeparator();
        CSVReader reader = new CSVReader(new InputStreamReader(fis), fieldSeparator);

        try {
            // row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0
            // contains data. thus read over header if necessary
            if (ignoreColumnHeaderRow) {
                reader.readNext();
            }

            int expectedFields = structure.getNumOfColumns();
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

                IRow genRow = targetGeneration.newRow();
                for (short j = 0; j < Math.min(structure.getNumOfColumns(), readLine.length); j++) {
                    String ipsValue = null;

                    String tableField = readLine[j];
                    if (j < readLine.length) {
                        if (!(nullRepresentationString.equals(tableField))) {
                            ipsValue = getIpsValue(tableField, datatypes[j]);
                        }
                    }

                    if (tableField == null) {
                        Object[] objects = new Object[3];
                        objects[0] = Integer.valueOf(rowNumber);
                        objects[1] = Integer.valueOf(j);
                        objects[2] = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                        String msg = NLS
                                .bind("In row {0}, column {1} no value is set - imported {2} instead.", objects); //$NON-NLS-1$
                        messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$

                    }
                    genRow.setValue(j, ipsValue);
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
}
