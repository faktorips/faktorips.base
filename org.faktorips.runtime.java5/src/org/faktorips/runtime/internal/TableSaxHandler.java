/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.List;

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
    private Table table;
    
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
    
    public TableSaxHandler(Table table) {
        this.table = table;
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = false;
            table.addRow(columns);
            columns.clear();
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(textBuffer == null && nullValue ? null : textBuffer == null ? new String("") : textBuffer
                    .substring(0));
            textBuffer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = true;
        } else if (isColumnValueNode(qName)){
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue("isNull")).booleanValue();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (!insideValueNode) {
            // ignore characters which are not inside a value node
            return;
        }
        String s = new String(buf, offset, len);
        if (textBuffer == null){
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }
    
    /*
     * Returns <code>true</code> if the given node is the column value node otherwise <code>false</code>
     */
    private boolean isColumnValueNode(String nodeName){
        return VALUE.equals(nodeName) && insideRowNode;
    }
}
