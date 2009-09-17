/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractTableImportOperation;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

import au.com.bytecode.opencsv.CSVReader;

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
    private ITableContentsGeneration targetGeneration;

    public CSVTableImportOperation(ITableStructure structure, String sourceFile,
            ITableContentsGeneration targetGeneration, ITableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList list, boolean importIntoExisting) {

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
                datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("Import file " + sourceFile, IProgressMonitor.UNKNOWN);

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

            if (!monitor.isCanceled()) {
                targetGeneration.getIpsObject().getIpsSrcFile().save(true, monitor);
                monitor.worked(1);
            } else {
                targetGeneration.getIpsObject().getIpsSrcFile().discardChanges();
            }
            monitor.done();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(NLS
                    .bind(Messages.getString("CSVImportOperation_errRead"), sourceFile), e)); //$NON-NLS-1$
        }
    }

    private void fillGeneration(ITableContentsGeneration targetGeneration, FileInputStream fis) throws IOException {
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
                if (readLine.length != expectedFields) {
                    String msg = NLS.bind("Row {0} did not match the expected format.", rowNumber);
                    messageList.add(new Message("", msg, Message.ERROR)); //$NON-NLS-1$
                }

                IRow genRow = targetGeneration.newRow();
                for (short j = 0; j < Math.min(structure.getNumOfColumns(), readLine.length); j++) {
                    String ipsValue = null;

                    String tableField = readLine[j];
                    if (j < readLine.length) {
                        if (nullRepresentationString.equals(tableField)) {
                            ipsValue = null;
                        } else {
                            ipsValue = getIpsValue(tableField, datatypes[j]);
                        }
                    }

                    if (tableField == null) {
                        Object[] objects = new Object[3];
                        objects[0] = new Integer(rowNumber);
                        objects[1] = new Integer(j);
                        objects[2] = nullRepresentationString;
                        String msg = NLS
                                .bind("In row {0}, column {1} no value is set - imported {2} instead.", objects);
                        messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$

                    }
                    genRow.setValue(j, ipsValue);
                }
                ++rowNumber;
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // this is a serious problem, so report it.
                IpsPlugin.log(e);
            }
        }
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
