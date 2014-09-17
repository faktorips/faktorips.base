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

import org.faktorips.values.DefaultInternationalString;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class to write values to XML and retrieve them from XML.
 * 
 * @author Jan Ortmann
 */
public class ValueToXmlHelper {

    /**
     * Used for both the value of a config element and the values of an enum value set.
     */
    public static final String XML_TAG_VALUE = "Value"; //$NON-NLS-1$
    public static final String XML_TAG_VALUE_SET = "ValueSet"; //$NON-NLS-1$
    public static final String XML_TAG_DATA = "Data"; //$NON-NLS-1$

    public static final String XML_TAG_CONFIG_ELEMENT = "ConfigElement";
    public static final String XML_TAG_ATTRIBUTE_VALUE = "AttributeValue";
    public static final String XML_ATTRIBUTE_ATTRIBUTE = "attribute";

    public static final String XML_TAG_ALL_VALUES = "AllValues"; //$NON-NLS-1$
    public static final String XML_TAG_ENUM = "Enum"; //$NON-NLS-1$
    public static final String XML_TAG_RANGE = "Range"; //$NON-NLS-1$
    public static final String XML_TAG_STEP = "Step"; //$NON-NLS-1$
    public static final String XML_TAG_UPPER_BOUND = "UpperBound"; //$NON-NLS-1$
    public static final String XML_TAG_LOWER_BOUND = "LowerBound"; //$NON-NLS-1$
    public static final String XML_TAG_TABLE_CONTENT_NAME = "TableContentName"; //$NON-NLS-1$
    public static final String XML_TAG_TABLE_CONTENT_USAGE = "TableContentUsage"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_STRUCTURE_USAGE = "structureUsage"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_IS_NULL = "isNull"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_CONTAINS_NULL = "containsNull"; //$NON-NLS-1$

    private ValueToXmlHelper() {
        // Utility class not to be instantiated.
    }

    /**
     * Adds the value to the given xml element. Takes care of proper null handling. By value we mean
     * a value of a datatype, e.g. 42EUR is a value of the datatype money.
     * 
     * @param value the string representation of the value
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value
     */
    public static void addValueToElement(String value, Element el, String tagName) {
        addValueAndReturnElement(value, el, tagName, false);
    }

    /**
     * Adds the {@link DefaultInternationalString} to the given xml element. Takes care of proper
     * null handling.
     * 
     * @param value the {@link DefaultInternationalString} to be added.
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value.
     */
    public static void addInternationalStringToElement(DefaultInternationalString value, Element el, String tagName) {
        addInternationalStringAndReturnElement(value, el, tagName);
    }

    /**
     * Adds the value to the given xml element. The value is inserted inside a CDATA section.
     * 
     * @param value the string representation of the value
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value
     */
    public static void addCDataValueToElement(String value, Element el, String tagName) {
        addValueAndReturnElement(value, el, tagName, true);
    }

    /**
     * Adds the value to the given xml element as does
     * {@link #addValueToElement(String, Element, String)}. The created element then is returned.
     * 
     * @param value the string representation of the value
     * @param el the XML element to add the value to.
     * @param tagName the tag name for the element that stored the value
     * @param useCDataSection when <code>true</code> the value is inserted into a CDATA section,
     *            otherwise it is added as text directly.
     * @return the created element with the given tag name, that contains the given value.
     */
    private static Element addValueAndReturnElement(String value, Element el, String tagName, boolean useCDataSection) {
        Document ownerDocument = el.getOwnerDocument();
        Element valueEl = createValueElement(value, tagName, ownerDocument, useCDataSection);
        el.appendChild(valueEl);
        return valueEl;
    }

    /**
     * Adds the {@link DefaultInternationalString} to the given xml element as does
     * {@link #addInternationalStringToElement(DefaultInternationalString, Element, String)}. The
     * created element then is returned.
     * 
     * @param value the {@link DefaultInternationalString} to be added.
     * @param el the XML element to add the value to.
     * @param tagName the tag name for the element that stored the value.
     * @return the created element with the given tag name, that contains the given value.
     */
    private static Element addInternationalStringAndReturnElement(DefaultInternationalString value,
            Element el,
            String tagName) {
        Document ownerDocument = el.getOwnerDocument();
        Element valueEl = ownerDocument.createElement(tagName);
        Element internationalStringEl = InternationalStringXmlReaderWriter.toXml(ownerDocument, value);
        valueEl.appendChild(internationalStringEl);
        el.appendChild(valueEl);
        return valueEl;
    }

