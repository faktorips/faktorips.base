/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
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
 * 
 * @author Peter Erzberger, Thorsten Waertel
 */
public abstract class Table<T> implements ITable {

    /**
     * Contains all rows of this table.
     */
    protected List<T> rows;

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
    public void initFromXml(InputStream is, IRuntimeRepository productRepository) throws Exception {
        rows = new ArrayList<T>(200);
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(is), new TableSaxHandler(this, productRepository));
        ((ArrayList<T>)rows).trimToSize();
        initKeyMaps();
    }

    /**
     * Is used by generated classes within the initKeyMaps() method to identify a null values.
     * 
     * @param valueElement the element that contains the value for a field within a row
     * @return true if the the <i>isNull</i> attribute contains <i>true</i> otherwise false
     */
    protected boolean isNull(Element valueElement) {
        return Boolean.valueOf(valueElement.getAttribute("isNull")).booleanValue();
    }

    /**
     * Returns the String representation of up to ten of this table's rows.
     */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        Iterator<T> it = rows.iterator();
        for (int i = 0; it.hasNext() && i < 10; i++) {
            if (i != 0) {
                output.append("\n");
            }
            output.append(it.next().toString());
        }
        return output.toString();
    }

}
