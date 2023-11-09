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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.values.DefaultInternationalString;
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
public enum MultiValueXmlHelper {
    /* no instances */;

    public static final String XML_TAG_MULTIVALUE = "MultiValue"; //$NON-NLS-1$
    public static final String XML_TAG_VALUE = "Value"; //$NON-NLS-1$

    /**
     * Reads String values from the an XML structure. Given the below XML (in this case an
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

        ArrayList<String> list = new ArrayList<>();
        NodeList valueNodeList = multiValueElement.getElementsByTagName(XML_TAG_VALUE);
        for (int i = 0; i < valueNodeList.getLength(); i++) {
            Element valueElement = (Element)valueNodeList.item(i);
            list.add(ValueToXmlHelper.getValueFromElement(valueElement));
        }
        return list;
    }

    /**
     * Reads {@link DefaultInternationalString} values from the XML structure.
     *
     * @param attrValueElement the element to extract multiple values from
     * @return a list containing all values in the given element as international strings
     * @throws NullPointerException if the outer value-tag or the MultiValue-Tag cannot be found
     */
    public static List<DefaultInternationalString> getInternationalStringsFromXML(Element attrValueElement) {
        Element value = XmlUtil.getFirstElement(attrValueElement, XML_TAG_VALUE);
        assertElementExists(value, XML_TAG_VALUE);
        Element multiValueElement = XmlUtil.getFirstElement(value, XML_TAG_MULTIVALUE);
        assertElementExists(multiValueElement, XML_TAG_MULTIVALUE);

        ArrayList<DefaultInternationalString> list = new ArrayList<>();
        NodeList valueNodeList = multiValueElement.getElementsByTagName(XML_TAG_VALUE);
        for (int i = 0; i < valueNodeList.getLength(); i++) {
            Element valueElement = (Element)valueNodeList.item(i);
            list.add(ValueToXmlHelper.getInternationalStringFromElement(XmlUtil.getFirstElement(valueElement,
                    InternationalStringXmlReaderWriter.XML_TAG)));
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
        outerValueElement.setAttribute("valueType", "MultiValue");
        element.appendChild(outerValueElement);

        Element multiValueElement = outerValueElement.getOwnerDocument().createElement(XML_TAG_MULTIVALUE);
        outerValueElement.appendChild(multiValueElement);

        for (String stringValue : stringList) {
            ValueToXmlHelper.addValueToElement(stringValue, multiValueElement, XML_TAG_VALUE);
        }
    }

    /**
     * Adds all {@link DefaultInternationalString international strings} in the given list as a
     * "multi-value" child element to the given element.
     */
    public static void addInternationalStringsToElement(Element element,
            List<DefaultInternationalString> internationalStringList) {
        Element outerValueElement = element.getOwnerDocument().createElement(XML_TAG_VALUE);
        outerValueElement.setAttribute("valueType", "MultiValue");
        element.appendChild(outerValueElement);

        Element multiValueElement = outerValueElement.getOwnerDocument().createElement(XML_TAG_MULTIVALUE);
        outerValueElement.appendChild(multiValueElement);

        for (DefaultInternationalString internationalStringValue : internationalStringList) {
            ValueToXmlHelper
                    .addInternationalStringToElement(internationalStringValue, multiValueElement, XML_TAG_VALUE);
        }
    }

}