    public static Element createValueElement(String value,
            String tagName,
            Document ownerDocument,
            boolean useCDataSection) {
        Element valueEl = ownerDocument.createElement(tagName);
        valueEl.setAttribute(XML_ATTRIBUTE_IS_NULL, value == null ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        if (value != null) {
            if (useCDataSection) {
                valueEl.appendChild(ownerDocument.createCDATASection(value));
            } else {
                valueEl.appendChild(ownerDocument.createTextNode(value));
            }
        }
        return valueEl;
    }

    /**
     * Adds the value to the given xml element as does
     * {@link #addValueToElement(String, Element, String)}. The created element then is returned.
     * 
     * @param value the string representation of the value
     * @param el the XML element to add the value to.
     * @param tagName the tag name for the element that stored the value
     * @return the created element with the given tag name, that contains the given value.
     */
    public static Element addValueAndReturnElement(String value, Element el, String tagName) {
        return addValueAndReturnElement(value, el, tagName, false);
    }

    /**
     * Adds a table usage to the XML element.
     * 
     * @param el the XML element to add the value to.
     * @param structureUsage the value for the structureUsage XML attribute
     * @param tableContentName the name of the used table content
     */
    public static void addTableUsageToElement(Element el, String structureUsage, String tableContentName) {
        Element tableContentElement = el.getOwnerDocument().createElement(XML_TAG_TABLE_CONTENT_USAGE);
        tableContentElement.setAttribute(XML_ATTRIBUTE_STRUCTURE_USAGE, structureUsage);
        addValueToElement(tableContentName, tableContentElement, XML_TAG_TABLE_CONTENT_NAME);
        el.appendChild(tableContentElement);
    }

    /**
     * Returns the string representation of the value stored in the child element of the given
     * element with the indicated name. Returns <code>null</code> if the value is null or no such
     * child element exists.
     * 
     * <pre>
     *     &lt;Parent&gt;
     *         &lt;Property isNull=&quot;false&quot;&gt;42&lt;/Property&gt;
     *     &lt;/Parent&gt;
     * </pre>
     * 
     * @param el The xml element that is the parent of the element storing the value.
     * @param tagName The name of the child
     */
    public static String getValueFromElement(Element el, String tagName) {
        Element valueEl = XmlUtil.getFirstElement(el, tagName);
        if (valueEl == null) {
            return null;
        }
        return getValueFromElement(valueEl);
    }

    /**
     * Returns the {@link DefaultInternationalString} stored in the child element of the given
     * element with the indicated name. Returns an empty DefaultInternationalString if the value is
     * null or no such child element exists.
     * 
     * @param el The xml element that is the parent of the element storing the international string.
     * @param tagName The name of the child
     */
    public static DefaultInternationalString getInternationalStringFromElement(Element el, String tagName) {
        return new DefaultInternationalString(InternationalStringXmlReaderWriter.fromXml(el, tagName));
    }

    /**
     * Returns the string representation of the value stored in given value element. Returns
     * <code>null</code> if the value is null, the attribute isNull is <code>true</code> or no such
     * child element exists.
     * 
     * <pre>
     *         &lt;Property isNull=&quot;false&quot;&gt;42&lt;/Property&gt;
     * </pre>
     * 
     * @param valueEl The xml value element containing the value.
     */
    public static String getValueFromElement(Element valueEl) {
        if (Boolean.parseBoolean(valueEl.getAttribute(XML_ATTRIBUTE_IS_NULL))) {
            return null;
        }
        Text text = XmlUtil.getTextNode(valueEl);
        if (text != null) {
            return text.getData();
        }
        CDATASection cdata = XmlUtil.getFirstCDataSection(valueEl);
        // if no cdata-section was found, the value stored was an empty string.
        // In this case, the cdata-section get lost during transformation of the
        // xml-document to a string.
        String result = ""; //$NON-NLS-1$
        if (cdata != null) {
            result = cdata.getData();
        }
        return result;
    }

    /**
     * Returns the {@link DefaultInternationalString} stored in the given element. Returns an empty
     * DefaultInternationalString if the value is null.
     * 
     * @param el The xml element storing the international string.
     */
    public static DefaultInternationalString getInternationalStringFromElement(Element el) {
        return new DefaultInternationalString(InternationalStringXmlReaderWriter.fromXml(el));
    }

    public static Range getRangeFromElement(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);

        if (valueSetEl == null) {
            return null;
        }
        Element rangeEl = XmlUtil.getFirstElement(valueSetEl, XML_TAG_RANGE);
        if (rangeEl == null) {
            return null;
        }
        boolean containsNull = Boolean.valueOf(rangeEl.getAttribute(XML_ATTRIBUTE_CONTAINS_NULL)).booleanValue();
        String lowerBound = getValueFromElement(rangeEl, XML_TAG_LOWER_BOUND);
        String upperBound = getValueFromElement(rangeEl, XML_TAG_UPPER_BOUND);
        String step = getValueFromElement(rangeEl, XML_TAG_STEP);

        return new Range(lowerBound, upperBound, step, containsNull);
    }

    public static EnumValues getEnumValueSetFromElement(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);
        if (valueSetEl == null) {
            return null;
        }
        Element enumEl = XmlUtil.getFirstElement(valueSetEl, XML_TAG_ENUM);
        if (enumEl == null) {
            return null;
        }

        NodeList valueElements = enumEl.getElementsByTagName(XML_TAG_VALUE);

        String[] values = new String[valueElements.getLength()];
        boolean containsNull = false;
        for (int i = 0; i < valueElements.getLength(); i++) {
            Element valueEl = (Element)valueElements.item(i);
            values[i] = getValueFromElement(valueEl, XML_TAG_DATA);
            if (values[i] == null) {
                containsNull = true;
            }
        }
        return new EnumValues(values, containsNull);
    }

    public static <T> UnrestrictedValueSet<T> getUnrestrictedValueSet(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);
        if (valueSetEl != null) {
            Element enumEl = XmlUtil.getFirstElement(valueSetEl, XML_TAG_ALL_VALUES);
            if (enumEl != null) {
                if (enumEl.hasAttribute(XML_ATTRIBUTE_CONTAINS_NULL)) {
                    boolean containsNull = Boolean.valueOf(enumEl.getAttribute(XML_ATTRIBUTE_CONTAINS_NULL))
                            .booleanValue();
                    return new UnrestrictedValueSet<T>(containsNull);
                }
            }
        }
        return new UnrestrictedValueSet<T>(true);
    }
}
