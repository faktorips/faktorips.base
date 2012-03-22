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
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A helper class to load and save XML elements representing multi values as they are used in
 * attribute values.
 * <p>
 * This class should only contains static utility method mainly called by generated runtime code.
 * 
 * @author dirmeier
 * @author widmaier
 */
public final class MultiValueXmlHelper {

    public static final String XML_TAG_MULTIVALUE = "MultiValue"; //$NON-NLS-1$
    public static final String XML_TAG_VALUE = "Value"; //$NON-NLS-1$

    /**
     * Helper class should not be instantiated
     */
    private MultiValueXmlHelper() {
        // do not instantiate
    }

    /**
     * Reads String values from the a XML structure. Given the below XML (in this case an
     * AttributeValue-Element), this method will return a List containing three strings: {null,
     * "foo", "bar"}.
     * 
     * <pre>
     * &lt;AttributeValue&gt;
     *      &lt;Value&gt;
     *          &lt;MultiValue&gt;
     *              &lt;Value isNull="true"/&gt;
     *              &lt;Value isNull="false"/&gt; foo &lt;/Value&gt;
     *              &lt;Value isNull="false"/&gt; bar &lt;/Value&gt;
     *          &lt;/MultiValue&gt;
     *      &lt;/Value&gt;
     * &lt;/AttributeValue&gt;
     * </pre>
     * 
     * @param attrValueElement the element to extract multiple values from
     * @return a list containing all values in the given element as strings
     * @throws NullPointerException if the outer value-tag or the MultiValue-Tag can not be found
     */
    public static List<String> getValuesFromXML(Element attrValueElement) {
        Element value = XmlUtil.getFirstElement(attrValueElement, XML_TAG_VALUE);
        assertElementExists(value, XML_TAG_VALUE);
        Element multiValueElement = XmlUtil.getFirstElement(value, XML_TAG_MULTIVALUE);
        assertElementExists(multiValueElement, XML_TAG_MULTIVALUE);

        ArrayList<String> list = new ArrayList<String>();
        NodeList valueNodeList = multiValueElement.getElementsByTagName(XML_TAG_VALUE);
        for (int i = 0; i < valueNodeList.getLength(); i++) {
            Element valueElement = (Element)valueNodeList.item(i);
            list.add(ValueToXmlHelper.getValueFromElement(valueElement));
        }
        return list;
    }

    /**
     * Throws a {@link NullPointerException} in case the given element is <code>null</code>.
     * 
     * @param element the element to check
     * @param xmlTagName the tag name that was expected
     */
    private static void assertElementExists(Element element, String xmlTagName) {
        if (element == null) {
            throw new NullPointerException("Inconsistent XML content. Element \"" + xmlTagName
                    + "\" was expected, but could not be found found.");
        }
    }

    /**
     * Adds all values in the given list as a "multi-value" child element to the given element. A
     * list containing the values {"foo", null, "bar"} will add the following elements to the given
     * element.
     * 
     * <pre>
     * &lt;Value&gt;
     *     &lt;MultiValue&gt;
     *         &lt;Value isNull="false"/&gt; foo &lt;/Value&gt;
     *         &lt;Value isNull="true"/&gt;
     *         &lt;Value isNull="false"/&gt; bar &lt;/Value&gt;
     *     &lt;/MultiValue&gt;
     * &lt;/Value&gt;
     * </pre>
     */
    public static void addValuesToElement(Element element, List<String> stringList) {
        Element outerValueElement = element.getOwnerDocument().createElement(XML_TAG_VALUE);
        element.appendChild(outerValueElement);

        Element multiValueElement = outerValueElement.getOwnerDocument().createElement(XML_TAG_MULTIVALUE);
        outerValueElement.appendChild(multiValueElement);

        for (String stringValue : stringList) {
            ValueToXmlHelper.addValueToElement(stringValue, multiValueElement, XML_TAG_VALUE);
        }
    }
}
