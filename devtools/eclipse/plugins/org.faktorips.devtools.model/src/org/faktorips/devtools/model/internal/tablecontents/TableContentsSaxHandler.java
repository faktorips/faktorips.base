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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.internal.ipsobject.Description;
import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.internal.IpsStringUtils;
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
    private static final String COLUMNREFERENCE_TAG = TableColumnReference.XML_TAG;
    private static final String COLUMNREFERENCE_NAME = IpsElement.PROPERTY_NAME;

    // extension properties support
    private static final String EXTENSIONPROPERTIES = TableRows.getXmlExtPropertiesElementName();
    private static final String EXTENSIONPROPERTIES_VALUE = TableRows.getXmlValueElement();
    private static final String EXTENSIONPROPERTIES_ID = TableRows.getXmlAttributeExtpropertyid();
    private static final String EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL = TableRows.getXmlAttributeIsnull();

    /** the table which will be filled */
    private final TableContents tableContents;

    /** the referenced table structure, hold for performance optimization */
    private ITableStructure tableStructure;

    private final boolean readRowsContent;

    /** contains all column values */
    private List<String> columns = new ArrayList<>(20);

    /** buffer to store the characters inside the value node */
    private StringBuilder textBuilder = null;

    /** true if the parser is inside the row node */
    private boolean insideRowNode;

    /** true if the parser is inside the extension properties node */
    private boolean insideExtensionPropertiesNode;

    /** true if the parser is inside the value node */
    private boolean insideValueNode;

    /** true if the parser is inside the description node */
    private boolean insideDescriptionNode;

    /** true if the parser is inside the rows node with format=CSV */
    private boolean insideCsvContent;

    /** true if the current value node represents the null value */
    private boolean nullValue;

    /** contains the id of the extension property node */
    private String extensionPropertyId;

    private String currentDescriptionLocale;

    private TableRows currentTableRows;

    private String currentId;

    private String referenceName;

    public TableContentsSaxHandler(TableContents tableContents, boolean readRowsContent) {
        this.tableContents = tableContents;
        this.readRowsContent = readRowsContent;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        updateCurrentId(attributes);
        if (isTableContents(qName)) {
            tableContents.setTableStructureInternal(attributes.getValue(ATTRIBUTE_TABLESTRUCTURE));
            try {
                tableStructure = tableContents.findTableStructure(tableContents.getIpsProject());
            } catch (IpsException e) {
                tableStructure = null;
            }
            tableContents.setNumOfColumnsInternal(Integer.parseInt(attributes.getValue(ATTRIBUTE_NUMOFCOLUMNS)));
        } else if (COLUMNREFERENCE_TAG.equals(qName)) {
            referenceName = attributes.getValue(COLUMNREFERENCE_NAME);
        } else if (isTableRowsTag(qName)) {
            tableContents.migrateColumnReferences();
            readRowsContent();
            insideCsvContent = isFormatCsv(attributes);
        } else if (DESCRIPTION.equals(qName)) {
            insideDescriptionNode = true;
            currentDescriptionLocale = attributes.getValue(IDescription.PROPERTY_LOCALE);
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = true;
        } else if (ROW.equals(qName)) {
            insideRowNode = true;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.parseBoolean(attributes.getValue("isNull")); //$NON-NLS-1$
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.parseBoolean(attributes.getValue(EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL));
            extensionPropertyId = attributes.getValue(EXTENSIONPROPERTIES_ID);
        }

    }

    private boolean isFormatCsv(Attributes attributes) {
        String format = attributes.getValue(ITableRows.PROPERTY_FORMAT);
        return format != null && ITableRows.FORMAT_CSV.equals(format);
    }

    private boolean isTableContents(String qName) {
        return TABLECONTENTS.equals(qName) && !readRowsContent;
    }

    private boolean isTableRowsTag(String qName) {
        return ITableRows.TAG_NAME.equals(qName) || IIpsObjectGeneration.TAG_NAME.equals(qName);
    }

    private void readRowsContent() throws SAXException {
        if (readRowsContent) {
            currentTableRows = (TableRows)tableContents.createNewTableRowsInternal(currentId);
            tableContents.setTableRowsInternal(currentTableRows);
        } else {
            throw new SAXException("Skip reading table content"); //$NON-NLS-1$
        }
    }

    private void updateCurrentId(Attributes attributes) {
        if (attributes != null) {
            String idValue = attributes.getValue(IIpsObjectPart.PROPERTY_ID);
            currentId = idValue;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = false;
            currentTableRows.newRow(tableStructure, Optional.ofNullable(currentId), columns);
            columns.clear();
        } else if (DESCRIPTION.equals(qName)) {
            insideDescriptionNode = false;
            handleDescription();
            textBuilder = null;
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = false;
        } else if (insideCsvContent && isTableRowsTag(qName)) {
            insideCsvContent = false;
            currentTableRows.initFromCsv(getText());
            textBuilder = null;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(getText());
            textBuilder = null;
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = false;
            handleExtensionProperty();
            textBuilder = null;
        } else if (isAttributeReferenceNode(qName)) {
            tableContents.createColumnReferenceSaxHandler(referenceName);
            referenceName = null;

        }
    }

    private void handleExtensionProperty() throws SAXNotSupportedException {
        if (currentTableRows == null) {
            tableContents.addExtensionProperty(extensionPropertyId, getText());
        } else {
            throw new SAXNotSupportedException("Extension properties inside a generation node are not supported!"); //$NON-NLS-1$
        }
    }

    private void handleDescription() {
        if (!(IpsStringUtils.isEmpty(currentDescriptionLocale))) {
            Locale locale = new Locale(currentDescriptionLocale);
            Description description = (Description)tableContents.getDescription(locale);
            if (description == null) {
                description = (Description)tableContents.newDescription(currentId);
                description.setLocaleWithoutChangeEvent(locale);
            }
            description.setTextWithoutChangeEvent(getText());
        }
    }

    private String getText() {
        return textBuilder == null && nullValue ? null
                : textBuilder == null ? IpsStringUtils.EMPTY : textBuilder.toString();
    }

    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (insideDescriptionNode || insideValueNode || insideCsvContent) {
            String s = new String(buf, offset, len);
            if (textBuilder == null) {
                textBuilder = new StringBuilder(s);
            } else {
                textBuilder.append(s);
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

    /**
     * Returns <code>true</code> if the given node is the attributeReference node otherwise
     * <code>false</code>
     */
    private boolean isAttributeReferenceNode(String nodeName) {
        return COLUMNREFERENCE_TAG.equals(nodeName);
    }

}
