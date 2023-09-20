/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.util.XsdValidatorHolder;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;

/**
 * A collection of utility methods for XML handling.
 *
 * @author Jan Ortmann
 */
public class XmlUtil {

    private static final Set<String> ELEMENTS_AND_CHILDREN_WITH_ID = Set.of("ExtensionProperties"); //$NON-NLS-1$

    private static final Map<IpsObjectType, ThreadLocal<Validator>> VALIDATORS = new ConcurrentHashMap<>();

    private XmlUtil() {
        // Utility class not to be instantiated.
    }

    public static final DocumentBuilder getDefaultDocumentBuilder() {
        return org.faktorips.runtime.internal.XmlUtil.getDocumentBuilder();
    }

    public static final void setAttributeConvertNullToEmptyString(Element el, String attribute, String value) {
        if (value == null) {
            el.setAttribute(attribute, ""); //$NON-NLS-1$
        } else {
            el.setAttribute(attribute, value);
        }
    }

    public static final String getAttributeConvertEmptyStringToNull(Element el, String attribute) {
        String value = el.getAttribute(attribute);
        if (IpsStringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    public static final boolean getBooleanAttributeOrFalse(Element el, String attribute) {
        return el.hasAttribute(attribute) ? Boolean.parseBoolean(el.getAttribute(attribute)) : false;
    }

    public static final String getAttributeOrEmptyString(Element el, String attribute) {
        return el.hasAttribute(attribute) ? el.getAttribute(attribute)
                : IpsStringUtils.EMPTY;
    }

    public static final String dateToXmlDateString(Date date) {
        if (date == null) {
            return IpsStringUtils.EMPTY;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToXmlDateString(calendar);
    }

    public static final String gregorianCalendarToXmlDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return IpsStringUtils.EMPTY;
        }
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        return calendar.get(Calendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "-" + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Parses the given XML String to a Date.
     */
    public static final Date parseXmlDateStringToDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return parseGregorianCalendar(s).getTime();
        } catch (XmlParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static final GregorianCalendar parseGregorianCalendar(String s) throws XmlParseException {
        if (IpsStringUtils.isEmpty(s)) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-"); //$NON-NLS-1$
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (NumberFormatException e) {
            throw new XmlParseException("Can't parse " + s + " to a date!", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Creates a {@link Validator} for {@link Schema} validation. Per default the validator will log
     * XSD errors and warnings and only throws a {@link RuntimeException} if a fatal error occurs.
     *
     * @param ipsObjectType the IPS object type for loading the XSD schema file
     * @return an XSD validator
     */
    public static final Validator getXsdValidator(IpsObjectType ipsObjectType) {
        return VALIDATORS.computeIfAbsent(ipsObjectType, XsdValidatorHolder::new).get();
    }

    public static final void resetXsdValidator(IpsObjectType ipsObjectType) {
        VALIDATORS.remove(ipsObjectType);
    }

    public static final void resetXsdValidators() {
        VALIDATORS.clear();
    }

    /**
     * Creates a {@link Validator} for {@link Schema} validation. The {@code customErrorHandler}
     * will be used instead of the default one.
     * <p>
     * e.g. use a custom {@link ErrorHandler} that adds all warnings and errors to a list.
     *
     * @param ipsObjectType the IPS object type for loading the XSD schema file
     * @param customErrorHandler the error handler to use
     * @return an XSD validator
     */
    public static final Validator getXsdValidator(IpsObjectType ipsObjectType, ErrorHandler customErrorHandler) {
        Validator validator = getXsdValidator(ipsObjectType);
        validator.setErrorHandler(customErrorHandler);
        return validator;
    }

    public static final Element getFirstElement(Node parent, String tagName) {
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
    public static final Element getFirstElement(Node parent) {
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
     * element considering all child elements with the given tag name. In contrast to
     * {@link Element#getElementsByTagName(String)} this method returns only the direct children,
     * not all descendants.
     *
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     *
     * @see Element#getElementsByTagName(java.lang.String)
     */
    public static final Element getElement(Node parent, String tagName, int index) {
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
     * Returns the child element at the given index. The index is the position of the element
     * considering all child nodes of type element.
     *
     * @param parent The parent node.
     * @param index The 0 based position of the child.
     *
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     */
    public static final Element getElement(Node parent, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                if (count == index) {
                    return (Element)nl.item(i);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     */
    public static final Text getTextNode(Node node) {
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
    public static final CDATASection getFirstCDataSection(Node node) {
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
    public static final String getCDATAorTextContent(Node node) {
        if (XmlUtil.getFirstCDataSection(node) != null) {
            return XmlUtil.getFirstCDataSection(node).getData();
        } else if (XmlUtil.getTextNode(node) != null) {
            return XmlUtil.getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Adds a child Element by the name given in childName to the parent and returns the child.
     */
    public static final Element addNewChild(Document doc, Node parent, String childName) {
        Element e = doc.createElement(childName);
        parent.appendChild(e);
        return e;
    }

    /**
     * Adds a TextNode containing the text to the parent and returns the TextNode.
     */
    public static final Node addNewTextChild(Document doc, Node parent, String text) {
        Node n = doc.createTextNode(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a CDATASection containing the text to the parent and returns the CDATASection.
     */
    public static final Node addNewCDATAChild(Document doc, Node parent, String text) {
        Node n = doc.createCDATASection(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a TextNode or, if text contains chars&gt;127, a CDATASection containing the text to the
     * parent and returns this new child.
     */
    public static final Node addNewCDATAorTextChild(Document doc, Node parent, String text) {
        if (text == null) {
            return null;
        }
        char[] chars = text.toCharArray();
        boolean toCDATA = false;
        for (int i = 0; i < chars.length && !toCDATA; i++) {
            if (chars[i] < 32 || 126 < chars[i]) {
                toCDATA = true;
            }
        }
        return toCDATA ? addNewCDATAChild(doc, parent, text) : addNewTextChild(doc, parent, text);
    }

    /**
     * Returns the value for the given property from the given parent element. The parent XML
     * element must have the following format:
     *
     * <pre>
     * {@code <Parent>
     *   <Property isNull="false">value</Property>
     * </Parent>
     * }
     * </pre>
     *
     * @throws NullPointerException if parent or propertyName is <code>null</code> or the parent
     *             element does not contain an element with the given propertyName.
     * @deprecated use {@link ValueToXmlHelper#getValueFromElement(Element, String)} instead
     */
    @Deprecated
    public String getValueFromElement(Element parent, String propertyName) {
        Element propertyEl = XmlUtil.getFirstElement(parent, propertyName);
        if (propertyEl == null) {
            throw new NullPointerException();
        }
        String isNull = parent.getAttribute("isNull"); //$NON-NLS-1$
        if (Boolean.parseBoolean(isNull)) {
            return null;
        } else {
            Text textNode = getTextNode(parent);
            if (textNode == null) {
                return ""; //$NON-NLS-1$
            } else {
                return textNode.getNodeValue();
            }
        }
    }

    public static final String getSchemaLocation(IpsObjectType ipsObjectType) {
        return getSchemaLocation(ipsObjectType.getXmlElementName());
    }

    public static final String getIpsProjectPropertiesSchemaLocation() {
        return getSchemaLocation("ipsProjectProperties"); //$NON-NLS-1$
    }

    public static final String getSchemaLocation(String schemaName) {
        AVersion version = Abstractions.getVersion();
        return String.format(org.faktorips.runtime.internal.XmlUtil.FAKTOR_IPS_SCHEMA_URL + "%s/%s.xsd", //$NON-NLS-1$
                version.majorMinor().toString(),
                schemaName);
    }

    /**
     * Removes the ID-Attribute from the given element and all its children.
     */
    public static void removeIds(Element element) {
        if (isElementWithChildrenExcludedFromRemove(element)) {
            return;
        }
        element.removeAttribute(IIpsObjectPart.PROPERTY_ID);
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element child = (Element)childNodes.item(i);
                removeIds(child);
            }
        }
    }

    private static boolean isElementWithChildrenExcludedFromRemove(Element element) {
        return ELEMENTS_AND_CHILDREN_WITH_ID.contains(element.getNodeName());
    }
}
