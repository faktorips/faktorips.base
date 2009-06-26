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

package org.faktorips.runtime.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.KeyType;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.TwoColumnKey;
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
@SuppressWarnings("unchecked")
// TODO change table model to generic?
public abstract class Table implements ITable {

    /**
     * Contains all rows of this table.
     */
    protected List rows;

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
     * Initializes this object with the data stored in the xml element.
     * 
     * @throws Exception
     */
    public void initFromXml(InputStream is, IRuntimeRepository productRepository) throws Exception {
        rows = new ArrayList(200);
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(is), new TableSaxHandler(this, productRepository));
        ((ArrayList)rows).trimToSize();
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
     * This method is used by generated classes to convert the values of the provided map into
     * <code>ReadOnlyBinaryRangeTree</code> objects. The provided map can recursively contain maps.
     * For each level accept from the zero level the maps are converted into trees. On zero level
     * there is still a map. This map will be returned.
     * 
     * @param map starting from this map a new map is build up while the keys of this map are
     *            transfered but the values which are assumed to be maps are converted into
     *            <code>ReadOnlyBinaryRangeTree</code> objects recursively.
     * 
     * @param treeTypes an array of tree types. See in the <code>ReadOnlyBinaryRangeTree</code>
     *            description for possible tree types
     * @return the zero level map see description above
     */
    protected Map convert(Map map, KeyType[] treeTypes) {
        return convert(map, treeTypes, 0);
    }

    private Map convert(Map map, KeyType[] treeTypes, int level) {
        Map returnValue = new HashMap();
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            if (treeTypes.length == level) {
                return map;
            }
            returnValue.put(entry.getKey(), generateTree((Map)entry.getValue(), treeTypes, level));
        }
        return returnValue;
    }

    /**
     * This method is used by generated classes to convert the provided map into
     * <code>ReadOnlyBinaryRangeTree</code> object. The provided map can recursively contain maps.
     * If so each map is converted into a tree.
     * 
     * @param map is converted into a <code>ReadOnlyBinaryRangeTree</code>
     * @param treeTypes the tree types of the created <code>ReadOnlyBinaryRangeTree</code>. The
     *            length of this are must reflect the depth of the map hierarchy. See in the
     *            <code>ReadOnlyBinaryRangeTree</code> description for possible tree types
     * @return returns the created <code>ReadOnlyBinaryRangeTree</code>
     */
    protected ReadOnlyBinaryRangeTree generateTree(Map map, KeyType[] treeTypes) {
        return generateTree(map, treeTypes, 0);
    }

    private ReadOnlyBinaryRangeTree generateTree(Map map, KeyType[] treeTypes, int level) {
        Map nextLevel = convert(map, treeTypes, level + 1);
        return new ReadOnlyBinaryRangeTree(nextLevel, treeTypes[level]);
    }

    /**
     * Is used by generated classes within the initKeyMaps() method. It returns a value from the
     * provided map that is assumed to be of type java.util.Map. If no such entry is found a new
     * java.util.HashMap is created and added to the map for the key and the new Map is returned.
     */
    protected Map getMap(Map searchMap, Object key) {
        Map returnValue = (Map)searchMap.get(key);
        if (returnValue == null) {
            returnValue = new HashMap();
            searchMap.put(key, returnValue);
        }
        return returnValue;
    }

    /**
     * Is used by generated classes within the initKeyMaps() method for two-column ranges. It
     * returns a value from the provided map that is assumed to be of type java.util.Map. If no such
     * entry is found a new java.util.HashMap is created and added to the map for the key and the
     * new Map is returned.
     */
    protected Map getMap(Map searchMap, Comparable lowerBound, Comparable upperBound) {
        TwoColumnKey key = new TwoColumnKey(lowerBound, upperBound);
        Map returnValue = (Map)searchMap.get(key);
        if (returnValue == null) {
            returnValue = new HashMap();
            searchMap.put(key, returnValue);
        }
        return returnValue;
    }

    /**
     * Is used within the find-methods of the generated classes to retrieve a value from a map that
     * contains trees as values. The key is used get the according tree from the provided map while
     * the <i>treeKey</i> parameter is used to retrieve the values from the tree hierarchy. The
     * length of the array is equal to the tree hierarchy.
     */
    protected Object getValue(Map map, Object key, Comparable[] treeKey) {
        ReadOnlyBinaryRangeTree tree = (ReadOnlyBinaryRangeTree)map.get(key);
        return getValue(tree, treeKey);
    }

    /**
     * Is used within the find-methods of the generated classes to retrieve a value from a tree
     * hierarchy. The length of the <i>keys</i> array is equals to the depth of the tree hierarchy.
     */
    protected Object getValue(ReadOnlyBinaryRangeTree tree, Comparable[] keys) {
        return getValue(tree, keys, 0);
    }

    private Object getValue(ReadOnlyBinaryRangeTree tree, Comparable[] keys, int level) {

        if (tree == null) {
            return null;
        }
        Object value = tree.getValue(keys[level]);
        if (value == null || level == keys.length - 1) {
            return value;
        }
        return getValue((ReadOnlyBinaryRangeTree)value, keys, ++level);
    }

    /**
     * Returns the String representation of up to ten of this table's rows.
     */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        Iterator it = rows.iterator();
        for (int i = 0; it.hasNext() && i < 10; i++) {
            if (i != 0) {
                output.append("\n");
            }
            output.append(it.next().toString());
        }
        return output.toString();
    }
}
