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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A collection of utility methods for xml handling.
 * 
 * @author Jan Ortmann
 */
public class XmlUtil {

    public final static Element getFirstElement(Node parent, String tagName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    return (Element)nl.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the first Element node
     */
    public final static Element getFirstElement(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return (Element)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the child element with the given tag name and index. The index is the position of the
     * element considering all child elements with the given tag name.
     * 
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     */
    public final static Element getElement(Node parent, String tagName, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    if (count == index) {
                        return (Element)nl.item(i);
                    }
                    count++;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     * 
     */
    public final static Text getTextNode(Node node) {
        node.normalize();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section or <code>null</code> if the node hasn't got one.
     */
    public final static CDATASection getFirstCDataSection(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                return (CDATASection)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section if the node has one. If not, this returns the node's
     * text child node or <code>null</code> if the node hasn't got a text node.
     */
    public final static String getCDATAorTextContent(Node node) {
        if (getFirstCDataSection(node) != null) {
            return getFirstCDataSection(node).getData();
        } else if (getTextNode(node) != null) {
            return getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Returns the value of the first element with the given node name, starts searching by the
     * given Element
     * 
     * @param elem The first element (root or parent) element the search begins
     * @param nodeName The name searching for
     */
    public final static String getValueFromNode(Element elem, String nodeName) {
        String value = null;
        Node el = getFirstElement(elem, nodeName);
        if (el != null) {
            Node child = el.getFirstChild();
            value = child != null ? child.getNodeValue() : null;
        }
        return value;
    }

    /**
     * Returns a list of element's with the following criteria:
     * <ul>
     * <li>the node name must be equals to the given node name
     * <li>the node must contain an attribute with the attribute name
     * <li>the value of the attribute (with the given name) must be equal to the given value
     * </ul>
     */
    public static final List<Element> getElementsFromNode(Element elem,
            String nodeName,
            String attributeName,
            String attributeValue) {
        List<Element> result = new ArrayList<Element>();
        NodeList nl = elem.getChildNodes();
        for (int i = 0, max = nl.getLength(); i < max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String typeAttr = el.getAttribute(attributeName);
            if (attributeValue.equals(typeAttr) && el.getNodeName().equals(nodeName)) {
                result.add(el);
            }
        }
        return result;
    }

    private XmlUtil() {
        // Utility class not to be instantiated.
    }

}
