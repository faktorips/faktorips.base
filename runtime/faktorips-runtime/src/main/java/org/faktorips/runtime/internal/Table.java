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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * The base class for all the generated table classes. The table content is read from a DOM element
 * by means of the initFromXml(Element) method. This class provides methods that are used by the
 * generated table classes to build up an in memory representation of the content so that clients of
 * the generated table classes can effectively access the table content by means of the generated
 * find methods.
 * <p>
 * Generated table classes have a java.util.Map or a org.faktorips.util.ReadOnlyBinaryRangeTree
 * member variable for each of their find methods. These maps or trees get filled at initialization
 * time and are accessed within the find methods. Maps are used as member variables if the value to
 * return can be identified by means of a hash key. A hash key can consist of multiple fields of
 * value types (e.g. integer, long, decimal). If the value to return is identified by a range of key
 * values a tree is used as the data structure to efficiently retrieve the value from. If the key
 * that identifies a value consists of a combination of hash keys and range fields a map is used
 * that contains trees as values. Depending on the number of range fields a tree hierarchy is build
 * up where the depth of the hierarchy is determined by the number of range fields. That means that
 * each node of a tree contains another tree representing the next level.
 */
public abstract class Table<R> implements ITable<R> {

    private static final String XML_ELEMENT_TABLE_CONTENTS = "TableContents";

    private static final String ATTRIBUTE_LOCALE = "locale";

    private static final String XML_ELEMENT_DESCRIPTION = "Description";

    /**
     * Contains all rows of this table.
     */
    // CSOFF: VisibilityModifierCheck
    // directly written to from generated subclasses
    protected List<R> rows;

    /**
     * The description for this table in all configured languages.
     */
    protected InternationalString description;
    // CSON: VisibilityModifierCheck

    /**
     * Contains the qualified name of this table.
     */
    private String name;

    /**
     * Default constructor for tables initialized from XML.
     * 
     * @since 23.6
     */
    public Table() {
    }

    /**
     * Constructor for tables created from code.
     * 
     * @since 23.6
     */
    public Table(String qualifiedTableName) {
        this.name = qualifiedTableName;
    }

    /**
     * Is used by the generated class to retrieve the values for a single row.
     *
     * @param columns List of objects that contain the values.
     */
    protected abstract void addRow(List<String> columns, IRuntimeRepository productRepository);

    /**
     * Is used by the generated classes to build up the the maps and trees that are used by the also
     * generated find-methods.
     */
    protected abstract void initKeyMaps();

    /**
     * Initializes this object with the data stored in the XML element.
     */
    public void initFromXml(InputStream is, IRuntimeRepository productRepository, String qualifiedTableName)
            throws Exception {
        rows = new ArrayList<>(200);
        name = qualifiedTableName;

        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(is), new TableSaxHandler(this, productRepository));
        ((ArrayList<R>)rows).trimToSize();
        init();
    }

    protected void init() {
        initKeyMaps();
        performAdditionalInitializations();
    }

    /**
     * Template method to perform additional initializations. Is called during the initialization of
     * the table (from XML), right after {@link #initKeyMaps()}.
     * <p>
     * Subclasses may override to provide an implementation. The default implementation is empty, so
     * no super-call is necessary.
     */
    protected void performAdditionalInitializations() {
        // implementation provided by subclasses
    }

    /**
     * Is used by generated classes within the initKeyMaps() method to identify a null values.
     *
     * @param valueElement the element that contains the value for a field within a row
     * @return true if the the <em>isNull</em> attribute contains <em>true</em> otherwise false
     */
    protected boolean isNull(Element valueElement) {
        return Boolean.parseBoolean(valueElement.getAttribute("isNull"));
    }

    /**
     * Returns the String representation of up to ten of this table's rows.
     */
    @Override
    public String toString() {
        return rows.stream()
                .limit(10)
                .map(R::toString)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription(Locale locale) {
        String string = description.get(locale);
        if (string == null) {
            return IpsStringUtils.EMPTY;
        } else {
            return string;
        }
    }

    @Override
    public List<R> getAllRows() {
        return Collections.unmodifiableList(rows);
    }

    private void writeDescriptionToXml(Element tableElement) {
        if (description != null) {
            for (LocalizedString localizedString : ((DefaultInternationalString)description).getLocalizedStrings()) {
                Element descriptionElement = tableElement.getOwnerDocument().createElement(XML_ELEMENT_DESCRIPTION);
                descriptionElement.setAttribute(ATTRIBUTE_LOCALE, localizedString.getLocale().toString());
                descriptionElement.setTextContent(localizedString.getValue());
                tableElement.appendChild(descriptionElement);
            }
        }
    }

    @Override
    public Element toXml(Document document) {
        IToXmlSupport.check(this);
        Element tableElement = document.createElement(XML_ELEMENT_TABLE_CONTENTS);
        writeDescriptionToXml(tableElement);
        ((IToXmlSupport)this).writePropertiesToXml(tableElement);
        return tableElement;
    }

}
