/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods for initFromXml methods used by {@link ProductComponent} and
 * {@link ProductComponentGeneration}.
 * 
 * 
 * @author dirmeier
 */
final class ProductComponentXmlUtil {

    static final String XML_TAG_FORMULA = "Formula";
    static final String XML_ATTR_FORMULA_SIGNATURE = "formulaSignature";
    static final String XML_TAG_EXPRESSION = "Expression";

    private ProductComponentXmlUtil() {
        // do not instantiate
    }

    /**
     * Returns a map containing the xml elements representing relations found in the indicated
     * generation's xml element. For each policy component type relation (pcTypeRelation) the map
     * contains an entry with the pcTypeRelation as key. The value is an array list containing all
     * relation elements for the pcTypeRelation.
     * 
     * @param genElement An xml element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    static final Map<String, List<Element>> getLinkElements(Element genElement) {
        Map<String, List<Element>> elementMap = new HashMap<String, List<Element>>();
        NodeList nl = genElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "Link".equals(node.getNodeName())) {
                Element childElement = (Element)nl.item(i);
                String association = childElement.getAttribute("association");
                List<Element> associationElements = elementMap.get(association);
                if (associationElements == null) {
                    associationElements = new ArrayList<Element>(1);
                    elementMap.put(association, associationElements);
                }
                associationElements.add(childElement);
            }

        }
        return elementMap;
    }

    /**
     * Returns a map containing the xml elements representing config elements found in the indicated
     * generation's xml element. For each config element the map contains an entry with the
     * pcTypeAttribute's name as key and the xml element containing the config element data as
     * value.
     * 
     * @param element An xml element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    // note: not private to allow access by test case
    static final Map<String, Element> getPropertyElements(Element element) {
        Map<String, Element> elementMap = new HashMap<String, Element>();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("ConfigElement".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    elementMap.put(childElement.getAttribute("attribute"), childElement);
                } else if ("AttributeValue".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    elementMap.put(childElement.getAttribute("attribute"), childElement);
                } else if ("TableContentUsage".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    String structureUsage = childElement.getAttribute("structureUsage");
                    elementMap.put(structureUsage, childElement);
                }

            }
        }
        return elementMap;
    }

    /**
     * Returns a set containing the formulaSignatures of all available formulars (with a not empty
     * expression) found in the indicated generation's xml element.
     * 
     * @param element An xml element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    static final Map<String, String> getAvailableFormulars(Element element) {
        Map<String, String> availableFormulas = new LinkedHashMap<String, String>();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element aFormula = (Element)node;
                if (XML_TAG_FORMULA.equals(node.getNodeName())) {
                    String name = aFormula.getAttribute(XML_ATTR_FORMULA_SIGNATURE);
                    NodeList nodeList = aFormula.getElementsByTagName(XML_TAG_EXPRESSION);
                    Element expressionElement = (Element)nodeList.item(0);

                    String content = expressionElement.getTextContent();
                    availableFormulas.put(name, content.trim());
                }
            }
        }
        return availableFormulas;
    }

}
