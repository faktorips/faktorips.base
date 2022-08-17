/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

import org.faktorips.runtime.IRuntimeRepository;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler class for ips table contents.
 * 
 * @author Joerg Ortmann
 */
public class TableSaxHandler extends DefaultHandler {

    private static final String NULL_VALUE = "\\N";
    private static final String CSV_FORMAT = "CSV";

    private static final String VALUE = "Value";
    private static final String ROW = "Row";
    private static final String ROWS = "Rows";
    private static final String PROPERTY_FORMAT = "format";

    // the table which will be filled
    private Table<?> table;

    // contains all column values,
    private List<String> columns = new ArrayList<>(20);

    // buffer to store the characters inside the value node
    private StringBuilder textBuilder = null;

    // true if the parser is inside the row node
    private boolean insideRowNode;

    // true if the parser is inside the rows node with format=CSV
    private boolean insideCsvContent;

    // true if the parser is inside the value node
    private boolean insideValueNode;

    // true if the current value node represents the null value
    private boolean nullValue;

    // the product repository to get product information from
    private IRuntimeRepository productRepository;

    public TableSaxHandler(Table<?> table, IRuntimeRepository productRepository) {
        this.table = table;
        this.productRepository = productRepository;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ROWS.equals(qName) && insideCsvContent) {
            insideCsvContent = false;
            initFromCsv(getText());
        } else if (ROW.equals(qName)) {
            insideRowNode = false;
            table.addRow(columns, productRepository);
            columns.clear();
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(getText());
            textBuilder = null;
        }
    }

    private String getText() {
        if (textBuilder != null) {
            return textBuilder.toString();
        } else {
            return nullValue ? null : IpsStringUtils.EMPTY;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ROWS.equals(qName)) {
            insideCsvContent = isFormatCsv(attributes);
        } else if (ROW.equals(qName)) {
            insideRowNode = true;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.parseBoolean(attributes.getValue("isNull"));
        }
    }

    private boolean isFormatCsv(Attributes attributes) {
        String format = attributes.getValue(PROPERTY_FORMAT);
        return CSV_FORMAT.equals(format);
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (!insideValueNode && !insideCsvContent) {
            // ignore characters which are not inside a value node
            return;
        }
        String s = new String(buf, offset, len);
        if (textBuilder == null) {
            textBuilder = new StringBuilder(s);
        } else {
            textBuilder.append(s);
        }
    }

    /*
     * Returns <code>true</code> if the given node is the column value node otherwise
     * <code>false</code>
     */
    private boolean isColumnValueNode(String nodeName) {
        return VALUE.equals(nodeName) && insideRowNode;
    }

    private void initFromCsv(String csv) {
        StringReader stringReader = new StringReader(csv);
        try {
            getClass().getClassLoader().loadClass("com.opencsv.CSVReader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load OpenCSV", e);
        }
        CSVReader csvReader = new CSVReader(stringReader, '|', '"', '\\');
        try {
            String[] csvLine;
            while ((csvLine = csvReader.readNext()) != null) {
                for (int i = 0; i < csvLine.length; i++) {
                    if (NULL_VALUE.equals(csvLine[i])) {
                        csvLine[i] = null;
                    }
                }
                table.addRow(Arrays.asList(csvLine), productRepository);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                csvReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
