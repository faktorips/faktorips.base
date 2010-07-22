/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class to write values to xml and retrieve them from xml.
 * 
 * @author Jan Ortmann
 */
public class ValueToXmlHelper {

    /**
     * Adds the value to the given xml element. Takes care of proper null handling. By value we mean
     * a value of a datatype, e.g. 42EUR is a value of the datatype money.
     * 
     * @param value the string representation of the value
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value
     */
    public final static void addValueToElement(String value, Element el, String tagName) {
        Element valueEl = el.getOwnerDocument().createElement(tagName);
        el.appendChild(valueEl);
        valueEl.setAttribute("isNull", value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (value != null) {
            valueEl.appendChild(el.getOwnerDocument().createTextNode(value));
        }
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
    public final static String getValueFromElement(Element el, String tagName) {
        Element valueEl = XmlUtil.getFirstElement(el, tagName);
        if (valueEl == null) {
            return null;
        }
        return getValueFromElement(valueEl);
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
    public final static String getValueFromElement(Element valueEl) {
        if (Boolean.valueOf(valueEl.getAttribute("isNull")).equals(Boolean.TRUE)) { //$NON-NLS-1$
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

    public final static Range getRangeFromElement(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);

        if (valueSetEl == null) {
            return null;
        }
        Element rangeEl = XmlUtil.getFirstElement(valueSetEl, "Range");
        if (rangeEl == null) {
            return null;
        }
        boolean containsNull = Boolean.valueOf(rangeEl.getAttribute("containsNull")).booleanValue();
        String lowerBound = getValueFromElement(rangeEl, "LowerBound");
        String upperBound = getValueFromElement(rangeEl, "UpperBound");
        String step = getValueFromElement(rangeEl, "Step");

        return new Range(lowerBound, upperBound, step, containsNull);
    }

    public final static EnumValues getEnumValueSetFromElement(Element el, String tagName) {
        Element valueSetEl = XmlUtil.getFirstElement(el, tagName);
        if (valueSetEl == null) {
            return null;
        }
        Element enumEl = XmlUtil.getFirstElement(valueSetEl, "Enum");
        if (enumEl == null) {
            return null;
        }

        NodeList valueElements = enumEl.getElementsByTagName("Value");

        String[] values = new String[valueElements.getLength()];
        boolean containsNull = false;
        for (int i = 0; i < valueElements.getLength(); i++) {
            Element valueEl = (Element)valueElements.item(i);
            values[i] = getValueFromElement(valueEl, "Data");
            if (values[i] == null) {
                containsNull = true;
            }
        }
        return new EnumValues(values, containsNull);
    }

    private ValueToXmlHelper() {
        // Utility class not to be instantiated.
    }

}
