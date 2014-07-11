/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.Description;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler class for ips table contents.<br>
 * 
 * @author Joerg Ortmann
 */
public class TableContentsSaxHandler extends DefaultHandler {

    private static final String TABLECONTENTS = IpsObjectType.TABLE_CONTENTS.getXmlElementName();
    private static final String VALUE = Row.VALUE_TAG_NAME;
    private static final String ROW = Row.TAG_NAME;
    private static final String DESCRIPTION = DescriptionHelper.XML_ELEMENT_NAME;
    private static final String ATTRIBUTE_TABLESTRUCTURE = ITableContents.PROPERTY_TABLESTRUCTURE;
    private static final String ATTRIBUTE_NUMOFCOLUMNS = ITableContents.PROPERTY_NUMOFCOLUMNS;

    // extension properties support
    private static final String EXTENSIONPROPERTIES = TableRows.getXmlExtPropertiesElementName();
    private static final String EXTENSIONPROPERTIES_VALUE = TableRows.getXmlValueElement();
    private static final String EXTENSIONPROPERTIES_ID = TableRows.getXmlAttributeExtpropertyid();
    private static final String EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL = TableRows.getXmlAttributeIsnull();

    /** the table which will be filled */
    private ITableContents tableContents;

    /** contains all column values */
    private List<String> columns = new ArrayList<String>(20);

    /** buffer to store the characters inside the value node */
    private StringBuffer textBuffer = null;

    /** true if the parser is inside the row node */
    private boolean insideRowNode;

    /** true if the parser is inside the extension properties node */
    private boolean insideExtensionPropertiesNode;

    /** true if the parser is inside the value node */
    private boolean insideValueNode;

    /** true if the parser is inside the description node */
    private boolean insideDescriptionNode;

    /** true if the current value node represents the null value */
    private boolean nullValue;

    /** contains the id of the value node */
    private String idValue;

    private String currentDescriptionLocale;

    private TableRows currentTableRows;

    private boolean readWholeContent;

    public TableContentsSaxHandler(ITableContents tableContents, boolean readWholeContent) {
        this.tableContents = tableContents;
        this.readWholeContent = readWholeContent;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = false;
            currentTableRows.newRow(columns);
            columns.clear();
        } else if (DESCRIPTION.equals(qName)) {
            insideDescriptionNode = false;
            if (!(StringUtils.isEmpty(currentDescriptionLocale))) {
                Locale locale = new Locale(currentDescriptionLocale);
                Description description = (Description)tableContents.getDescription(locale);
                if (description == null) {
                    description = (Description)tableContents.newDescription();
                    description.setLocaleWithoutChangeEvent(locale);
                }
                description.setTextWithoutChangeEvent(getText());
            }
            textBuffer = null;
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = false;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(getText());
            textBuffer = null;
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = false;
            if (currentTableRows == null) {
                tableContents.addExtensionProperty(idValue, getText());
            } else {
                throw new SAXNotSupportedException("Extension properties inside a generation node are not supported!"); //$NON-NLS-1$
            }
            textBuffer = null;
        }
    }

    private String getText() {
        return textBuffer == null && nullValue ? null : textBuffer == null ? StringUtils.EMPTY : textBuffer.toString();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (TABLECONTENTS.equals(qName)) {
            ((TableContents)tableContents).setTableStructureInternal(attributes.getValue(ATTRIBUTE_TABLESTRUCTURE));
            ((TableContents)tableContents).setNumOfColumnsInternal(Integer.parseInt(attributes
                    .getValue(ATTRIBUTE_NUMOFCOLUMNS)));
        } else if (ITableRows.TAG_NAME.equals(qName) || IIpsObjectGeneration.TAG_NAME.equals(qName)) {
            if (readWholeContent) {
                currentTableRows = (TableRows)((TableContents)tableContents).createNewTableRows();
            } else {
                throw new SAXException("Skip reading table content"); //$NON-NLS-1$
            }
        } else if (DESCRIPTION.equals(qName)) {
            insideDescriptionNode = true;
            currentDescriptionLocale = attributes.getValue(IDescription.PROPERTY_LOCALE);
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = true;
        } else if (ROW.equals(qName)) {
            insideRowNode = true;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue("isNull")).booleanValue(); //$NON-NLS-1$
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue(EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL)).booleanValue();
            idValue = attributes.getValue(EXTENSIONPROPERTIES_ID);
        }
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (insideDescriptionNode || insideValueNode) {
            String s = new String(buf, offset, len);
            if (textBuffer == null) {
                textBuffer = new StringBuffer(s);
            } else {
                textBuffer.append(s);
            }
        }
    }

    /**
     * Returns <code>true</code> if the given node is the column value node otherwise
     * <code>false</code>
     */
    private boolean isColumnValueNode(String nodeName) {
        return VALUE.equals(nodeName) && insideRowNode;
    }

    /**
     * Returns <code>true</code> if the given node is the extension properties value node otherwise
     * <code>false</code>
     */
    private boolean isExtensionPropertiesValueNode(String nodeName) {
        return EXTENSIONPROPERTIES_VALUE.equals(nodeName) && insideExtensionPropertiesNode;
    }

}
