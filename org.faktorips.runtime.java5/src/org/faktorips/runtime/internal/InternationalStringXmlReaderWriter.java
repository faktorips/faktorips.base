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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class to read {@link InternationalString international strings} from XML and write them
 * back to XML.
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
     * Creates a new {@link Element} containing the given {@link InternationalString}.
     * 
     * @param doc the xml {@link Document} used to store the new element.
     * @param internationalString the {@link InternationalString} to be saved.
     * @return the new element representing the given international string.
     */
    public static Element toXml(Document doc, InternationalString internationalString) {
        return toXml(doc, internationalString.getLocalizedStrings());
    }

    /**
     * Creates a new {@link Element} containing the given {@link LocalizedString localized strings}.
     * The element that is created represents an {@link InternationalString} even though the element
     * can be created from passing only the contents (the localized strings) of an international
     * string.
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
     * Reads the {@link LocalizedString localized strings} stored in the given xml {@link Element}.
     * If the given element is not representing an {@link InternationalString}, an empty collection
     * is returned.
     * 
     * @param element the xml {@link Element} representing an {@link InternationalString}.
     * @return a collection of all {@link LocalizedString localized strings} defined in the
     *         {@link InternationalString} represented by the given {@link Element}.
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
     * that child does not represent an {@link InternationalString}, an empty collection is
     * returned.
     * 
     * @param element the xml {@link Element} representing the parent of an
     *            {@link InternationalString}.
     * @param tagName the name of the child node representing an {@link InternationalString}.
     * @return a collection of all {@link LocalizedString localized strings} defined in the
     *         {@link InternationalString}.
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

    /**
     * Reads an {@link InternationalString} from an XML snippet represented by the String parameter.
     * 
     * @param xmlSnippet The XML representation of the {@link InternationalString}
     * @return An {@link InternationalString} loaded from the XML snippet
     */
    public static InternationalString fromXml(String xmlSnippet) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlSnippet.getBytes()));
            Element rootElement = document.getDocumentElement();
            Collection<LocalizedString> collection = fromXml(rootElement);
            return new InternationalString(collection);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
