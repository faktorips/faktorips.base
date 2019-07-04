/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;

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
    private static final String VALUE = "Value";
    private static final String ROW = "Row";

    // the table which will be filled
    private Table<?> table;

    // contains all column values,
    private List<String> columns = new ArrayList<String>(20);

    // buffer to store the characters inside the value node
    private StringBuffer textBuffer = null;

    // true if the parser is inside the row node
    private boolean insideRowNode;

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
        if (ROW.equals(qName)) {
            insideRowNode = false;
            table.addRow(columns, productRepository);
            columns.clear();
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(textBuffer == null && nullValue ? null : textBuffer == null ? new String("") : textBuffer
                    .substring(0));
            textBuffer = null;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = true;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue("isNull")).booleanValue();
        }
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (!insideValueNode) {
            // ignore characters which are not inside a value node
            return;
        }
        String s = new String(buf, offset, len);
        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }

    /*
     * Returns <code>true</code> if the given node is the column value node otherwise
     * <code>false</code>
     */
    private boolean isColumnValueNode(String nodeName) {
        return VALUE.equals(nodeName) && insideRowNode;
    }
}
