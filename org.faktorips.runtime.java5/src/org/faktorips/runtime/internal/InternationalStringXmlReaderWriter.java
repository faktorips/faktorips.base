/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public static final String XML_ATTR_LOCALE = "locale"; //$NON-NLS-1$
    public static final String XML_ATTR_TEXT = "text"; //$NON-NLS-1$

    private InternationalStringXmlReaderWriter() {
        // Prevent instantiation.
    }

    /**
     * Creates a new {@link Element} containing the given {@link DefaultInternationalString}.
     * 
     * @param doc the xml {@link Document} used to store the new element.
     * @param internationalString the {@link DefaultInternationalString} to be saved.
     * @return the new element representing the given international string.
     */
    public static Element toXml(Document doc, DefaultInternationalString internationalString) {
        return toXml(doc, internationalString.getLocalizedStrings());
    }

    /**
     * Creates a new {@link Element} containing the given {@link LocalizedString localized strings}.
     * The element that is created represents an {@link DefaultInternationalString} even though the
     * element can be created from passing only the contents (the localized strings) of an
     * international string.
     * 
     * @param doc the xml {@link Document} used to store the new element.
     * @param localizedStrings the {@link LocalizedString localized strings} to be saved.
     * @return the new element representing an international string.
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
     *         international string represented by the given {@link Element}.
     */
    public static Collection<LocalizedString> fromXml(Element element) {
        List<LocalizedString> localizedStrings = new ArrayList<LocalizedString>();
        if (!element.getNodeName().equals(XML_TAG)) {
            return localizedStrings;
        }
        element.getChildNodes();
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
     *         international string.
     */
    public static Collection<LocalizedString> fromXml(Element element, String tagName) {
        Element valueEl = XmlUtil.getFirstElement(element, tagName);
        if (valueEl == null) {
            return null;
        }
        Element internationalStringElement = XmlUtil.getFirstElement(valueEl, XML_TAG);
        if (internationalStringElement == null) {
            return null;
        }
        return fromXml(internationalStringElement);
    }

}
