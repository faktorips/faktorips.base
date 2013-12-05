/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.values.DefaultInternationalString;
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

    public static final String XML_ATTRIBUTE_IS_NULL = "isNull"; //$NON-NLS-1$

    public static final String XML_TAGNAME_VALUE = "Value"; //$NON-NLS-1$

    private static final String XML_ELEMENT_DATA = "Data"; //$NON-NLS-1$

    private static final String XML_ELEMENT_ENUM = "Enum"; //$NON-NLS-1$

    private static final String XML_ELEMENT_STEP = "Step"; //$NON-NLS-1$

    private static final String XML_ELEMENT_UPPER_BOUND = "UpperBound"; //$NON-NLS-1$

    private static final String XML_ELEMENT_LOWER_BOUND = "LowerBound"; //$NON-NLS-1$

    private static final String XML_ATTRIBUTE_CONTAINS_NULL = "containsNull"; //$NON-NLS-1$

    private static final String XML_ELEMENT_RANGE = "Range"; //$NON-NLS-1$

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
     * Adds the {@link DefaultInternationalString} to the given xml element. Takes care of proper null
     * handling.
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
     * {@link #addInternationalStringToElement(DefaultInternationalString, Element, String)}. The created
     * element then is returned.
     * 
     * @param value the {@link DefaultInternationalString} to be added.
     * @param el the XML element to add the value to.
     * @param tagName the tag name for the element that stored the value.
     * @return the created element with the given tag name, that contains the given value.
     */
    private static Element addInternationalStringAndReturnElement(DefaultInternationalString value, Element el, String tagName) {
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
     * Returns the {@link DefaultInternationalString} stored in the child element of the given element with
     * the indicated name. Returns an empty DefaultInternationalString if the value is null or no such
     * child element exists.
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
        Element rangeEl = XmlUtil.getFirstElement(valueSetEl, XML_ELEMENT_RANGE);
        if (rangeEl == null) {
            return null;
        }
        boolean containsNull = Boolean.valueOf(rangeEl.getAttribute(XML_ATTRIBUTE_CONTAINS_NULL)).booleanValue();
        String lowerBound = getValueFromElement(rangeEl, XML_ELEMENT_LOWER_BOUND);
        String upperBound = getValueFromElement(rangeEl, XML_ELEMENT_UPPER_BOUND);
        String step = getValueFromElement(rangeEl, XML_ELEMENT_STEP);

        return new Range(lowerBound, upperBound, step, containsNull);
    }

    public static EnumValues getEnumValueSetFromElement(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);
        if (valueSetEl == null) {
            return null;
        }
        Element enumEl = XmlUtil.getFirstElement(valueSetEl, XML_ELEMENT_ENUM);
        if (enumEl == null) {
            return null;
        }

        NodeList valueElements = enumEl.getElementsByTagName(XML_TAGNAME_VALUE);

        String[] values = new String[valueElements.getLength()];
        boolean containsNull = false;
        for (int i = 0; i < valueElements.getLength(); i++) {
            Element valueEl = (Element)valueElements.item(i);
            values[i] = getValueFromElement(valueEl, XML_ELEMENT_DATA);
            if (values[i] == null) {
                containsNull = true;
            }
        }
        return new EnumValues(values, containsNull);
    }

}
