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
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to read international strings from XML and write them back to XML.
 * <p>
 * The helper should not be initialized, just use the static utility methods.
 */
public class InternationalStringXmlReaderWriter {

    public static final String XML_TAG = "InternationalString"; //$NON-NLS-1$
    public static final String XML_ELEMENT_LOCALIZED_STRING = "LocalizedString"; //$NON-NLS-1$
    public static final String XML_ATTR_DEFAULT_LOCALE = "defaultLocale"; //$NON-NLS-1$
    public static final String XML_ATTR_LOCALE = "locale"; //$NON-NLS-1$
    public static final String XML_ATTR_TEXT = "text"; //$NON-NLS-1$

    private InternationalStringXmlReaderWriter() {
        // Prevent instantiation.
    }

    /**
     * Creates a new {@link Element} representing the given {@link DefaultInternationalString}.
     * 
     * @param doc the XML {@link Document} that will contain the new element
     * @param internationalString the {@link DefaultInternationalString} to create an element for
     * @return the new element representing the given international string
     */
    public static Element toXml(Document doc, DefaultInternationalString internationalString) {
        Element element = toXml(doc, internationalString.getLocalizedStrings());
        element.setAttribute(XML_ATTR_DEFAULT_LOCALE, internationalString.getDefaultLocale().getLanguage());
        return element;
    }

    /**
     * Creates a new {@link Element} representing the given {@link LocalizedString localized
     * strings}.
     * <p>
     * The element that is created can be used to initialize the localized string of a
     * {@link DefaultInternationalString} but is missing the attribute for the default locale
     * required by {@link DefaultInternationalString}. That attribute is added by
     * {@code org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder} when writing a
     * product component's XML.
     * 
     * @param doc the XML {@link Document} that contains the new element
     * @param localizedStrings the {@link LocalizedString localized strings} to be saved
     * @return the new element representing the localized strings
     */
    public static Element toXml(Document doc, Collection<LocalizedString> localizedStrings) {
        Element element = doc.createElement(XML_TAG);
        for (LocalizedString localizedString : localizedStrings) {
            if (localizedString.getValue() != null) {
                Element partElement = doc.createElement(XML_ELEMENT_LOCALIZED_STRING);
                Locale locale = localizedString.getLocale();
                partElement.setAttribute(XML_ATTR_LOCALE, locale.toString());
                partElement.setAttribute(XML_ATTR_TEXT, localizedString.getValue());
                element.appendChild(partElement);
            }
        }
        return element;
    }

    /**
     * Reads the {@link LocalizedString localized strings} stored in the given XML {@link Element}.
     * If the given element is not representing an international string, an empty collection is
     * returned.
     * 
     * @param element the XML {@link Element} representing an international string
     * @return a collection of all {@link LocalizedString localized strings} defined in the
     *             international string represented by the given {@link Element}.
     */
    public static Collection<LocalizedString> fromXml(Element element) {
        List<LocalizedString> localizedStrings = new ArrayList<>();
        if (!element.getNodeName().equals(XML_TAG)) {
            return localizedStrings;
        }
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element partEl = (Element)item;
            if (partEl.getNodeName().equals(XML_ELEMENT_LOCALIZED_STRING)) {
                String localeString = partEl.getAttribute(XML_ATTR_LOCALE);
                Locale locale = new Locale(localeString);
                String value = partEl.getAttribute(XML_ATTR_TEXT);
                LocalizedString localizedString = new LocalizedString(locale, value);
                localizedStrings.add(localizedString);
            }
        }
        return localizedStrings;
    }

    /**
     * Reads the {@link LocalizedString localized strings} stored in the child {@link Element} with
     * the specified name. If the given element does not contain a child of the given name or if
     * that child does not represent an international string, an empty collection is returned.
     * 
     * @param element the XML {@link Element} representing the parent of an international string.
     * @param tagName the name of the child node representing an international string.
     * @return a collection of all {@link LocalizedString localized strings} defined in the
     *             international string.
     */
    public static Collection<LocalizedString> fromXml(Element element, String tagName) {
        Element internationalStringElement = getInternationalStringElement(element, tagName);
        if (internationalStringElement == null) {
            return null;
        }
        return fromXml(internationalStringElement);
    }

    /**
     * Reads the default locale from the international string stored in the child {@link Element}
     * with the specified name. Returns {@code null} if the given element does not contain a child
     * of the given name, if that child does not represent an international string or if that
     * international string does not have a default locale.
     * <p>
     * Note that the attribute for the default locale is only written by the
     * {@link #toXml(Document, DefaultInternationalString)} method. The
     * {@link #toXml(Document, Collection)} method does not write the attribute for the default
     * locale. {@code org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder} adds the
     * default locale when writing a product component's XML.
     * 
     * @param element the XML {@link Element} representing the parent of an international string
     * @param tagName the name of the child node representing an international string
     * @return the default locale defined in the international string if present or {@code null}
     */
    public static Locale defaultLocaleFromXml(Element element, String tagName) {
        Element internationalStringElement = getInternationalStringElement(element, tagName);
        if (internationalStringElement == null) {
            return null;
        }
        return defaultLocaleFromXml(internationalStringElement);
    }

    /**
     * Returns the default locale from the {@link #XML_ATTR_DEFAULT_LOCALE default locale attribute}
     * of the given element. Returns {@code null} if the element is not an
     * {@link InternationalStringXmlReaderWriter#XML_ELEMENT_LOCALIZED_STRING localized string}
     * element or does not have a default locale attribute.
     * <p>
     * Note that the attribute for the default locale is only written by the
     * {@link #toXml(Document, DefaultInternationalString)} method. The
     * {@link #toXml(Document, Collection)} method does not write the attribute for the default
     * locale. {@code org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder} adds the
     * default locale when writing a product component's XML.
     * 
     * @param e the XML element representing an international string
     * @return the default locale from the given element or {@code null} if the element does not
     *             represent an international string or does not have a default locale attribute
     */
    public static Locale defaultLocaleFromXml(Element e) {
        if (!e.getNodeName().equals(XML_TAG)) {
            return null;
        }
        String defaultLocale = e.getAttribute(XML_ATTR_DEFAULT_LOCALE);
        if (IpsStringUtils.isBlank(defaultLocale)) {
            return null;
        }
        return new Locale(defaultLocale);
    }

    /**
     * Returns the element representing an international string contained in the given element's
     * child with the given name. Returns {@code null} if no such child exists or the child does not
     * contain an element representing an international string.
     */
    private static Element getInternationalStringElement(Element element, String tagName) {
        return XmlUtil.findFirstElement(element, tagName).map(e -> XmlUtil.getFirstElement(e, XML_TAG)).orElse(null);
    }

    public static void setDefaultLocaleInXml(Node node, Locale defaultLocale) {
        if (node instanceof Element) {
            ((Element)node).setAttribute(XML_ATTR_DEFAULT_LOCALE, defaultLocale.getLanguage());
        }

    }
}
