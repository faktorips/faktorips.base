/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Internal helper class to read the table rows as CSV
 */
class TableRowsCsvHelper {

    private static final String NULL_VALUE = "\\N"; //$NON-NLS-1$

    private final TableRows tableRows;

    TableRowsCsvHelper(TableRows tableRows) {
        this.tableRows = tableRows;
    }

    private ITableContents getTableContents() {
        return tableRows.getTableContents();
    }

    private IIpsProject getIpsProject() {
        return tableRows.getIpsProject();
    }

    public void partsToCsv(Document doc, Element element) {
        String nullRepresentationString = getNullRepresentationString();
        try (StringWriter stringWriter = new StringWriter();
                CSVWriter csvWriter = new CSVWriter(stringWriter, '|', '"', '\\')) {
            int numOfColumns = getTableContents().getNumOfColumns();
            String[] nextLine = new String[numOfColumns];
            tableRows.getRowsAsList().forEach(r -> writeRowToCsv(csvWriter, nextLine, r, nullRepresentationString));
            String csv = stringWriter.toString();
            CDATASection text = doc.createCDATASection(csv);
            element.appendChild(text);
        } catch (IOException e) {
            throw asIpsException(e);
        }
    }

    /**
     * This is the string that represents a null value in the csv file. This does not need to be the
     * same as null presentation in preferences which is only a UI setting!
     */
    private String getNullRepresentationString() {
        return NULL_VALUE;
    }

    private void writeRowToCsv(CSVWriter csvWriter, String[] nextLine, Row row, String nullRepresentationString) {
        for (int i = 0; i < nextLine.length; i++) {
            String value = row.getValue(i);
            if (value == null) {
                value = nullRepresentationString;
            }
            nextLine[i] = value;
        }
        csvWriter.writeNext(nextLine, false);
    }

    private IpsException asIpsException(Exception e) {
        IpsLog.log(e);
        return new IpsException(new Status(IStatus.ERROR, IpsModelActivator.PLUGIN_ID,
                "Could not write table contents to CSV string", e)); //$NON-NLS-1$
    }

    public void initFromCsv(String csv) {
        String nullRepresentationString = getNullRepresentationString();
        try (StringReader stringReader = new StringReader(csv);
                CSVReader csvReader = new CSVReader(stringReader, '|', '"', '\\');) {
            ITableStructure structure = getTableContents().findTableStructure(getIpsProject());
            String[] csvLine;
            while ((csvLine = csvReader.readNext()) != null) {
                for (int i = 0; i < csvLine.length; i++) {
                    if (nullRepresentationString.equals(csvLine[i])) {
                        csvLine[i] = null;
                    }
                }
                tableRows.newRow(structure, Optional.empty(), Arrays.asList(csvLine));
            }
        } catch (IOException e) {
            throw asIpsException(e);
        }

    }

}
